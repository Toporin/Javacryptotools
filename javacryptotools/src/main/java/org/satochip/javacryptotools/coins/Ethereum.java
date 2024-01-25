package org.satochip.javacryptotools.coins;

import org.satochip.javacryptotools.explorers.*;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.satochip.javacryptotools.utils.Hash;
import org.satochip.javacryptotools.utils.Numeric;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

public class Ethereum extends BaseCoin {

    public Ethereum(boolean is_testnet, Map<String, String> apikeys){
        this(is_testnet, apikeys, Level.WARNING);
    }
    public Ethereum(boolean is_testnet, Map<String, String> apikeys, Level logLevel){
        super(logLevel);

        is_testnet= is_testnet;
        apikeys= apikeys;

        segwit_supported = false;
        use_compressed_addr = false;

        xprv_headers = new HashMap<String, Integer>();
        xpub_headers = new HashMap<String, Integer>();
        wif_script_types = new HashMap<String, Integer>();
        wif_script_types.put("p2pkh", 0);
        wif_script_types.put("p2wpkh", 1);
        wif_script_types.put("p2wpkh-p2sh", 2);
        wif_script_types.put("p2sh", 5);
        wif_script_types.put("p2wsh", 6);
        wif_script_types.put("p2wsh-p2sh", 7);
        
        if (is_testnet){
            coin_symbol = "ROP";
            display_name = "Ropsten Testnet";

            magicbyte = 111;
            script_magicbyte = 196;
            segwit_hrp = "tb";
            wif_prefix = 0xef;
            hd_path=1;

            xprv_headers.put("p2pkh", 0x04358394);
            xprv_headers.put("p2wpkh", 0x04358394);
            xprv_headers.put("p2wpkh-p2sh", 0x044a4e28);
            xprv_headers.put("p2wsh", 0x2aa7a99);
            xprv_headers.put("p2wsh-p2sh", 0x295b005);

            xpub_headers.put("p2pkh", 0x043587cf);
            xpub_headers.put("p2wpkh", 0x043587cf);
            xpub_headers.put("p2wpkh-p2sh", 0x044a5262);
            xpub_headers.put("p2wsh", 0x2aa7ed3);
            xpub_headers.put("p2wsh-p2sh", 0x295b43f);
        } else{
            coin_symbol = "ETH";
            display_name = "Ethereum";

            magicbyte = 0;
            script_magicbyte = 5;
            segwit_hrp = "bc";
            wif_prefix = 0x80;
            hd_path=0;
            
            xprv_headers.put("p2pkh", 0x0488ade4);
            xprv_headers.put("p2wpkh", 0x4b2430c);
            xprv_headers.put("p2wpkh-p2sh", 0x049d7878);
            xprv_headers.put("p2wsh", 0x2aa7a99);
            xprv_headers.put("p2wsh-p2sh", 0x295b005);

            xpub_headers.put("p2pkh", 0x0488b21e);
            xpub_headers.put("p2wpkh", 0x4b24746);
            xpub_headers.put("p2wpkh-p2sh", 0x049d7cb2);
            xpub_headers.put("p2wsh", 0x2aa7ed3);
            xpub_headers.put("p2wsh-p2sh", 0x295b43f);
        }

        //explorer= new Etherscan(coin_symbol, apikeys);
        explorer= new Ethplorer(coin_symbol, apikeys, logLevel);
        token_supported= true;
        nft_supported= true;
        //nftExplorer= new Opensea(coin_symbol, apikeys);
        nftExplorer= new Rarible(coin_symbol, apikeys, logLevel);
        if (is_testnet){
            priceExplorer= new Coingecko("testnet", apikeys);
        }else{
            priceExplorer= new Coingecko("ethereum", apikeys);
        }
    }
    
    // see https://github.com/web3j/web3j/blob/master/crypto/src/main/java/org/web3j/crypto/Keys.java
    public String pubToAddress(byte[] pubkey){
        byte[] ethPubkey;
        if (pubkey.length==64){
            ethPubkey= pubkey;
        } else if (pubkey.length==65){ // remove first byte
            ethPubkey= new byte[64];
            System.arraycopy(pubkey, 1, ethPubkey, 0, 64);
        }
        else {
            throw new RuntimeException("Ethereum pubToAddress: wrong pubkey length: "+ pubkey.length);
        }
        
        Keccak.DigestKeccak keccak = new Keccak.Digest256();
        keccak.update(ethPubkey, 0, ethPubkey.length);
        byte[] hash= keccak.digest();
        byte[] hash20= Arrays.copyOfRange(hash, hash.length - 20, hash.length); // right most 160 bits
        String address= "0x" + Hex.toHexString(hash20);
        return address;
    }

    /**
     * Checksum address encoding as per <a
     * href="https://github.com/ethereum/EIPs/blob/master/EIPS/eip-55.md">EIP-55</a>.
     * Based on https://github.com/web3j/web3j/blob/master/crypto/src/main/java/org/web3j/crypto/Keys.java
     *
     * @param address a valid hex encoded address
     * @return hex encoded checksum address
     */
    public static String toChecksumAddress(String address) {
        String lowercaseAddress = Numeric.cleanHexPrefix(address).toLowerCase();
        String addressHash = Numeric.cleanHexPrefix(Hash.sha3String(lowercaseAddress));

        StringBuilder result = new StringBuilder(lowercaseAddress.length() + 2);

        result.append("0x");

        for (int i = 0; i < lowercaseAddress.length(); i++) {
            if (Integer.parseInt(String.valueOf(addressHash.charAt(i)), 16) >= 8) {
                result.append(String.valueOf(lowercaseAddress.charAt(i)).toUpperCase());
            } else {
                result.append(lowercaseAddress.charAt(i));
            }
        }

        return result.toString();
    }
}
