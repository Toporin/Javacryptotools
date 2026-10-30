package javacryptotools;

import static javacryptotools.TestVectors.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import org.satochip.javacryptotools.coins.AddressFormatException;
import org.satochip.javacryptotools.coins.Base58;
import org.satochip.javacryptotools.coins.Bech32;
import org.satochip.javacryptotools.coins.CashAddress;
import org.satochip.javacryptotools.coins.Constants;
import org.satochip.javacryptotools.coins.Utils;

// Offline tests for low-level encodings and constants
class EncodingTest {

    @Test
    void base58_roundTrip() {
        byte[] data = Hex.decode("00010203fffe");
        assertArrayEquals(data, Base58.decode(Base58.encode(data)));
        assertEquals("", Base58.encode(new byte[0]));
        assertEquals("11", Base58.encode(new byte[2])); // leading zeros are preserved
    }

    @Test
    void base58Checked_decodesLegacyAddressToHash160() {
        byte[] pubkey = Hex.decode(PUBKEY_BTC[0]);
        byte[] data = Base58.decodeChecked(ADDRESS_LEGACY_BTC[0]);
        assertEquals(21, data.length);
        assertEquals(0, data[0]); // P2PKH version byte
        byte[] hash160 = new byte[20];
        System.arraycopy(data, 1, hash160, 0, 20);
        assertArrayEquals(Utils.sha256hash160(pubkey), hash160);
        assertEquals(ADDRESS_LEGACY_BTC[0], Base58.encodeChecked(0, hash160));
    }

    @Test
    void base58Checked_rejectsBadChecksum() {
        String valid = ADDRESS_LEGACY_BTC[0];
        String corrupted = valid.substring(0, valid.length() - 1) + (valid.endsWith("v") ? "w" : "v");
        assertThrows(AddressFormatException.class, () -> Base58.decodeChecked(corrupted));
    }

    @Test
    void base58_rejectsInvalidCharacter() {
        assertThrows(AddressFormatException.class, () -> Base58.decode("0OIl"));
    }

    @Test
    void bech32_roundTrip() {
        for (String addr : ADDRESS_SEGWIT_BTC) {
            Bech32.Bech32Data decoded = Bech32.decode(addr);
            assertEquals("bc", decoded.hrp);
            assertEquals(Bech32.Encoding.BECH32, decoded.encoding);
            assertEquals(addr, Bech32.encode(decoded));
        }
        assertEquals("tltc", Bech32.decode(ADDRESS_SEGWIT_LTCTEST[0]).hrp);
    }

    @Test
    void bech32_rejectsBadChecksum() {
        String valid = ADDRESS_SEGWIT_BTC[0];
        String corrupted = valid.substring(0, valid.length() - 1) + (valid.endsWith("q") ? "p" : "q");
        assertThrows(AddressFormatException.class, () -> Bech32.decode(corrupted));
    }

    @Test
    void cashAddress_getPrefix() {
        assertEquals("bitcoincash", CashAddress.getPrefix(ADDRESS_CASHADDR_BCH[0]));
        assertEquals("bchtest", CashAddress.getPrefix(ADDRESS_CASHADDR_BCHTEST[0]));
        assertNull(CashAddress.getPrefix("qzq77lnqvtk8afrsjr2qqcha39lhm4wcmq5e75xsrg"));
    }

    @Test
    void constants_slip44MapsAreInverse() {
        assertEquals(Constants.MAP_SLIP44_BY_SYMBOL.size(), Constants.MAP_SYMBOL_BY_SLIP44.size());
        for (Map.Entry<String, Integer> entry : Constants.MAP_SLIP44_BY_SYMBOL.entrySet()) {
            assertEquals(entry.getKey(), Constants.MAP_SYMBOL_BY_SLIP44.get(entry.getValue()));
            assertTrue(entry.getValue() < 0, entry.getKey() + " slip44 must be hardened"); // 0x80000000 bit set
        }
    }

    @Test
    void constants_slip44Values() {
        // https://github.com/satoshilabs/slips/blob/master/slip-0044.md
        assertEquals(0x80000000, Constants.MAP_SLIP44_BY_SYMBOL.get("BTC"));
        assertEquals(0x80000000 | 2, Constants.MAP_SLIP44_BY_SYMBOL.get("LTC"));
        assertEquals(0x80000000 | 9, Constants.MAP_SLIP44_BY_SYMBOL.get("XCP"));
        assertEquals(0x80000000 | 60, Constants.MAP_SLIP44_BY_SYMBOL.get("ETH"));
        assertEquals(0x80000000 | 145, Constants.MAP_SLIP44_BY_SYMBOL.get("BCH"));
        assertEquals(0x80000000 | 966, Constants.MAP_SLIP44_BY_SYMBOL.get("POL"));
    }

    @Test
    void constants_supportsToken() {
        assertTrue(Constants.SUPPORTS_TOKEN_SET.contains("ETH"));
        assertTrue(Constants.SUPPORTS_TOKEN_SET.contains("POL"));
        assertTrue(Constants.SUPPORTS_TOKEN_SET.contains("XCP"));
        assertEquals(false, Constants.SUPPORTS_TOKEN_SET.contains("BTC"));
    }
}
