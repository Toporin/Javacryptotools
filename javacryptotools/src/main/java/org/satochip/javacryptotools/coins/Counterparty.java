package org.satochip.javacryptotools;

import org.satochip.javacryptotools.explorers.*;

import java.util.Map;
import java.util.HashMap;

public class Counterparty extends BaseCoin {

    public Counterparty(boolean is_testnet, Map<String, String> apikeys){

        is_testnet= is_testnet;
        apikeys= apikeys;

        segwit_supported = false; // currently, use legacy
        use_compressed_addr = true;

        if (is_testnet){
            coin_symbol = "XCPTEST";
            display_name = "Counterparty Testnet";
            magicbyte = 111;
            script_magicbyte = 196;
            segwit_hrp = "tb";
            wif_prefix = 0xef;
            hd_path=1;
        } else{
            coin_symbol = "XCP";
            display_name = "Counterparty";
            magicbyte = 0;
            script_magicbyte = 5;
            segwit_hrp = "bc";
            wif_prefix = 0x80;
            hd_path=0;
        }
    
        token_supported= true;
        nft_supported= true;
        explorer= new Xchain(coin_symbol, apikeys);
        nftExplorer= new Xchain(coin_symbol, apikeys);
        if (is_testnet){
            priceExplorer= new Coingecko("testnet", apikeys);
        }else{
            priceExplorer= new Coingecko("xcp", apikeys);
        }
    }

}
