package javacryptotools;

import static javacryptotools.TestVectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.satochip.javacryptotools.coins.BaseCoin;
import org.satochip.javacryptotools.coins.Bitcoin;
import org.satochip.javacryptotools.coins.BitcoinCash;
import org.satochip.javacryptotools.coins.Counterparty;
import org.satochip.javacryptotools.coins.Ethereum;
import org.satochip.javacryptotools.coins.Litecoin;
import org.satochip.javacryptotools.coins.Polygon;
import org.satochip.javacryptotools.coins.UnsupportedCoin;
import org.satochip.javacryptotools.explorers.Alchemy;
import org.satochip.javacryptotools.explorers.Blockcypher;
import org.satochip.javacryptotools.explorers.Blockscout;
import org.satochip.javacryptotools.explorers.Blockstream;
import org.satochip.javacryptotools.explorers.CoinCombined;
import org.satochip.javacryptotools.explorers.Coingate;
import org.satochip.javacryptotools.explorers.Fullstack;
import org.satochip.javacryptotools.explorers.LitecoinSpace;
import org.satochip.javacryptotools.explorers.Tokenscan;

// Offline tests for coin metadata, explorer wiring and explorer web urls (no network access)
class CoinPropertiesTest {

    static class Case {
        final Supplier<BaseCoin> factory;
        final boolean isTestnet;
        final String symbol;
        final String displayName;
        final boolean segwit;
        final boolean compressed;
        final boolean tokens;
        final boolean nfts;
        final List<Class<?>> explorers;
        final List<Class<?>> priceExplorers;
        final String address;
        final String addressWebUrl;

