package org.satochip.javacryptotools;

import org.satochip.javacryptotools.explorers.*;

import java.util.Map;
import java.util.HashMap;

public class BitcoinCash extends BaseCoin {
    
    public String cashAddrPrefix;
    public String simpleledgerPrefix;
    public static final byte cashAddressTypePubkey= 0; 
    public static final byte cashAddressTypeScript=1;
    
    public BitcoinCash(boolean is_testnet, Map<String, String> apikeys){
        
        is_testnet= is_testnet;
        apikeys= apikeys;

        segwit_supported = false;
        use_compressed_addr = true;

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
            coin_symbol = "BCHTEST";
            display_name = "Bitcoin Cash Testnet";

            //explorer = blockstream; //blockchain
            magicbyte = 111;
            script_magicbyte = 196;
            segwit_hrp = "tb";
            wif_prefix = 0xef;
            hd_path=1;
            cashAddrPrefix = "bchtest";
            simpleledgerPrefix = "slptest";
        
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
            coin_symbol = "BCH";
            display_name = "Bitcoin Cash";
            //explorer = blockstream; //blockchain
            magicbyte = 0;
            script_magicbyte = 5;
            segwit_hrp = "bc";
            wif_prefix = 0x80;
            hd_path = 145;
            cashAddrPrefix = "bitcoincash";
            simpleledgerPrefix = "simpleledger";
            
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
    
        token_supported= false;
        nft_supported= false;
        explorer= new Fullstack(coin_symbol, apikeys);
        if (is_testnet){
            priceExplorer= new Coingecko("testnet", apikeys);
        }else{
            priceExplorer= new Coingecko("bitcoin-cash", apikeys);
        }
    }
    
    
    public String pubToAddress(byte[] pubkey){
        
        String legacy= pubToLegacyAddress(pubkey);
        // CashAddress CASH = CashAddress.CASH;
        // String cashAddress= CASH.encodeCashAdrressByLegacy(legacy);
        
        // compute hash160
        byte[] bytes;
        if (use_compressed_addr){
            
            if (pubkey.length==65){
                // compress pubkey
                bytes = new byte[33];
                int parity= pubkey[64]%2;
                System.arraycopy(pubkey, 0, bytes, 0, 33);
                bytes[0] = (parity==0)?  (byte)0x02 : (byte)0x03;
            } else {
                bytes= pubkey;
            }
            
        } else {
            if (pubkey.length==65){
                bytes= pubkey;
            } else {
                // should not happen!
                throw new AddressFormatException("Wrong pubkey size!");
            }
        }

        byte[] pubkeyHash = Utils.sha256hash160(bytes);
        byte[] pack= CashAddress.packAddressData(pubkeyHash, cashAddressTypePubkey);
        String cashAddress= CashAddress.encodeCashAddress(cashAddrPrefix, pack);  
        return cashAddress;
    }
    
}
