package org.satochip.javacryptotools;

import org.satochip.javacryptotools.explorers.*;

import java.util.Map;
import java.util.HashMap;

public class UnsupportedCoin extends BaseCoin {

    public UnsupportedCoin(boolean is_testnet, Map<String, String> apikeys){

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
            coin_symbol = "?";
            display_name = "Unsupported Testnet";

            //explorer = blockstream; //blockchain
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
            coin_symbol = "???";
            display_name = "Unsupported Coin";
            //explorer = blockstream; //blockchain
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
    
        token_supported= false;
        nft_supported= false;
        explorer= null;
        nftExplorer= null;
        priceExplorer= null;
    }
    
    public String encodePrivkey(byte[] privkey){
        return "(unsupported)";
    }
    
    public String pubToAddress(byte[] pubkey){
        return "(unsupported)";
    }
    
    public String pubToLegacyAddress(byte[] pubkey){
        return "(unsupported)";
    } 
    
    public String pubToSegwitAddress(byte[] pubkey)  throws AddressFormatException{
        return "(unsupported)";
    }

}
