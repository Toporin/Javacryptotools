package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

//Docs: https://chain.so/api

// DEPRECATED
// Sochain updated its API to v3, which requires an API key
// Use BlockCypher instead
// todo: upgrade to v3 if needed?
public class Sochain extends BaseExplorer implements Explorer{

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
        logger.info("JAVACRYPTOTOOLS: Sochain explorer  url: " + url);
        
        HttpsClient client= new HttpsClient(url);
        String content= client.request();
        logger.info("JAVACRYPTOTOOLS: Sochain request content: " + content);
        
        // parse json
        JSONObject reader = new JSONObject(content);
        String status  = reader.getString("status");
        if (!status.equals("success")){
            throw new RuntimeException("Failed to fetch balance: "+ content);
        }
        JSONObject data = reader.getJSONObject("data");
        double balance= data.getDouble("confirmed_balance"); // TODO: unconfirmed_balance?
        logger.info("JAVACRYPTOTOOLS: balance: " + balance);
        return balance;
    }

    public List<Asset> get_asset_list(String address){
        return null;
    }

    public double get_token_balance(String address, String contract){
        return (double)-1;
    }
    
    public HashMap<String, String> get_token_info(String contract){
        return new HashMap<String, String>();
    }
    
}
