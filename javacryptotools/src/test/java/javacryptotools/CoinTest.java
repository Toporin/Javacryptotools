package javacryptotools;

import static javacryptotools.TestVectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.satochip.javacryptotools.coins.Base58;
import org.satochip.javacryptotools.coins.BaseCoin;
import org.satochip.javacryptotools.coins.Bitcoin;
import org.satochip.javacryptotools.coins.BitcoinCash;
import org.satochip.javacryptotools.coins.Counterparty;
import org.satochip.javacryptotools.coins.Ethereum;
import org.satochip.javacryptotools.coins.Litecoin;
import org.satochip.javacryptotools.coins.Polygon;
import org.satochip.javacryptotools.coins.UnsupportedCoin;

// Offline tests for key & address encoding (no network access)
class CoinTest {

    static final ECParameterSpec SPEC = ECNamedCurveTable.getParameterSpec("secp256k1");

    static class Case {
        final String name;
        final Supplier<BaseCoin> factory;
        final String[] pubkeys;
        final String[] addresses; // expected result of pubToAddress()
        final String[] legacyAddresses; // expected result of pubToLegacyAddress(), or null
        final String[] wifs; // expected result of encodePrivkey(), or null

        Case(String name, Supplier<BaseCoin> factory, String[] pubkeys, String[] addresses,
             String[] legacyAddresses, String[] wifs) {
            this.name = name;
            this.factory = factory;
            this.pubkeys = pubkeys;
            this.addresses = addresses;
            this.legacyAddresses = legacyAddresses;
            this.wifs = wifs;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Case> cases() {
        return Stream.of(
                new Case("BTC", () -> new Bitcoin(false, null),
                        PUBKEY_BTC, ADDRESS_SEGWIT_BTC, ADDRESS_LEGACY_BTC, PRIVKEY_WIF_BTC),
                new Case("BTCTEST", () -> new Bitcoin(true, null),
                        PUBKEY_BTCTEST, ADDRESS_SEGWIT_BTCTEST, ADDRESS_LEGACY_BTCTEST, PRIVKEY_WIF_BTCTEST),
                new Case("LTC", () -> new Litecoin(false, null),
                        PUBKEY_LTC, ADDRESS_SEGWIT_LTC, ADDRESS_LEGACY_LTC, PRIVKEY_WIF_LTC),
                new Case("LTCTEST", () -> new Litecoin(true, null),
                        PUBKEY_BTCTEST, ADDRESS_SEGWIT_LTCTEST, ADDRESS_LEGACY_BTCTEST, PRIVKEY_WIF_BTCTEST),
                new Case("BCH", () -> new BitcoinCash(false, null),
                        PUBKEY_BCH, ADDRESS_CASHADDR_BCH, ADDRESS_LEGACY_BCH, PRIVKEY_WIF_BCH),
                new Case("BCHTEST", () -> new BitcoinCash(true, null),
                        PUBKEY_BCH, ADDRESS_CASHADDR_BCHTEST, ADDRESS_LEGACY_BCHTEST, PRIVKEY_WIF_BCHTEST),
                new Case("XCP", () -> new Counterparty(false, null),
                        PUBKEY_BTC, ADDRESS_LEGACY_BTC, ADDRESS_LEGACY_BTC, PRIVKEY_WIF_BTC),
                new Case("XCPTEST", () -> new Counterparty(true, null),
                        PUBKEY_BTCTEST, ADDRESS_LEGACY_BTCTEST, ADDRESS_LEGACY_BTCTEST, PRIVKEY_WIF_BTCTEST),
                new Case("ETH", () -> new Ethereum(false, null),
                        PUBKEY_ETH, ADDRESS_ETH, null, null),
                new Case("ETHTEST", () -> new Ethereum(true, null),
                        PUBKEY_ETH, ADDRESS_ETH, null, null),
                new Case("POL", () -> new Polygon(false, null),
                        PUBKEY_ETH, ADDRESS_ETH, null, null),
                new Case("POL testnet", () -> new Polygon(true, null),
                        PUBKEY_ETH, ADDRESS_ETH, null, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void pubToAddress_matchesTestVectors(Case c) {
        BaseCoin coin = c.factory.get();
        for (int i = 0; i < c.pubkeys.length; i++) {
            byte[] pubkey = Hex.decode(c.pubkeys[i]);
            if (!coin.use_compressed_addr) {
                pubkey = uncompress(pubkey);
            }
            assertEquals(c.addresses[i], coin.pubToAddress(pubkey), "pubkey index " + i);
        }
    }

    // coins using compressed pubkeys should also accept uncompressed pubkeys
    static Stream<Case> compressedCases() {
        return cases().filter(c -> c.factory.get().use_compressed_addr);
    }

    static Stream<Case> legacyCases() {
        return cases().filter(c -> c.legacyAddresses != null);
    }

    static Stream<Case> wifCases() {
        return cases().filter(c -> c.wifs != null);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("compressedCases")
    void pubToAddress_acceptsUncompressedPubkey(Case c) {
        BaseCoin coin = c.factory.get();
        for (int i = 0; i < c.pubkeys.length; i++) {
            byte[] pubkey = uncompress(Hex.decode(c.pubkeys[i]));
            assertEquals(c.addresses[i], coin.pubToAddress(pubkey), "pubkey index " + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("legacyCases")
    void pubToLegacyAddress_matchesTestVectors(Case c) {
        BaseCoin coin = c.factory.get();
        for (int i = 0; i < c.pubkeys.length; i++) {
            byte[] pubkey = Hex.decode(c.pubkeys[i]);
            assertEquals(c.legacyAddresses[i], coin.pubToLegacyAddress(pubkey), "pubkey index " + i);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wifCases")
    void encodePrivkey_matchesWifTestVectors(Case c) {
        BaseCoin coin = c.factory.get();
        for (int i = 0; i < c.wifs.length; i++) {
            byte[] privkey = wifToPrivkey(c.wifs[i]);
            assertEquals(c.wifs[i], coin.encodePrivkey(privkey), "privkey index " + i);
        }
    }

    @Test
    void ethereum_toChecksumAddress_followsEip55() {
        for (int i = 0; i < ADDRESS_ETH.length; i++) {
            assertEquals(ADDRESS_ETH_CHECKSUM[i], Ethereum.toChecksumAddress(ADDRESS_ETH[i]));
            // idempotent on already checksummed address
            assertEquals(ADDRESS_ETH_CHECKSUM[i], Ethereum.toChecksumAddress(ADDRESS_ETH_CHECKSUM[i]));
        }
    }

    @Test
    void ethereum_pubToAddress_accepts64BytesRawPubkey() {
        Ethereum coin = new Ethereum(false, null);
        byte[] uncompressed = uncompress(Hex.decode(PUBKEY_ETH[0]));
        byte[] raw = Arrays.copyOfRange(uncompressed, 1, 65);
        assertEquals(ADDRESS_ETH[0], coin.pubToAddress(raw));
    }

    @Test
    void ethereum_pubToAddress_rejectsCompressedPubkey() {
        Ethereum coin = new Ethereum(false, null);
        assertThrows(RuntimeException.class, () -> coin.pubToAddress(Hex.decode(PUBKEY_ETH[0])));
    }

    @Test
    void unsupportedCoin_returnsPlaceholders() {
        UnsupportedCoin coin = new UnsupportedCoin(false, null);
        byte[] pubkey = Hex.decode(PUBKEY_BTC[0]);
        assertEquals("(unsupported)", coin.pubToAddress(pubkey));
        assertEquals("(unsupported)", coin.pubToLegacyAddress(pubkey));
        assertEquals("(unsupported)", coin.pubToSegwitAddress(pubkey));
        assertEquals("(unsupported)", coin.encodePrivkey(new byte[32]));
    }

    /*
     *   HELPERS
     */

    // extract the 32-byte private key from a (compressed) WIF string
    static byte[] wifToPrivkey(String wif) {
        byte[] data = Base58.decodeChecked(wif); // version byte + privkey (+ 0x01 compression flag)
        return Arrays.copyOfRange(data, 1, 33);
    }

    static byte[] uncompress(byte[] compressed) {
        ECPoint point = SPEC.getCurve().decodePoint(compressed);
        return point.getEncoded(false);
    }
}
