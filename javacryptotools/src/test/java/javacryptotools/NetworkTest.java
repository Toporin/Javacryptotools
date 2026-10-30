package javacryptotools;

import static javacryptotools.TestVectors.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.BaseCoin;
import org.satochip.javacryptotools.coins.Bitcoin;
import org.satochip.javacryptotools.coins.BitcoinCash;
import org.satochip.javacryptotools.coins.Counterparty;
import org.satochip.javacryptotools.coins.Ethereum;
import org.satochip.javacryptotools.coins.Litecoin;
import org.satochip.javacryptotools.coins.Polygon;
import org.satochip.javacryptotools.explorers.Etherscan;

// Integration tests against third-party APIs: run with ./gradlew integrationTest
// Results depend on external services and on-chain state, so assertions are kept loose:
// balances that cannot decrease (unspendable or locked funds) must be > 0, other balances >= 0
// (i.e. the request succeeded, as failures return -1).
@Tag("network")
@Timeout(120)
class NetworkTest {

    static HashMap<String, String> apikeys;

    @BeforeAll
    static void loadApiKeys() {
        apikeys = ApiKeys.load();
    }

    static final String BTC_GENESIS = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"; // unspendable
    static final String BCH_GENESIS = "bitcoincash:qp3wjpa3tjlj042z2wv7hahsldgwhwy0rq9sywjpyy"; // unspendable
    static final String BTCTEST_FAUCET = "mipcBbFg9gMiCh81Kj8tqqdgoZub1ZJRfn";
    static final String LTC_ADDRESS = "LQTpS3VaYTjCr4s9Y1t5zbeY26zevf7Fb3";
    static final String ETH_DEPOSIT_CONTRACT = "0x00000000219ab540356cBB839Cbe05303d7705Fa"; // locked ETH
    static final String VITALIK = "0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045";
    static final String XCP_BURN_PEPE = "1BurnPepexxxxxxxxxxxxxxxxxxxAK33R";
    static final String XCPTEST_ADDRESS = "mvCounterpartyXXXXXXXXXXXXXXW24Hef";

    static class Case {
        final String name;
        final Function<HashMap<String, String>, BaseCoin> factory;
        final String address;
        final boolean positive; // balance must be > 0 (otherwise >= 0), or asset list must not be empty
        final String requiredApiKey; // or null

        Case(String name, Function<HashMap<String, String>, BaseCoin> factory, String address,
             boolean positive, String requiredApiKey) {
            this.name = name;
            this.factory = factory;
            this.address = address;
            this.positive = positive;
            this.requiredApiKey = requiredApiKey;
        }

        BaseCoin coin() {
            if (requiredApiKey != null) {
                assumeTrue(ApiKeys.has(apikeys, requiredApiKey), "missing " + requiredApiKey + " in api_keys.ini");
            }
            return factory.apply(apikeys);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /*
     *   BALANCES
     */

    static Stream<Case> balanceCases() {
        return Stream.of(
                new Case("BTC", keys -> new Bitcoin(false, keys), BTC_GENESIS, true, null),
                new Case("BTCTEST", keys -> new Bitcoin(true, keys), BTCTEST_FAUCET, false, null),
                new Case("LTC", keys -> new Litecoin(false, keys), LTC_ADDRESS, false, null),
                new Case("LTCTEST", keys -> new Litecoin(true, keys), ADDRESS_SEGWIT_LTCTEST[0], false, null),
                new Case("XCP", keys -> new Counterparty(false, keys), XCP_BURN_PEPE, false, null),
                new Case("XCPTEST", keys -> new Counterparty(true, keys), XCPTEST_ADDRESS, false, null),
                new Case("ETH", keys -> new Ethereum(false, keys), ETH_DEPOSIT_CONTRACT, true, null),
                new Case("ETHTEST", keys -> new Ethereum(true, keys), VITALIK, false, null),
                // polygon.blockscout.com is behind a Cloudflare challenge: currently works with the Etherscan fallback only
                new Case("POL", keys -> new Polygon(false, keys), VITALIK, false, Etherscan.APIKEY_NAME),
                new Case("POLTEST", keys -> new Polygon(true, keys), VITALIK, false, Etherscan.APIKEY_NAME)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("balanceCases")
    void getBalance(Case c) {
        double balance = c.coin().getBalance(c.address);
        if (c.positive) {
            assertTrue(balance > 0, "expected positive balance, got " + balance);
        } else {
            assertTrue(balance >= 0, "balance request failed (got " + balance + ")");
        }
    }

    static final String FULLSTACK_DOWN = "Fullstack API (api.fullstack.cash) returns an html page instead of json: BCH explorer needs a replacement";

    @Disabled(FULLSTACK_DOWN)
    @Test
    void getBalance_bch() {
        getBalance(new Case("BCH", keys -> new BitcoinCash(false, keys), BCH_GENESIS, true, null));
    }

    @Disabled(FULLSTACK_DOWN)
    @Test
    void getBalance_bchTestnet() {
        getBalance(new Case("BCHTEST", keys -> new BitcoinCash(true, keys), ADDRESS_CASHADDR_BCHTEST[0], false, null));
    }

    /*
     *   TOKENS & NFTS
     */

    static Stream<Case> assetCases() {
        return Stream.of(
                new Case("ETH", keys -> new Ethereum(false, keys), ETH_DEPOSIT_CONTRACT, true, null),
                // polygon.blockscout.com is behind a Cloudflare challenge: currently works with the Etherscan fallback only
                new Case("POL", keys -> new Polygon(false, keys), VITALIK, true, Etherscan.APIKEY_NAME),
                new Case("POLTEST", keys -> new Polygon(true, keys), VITALIK, true, Etherscan.APIKEY_NAME),
                // only holds XCP, which is not listed as an asset: checks that the request succeeds
                new Case("XCPTEST", keys -> new Counterparty(true, keys), XCPTEST_ADDRESS, false, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("assetCases")
    void getAssetList(Case c) {
        List<Asset> assets = c.coin().getAssetList(c.address);
        assertNotNull(assets, "asset list request failed");
        if (c.positive) {
            assertFalse(assets.isEmpty(), "expected at least one asset");
        }
        for (Asset asset : assets) {
            assertNotNull(asset.type);
            assertNotNull(asset.balance, "missing balance for " + asset.name);
            if (asset.type == AssetType.NFT) {
                assertNotNull(asset.nftExplorerLink, "missing nft explorer link for " + asset.name);
            } else {
                assertNotNull(asset.explorerLink, "missing explorer link for " + asset.name);
                assertFalse(asset.explorerLink.endsWith("/token/"), "missing contract in " + asset.explorerLink);
            }
        }
    }

    /*
     *   EXCHANGE RATES
     */

    static Stream<Case> rateCases() {
        return Stream.of(
                new Case("BTC", keys -> new Bitcoin(false, keys), null, true, null),
                new Case("LTC", keys -> new Litecoin(false, keys), null, true, null),
                new Case("BCH", keys -> new BitcoinCash(false, keys), null, true, null),
                new Case("XCP", keys -> new Counterparty(false, keys), null, true, null),
                new Case("ETH", keys -> new Ethereum(false, keys), null, true, null),
                new Case("POL", keys -> new Polygon(false, keys), null, true, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rateCases")
    void exchangeRateWithFiat(Case c) {
        BaseCoin coin = c.coin();
        for (String fiat : new String[]{"USD", "EUR"}) {
            double rate = coin.get_exchange_rate_with(fiat);
            assertTrue(rate > 0, coin.coin_symbol + "/" + fiat + " rate request failed (got " + rate + ")");
        }
    }
}
