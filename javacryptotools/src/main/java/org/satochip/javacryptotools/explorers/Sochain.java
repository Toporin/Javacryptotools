package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

//Docs: https://chain.so/api

public class Sochain extends Explorer{
    
    public static final String base_url = "https://chain.so/api/v2/";
    
    public Sochain(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }

    public String get_address_weburl(String addr){        
        String web_url= "https://chain.so/address/"+ this.coin_symbol + "/" + addr;
        return web_url;
    }

    public double get_balance(String addr) {
        
        String url= base_url + "get_address_balance/" + this.coin_symbol + "/" + addr;
        System.out.println("Sochain explorer  url: " + url);
        
        HttpsClient client= new HttpsClient(url);
        String content= client.request();
        System.out.println("Request content: " + content);
        
        // parse json
        JSONObject reader = new JSONObject(content);
        String status  = reader.getString("status");
        if (!status.equals("success")){
            throw new RuntimeException("Failed to fetch balance: "+ content);
        }
        JSONObject data = reader.getJSONObject("data");
        double balance= data.getDouble("confirmed_balance"); // TODO: unconfirmed_balance?
        // System.out.println("balance: " + balance);
        return balance;
    }
    
    public long get_token_balance(String address, String contract){
        return (long)-1;
    }
    
    public HashMap<String, String> get_token_info(String contract){
        return new HashMap<String, String>();
    }
    
}
