package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public class Blockstream extends Explorer{
    
    public Blockstream(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
        System.out.println("Blockstream in constructor coin_symbol: " + this.coin_symbol);
    }

    public String get_url(){
        System.out.println("Blockstream in get_url() coin_symbol: " + this.coin_symbol);
        if (this.coin_symbol.equals("BTC"))
            return "https://blockstream.info/api";
        else
            return "https://blockstream.info/testnet/api";
    }
    
    public String get_address_weburl(String addr){
        if (this.coin_symbol.equals("BTC"))
            return "https://blockstream.info/address/"+addr;
        else
            return "https://blockstream.info/testnet/address/"+addr;
    }

    public double get_balance(String addr){
        try{
            String base_url = get_url();
            String url = base_url+ "/address/" + addr; //address_url % (base_url, addr)
            System.out.println("Blockstream explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            System.out.println("Request content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            JSONObject chain_stats  = reader.getJSONObject("chain_stats");
            long funded_txo_sum= chain_stats.getLong("funded_txo_sum");
            System.out.println("funded_txo_sum: " + funded_txo_sum);
            long spent_txo_sum= chain_stats.getLong("spent_txo_sum");
            System.out.println("spent_txo_sum: " + spent_txo_sum);
            double  balance= (double) (funded_txo_sum - spent_txo_sum)/(100000000);
            System.out.println("balance: " + spent_txo_sum);
            return balance;
        } catch (Exception e){
            System.out.println("Exception in balance: " + e);
            throw new RuntimeException("Blockstream: failed to fetch balance!");
        }   
    }
    
    public long get_token_balance(String address, String contract){
        return (long)-1;
    }
    public HashMap<String, String> get_token_info(String contract){
        return new HashMap<String, String>();
    }
    
}