        Case(Supplier<BaseCoin> factory, boolean isTestnet, String symbol, String displayName,
             boolean segwit, boolean compressed, boolean tokens, boolean nfts,
             List<Class<?>> explorers, List<Class<?>> priceExplorers,
             String address, String addressWebUrl) {
            this.factory = factory;
            this.isTestnet = isTestnet;
            this.symbol = symbol;
            this.displayName = displayName;
            this.segwit = segwit;
            this.compressed = compressed;
            this.tokens = tokens;
            this.nfts = nfts;
            this.explorers = explorers;
            this.priceExplorers = priceExplorers;
            this.address = address;
            this.addressWebUrl = addressWebUrl;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    static List<Class<?>> classes(Class<?>... classes) {
        return Arrays.asList(classes);
    }

    static Stream<Case> cases() {
        return Stream.of(
                new Case(() -> new Bitcoin(false, null), false, "BTC", "Bitcoin",
                        true, true, false, false,
                        classes(Blockstream.class), classes(Coingate.class),
                        ADDRESS_SEGWIT_BTC[0], "https://blockstream.info/address/" + ADDRESS_SEGWIT_BTC[0]),
                new Case(() -> new Bitcoin(true, null), true, "BTCTEST", "Bitcoin Testnet",
                        true, true, false, false,
                        classes(Blockstream.class), classes(Coingate.class),
                        ADDRESS_SEGWIT_BTCTEST[0], "https://blockstream.info/testnet/address/" + ADDRESS_SEGWIT_BTCTEST[0]),
                new Case(() -> new Litecoin(false, null), false, "LTC", "Litecoin",
                        true, true, false, false,
                        classes(Blockcypher.class, LitecoinSpace.class), classes(Coingate.class),
                        ADDRESS_SEGWIT_LTC[0], "https://live.blockcypher.com/ltc/address/" + ADDRESS_SEGWIT_LTC[0]),
                new Case(() -> new Litecoin(true, null), true, "LTCTEST", "Litecoin Testnet",
                        true, true, false, false,
                        classes(LitecoinSpace.class), classes(Coingate.class),
                        ADDRESS_SEGWIT_LTCTEST[0], "https://litecoinspace.org/testnet/address/" + ADDRESS_SEGWIT_LTCTEST[0]),
                new Case(() -> new BitcoinCash(false, null), false, "BCH", "Bitcoin Cash",
                        false, true, false, false,
                        classes(Fullstack.class), classes(Coingate.class),
                        ADDRESS_CASHADDR_BCH[0], "https://www.blockchain.com/bch/address/" + ADDRESS_CASHADDR_BCH[0]),
                new Case(() -> new BitcoinCash(true, null), true, "BCHTEST", "Bitcoin Cash Testnet",
                        false, true, false, false,
                        classes(Fullstack.class), classes(Coingate.class),
                        ADDRESS_CASHADDR_BCHTEST[0], "https://www.blockchain.com/bch-testnet/address/" + ADDRESS_CASHADDR_BCHTEST[0]),
                new Case(() -> new Counterparty(false, null), false, "XCP", "Counterparty",
                        false, true, true, true,
                        classes(Tokenscan.class), classes(CoinCombined.class),
                        ADDRESS_LEGACY_BTC[0], "https://tokenscan.io/address/" + ADDRESS_LEGACY_BTC[0]),
                new Case(() -> new Counterparty(true, null), true, "XCPTEST", "Counterparty Testnet",
                        false, true, true, true,
                        classes(Tokenscan.class), classes(CoinCombined.class),
                        ADDRESS_LEGACY_BTCTEST[0], "https://testnet.tokenscan.io/address/" + ADDRESS_LEGACY_BTCTEST[0]),
                new Case(() -> new Ethereum(false, null), false, "ETH", "Ethereum",
                        false, false, true, true,
                        classes(Blockscout.class), classes(Coingate.class),
                        ADDRESS_ETH[0], "https://eth.blockscout.com/address/" + ADDRESS_ETH[0]),
                new Case(() -> new Ethereum(true, null), true, "ETHTEST", "Sepolia Testnet",
                        false, false, true, true,
                        classes(Blockscout.class), classes(Coingate.class),
                        ADDRESS_ETH[0], "https://eth-sepolia.blockscout.com/address/" + ADDRESS_ETH[0]),
                new Case(() -> new Polygon(false, null), false, "POL", "Polygon",
                        false, false, true, true,
                        classes(Alchemy.class), classes(Coingate.class),
                        ADDRESS_ETH[0], "https://polygonscan.com/address/" + ADDRESS_ETH[0]),
                new Case(() -> new Polygon(true, null), true, "POLTEST", "Polygon Amoy Testnet",
                        false, false, true, true,
                        classes(Alchemy.class), classes(Coingate.class),
                        ADDRESS_ETH[0], "https://amoy.polygonscan.com/address/" + ADDRESS_ETH[0]),
                new Case(() -> new UnsupportedCoin(false, null), false, "???", "Unsupported Coin",
                        false, false, false, false,
                        classes(), classes(),
                        "addr", "")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void coinMetadata(Case c) {
        BaseCoin coin = c.factory.get();
        assertEquals(c.isTestnet, coin.is_testnet);
        assertEquals(c.symbol, coin.coin_symbol);
        assertEquals(c.displayName, coin.display_name);
        assertEquals(c.segwit, coin.segwit_supported);
        assertEquals(c.segwit, coin.isSegwitSupported());
        assertEquals(c.compressed, coin.use_compressed_addr);
        assertEquals(c.compressed, coin.useCompressedAddress());
        assertEquals(c.tokens, coin.token_supported);
        assertEquals(c.nfts, coin.nft_supported);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void explorersAttached(Case c) {
        BaseCoin coin = c.factory.get();
        assertEquals(c.explorers, coin.explorers.stream().map(Object::getClass).collect(Collectors.toList()));
        assertEquals(c.priceExplorers, coin.priceExplorers.stream().map(Object::getClass).collect(Collectors.toList()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void addressWebUrl(Case c) {
        BaseCoin coin = c.factory.get();
        assertEquals(c.addressWebUrl, coin.getAddressWeburl(c.address));
    }

    static Stream<Case> testnetCases() {
        return cases().filter(c -> c.isTestnet);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testnetCases")
    void testnetExchangeRateIsZero(Case c) {
        // testnet coins have no market value: rate is 0 without any API request
        BaseCoin coin = c.factory.get();
        assertEquals(0, coin.get_exchange_rate_with("USD"));
        assertEquals(0, coin.get_exchange_rate_with("EUR"));
        assertEquals(0, coin.priceExplorers.get(0).get_exchange_rate_with("USD"));
    }
}
