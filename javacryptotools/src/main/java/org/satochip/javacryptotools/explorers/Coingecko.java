package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

// https://www.coingecko.com/en/api/documentation
// !! coingecko uses non-standard coin symbol ('id') to reference coins in its API (e.g. "bitcoin", "bitcoin-cash", "ethereum"...)

public class Coingecko extends PriceExplorer{
    
    private final boolean isTestnet;
    
    public Coingecko(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
        if (coin_symbol.equals("testnet")){
            this.isTestnet=true;
        } else {
            this.isTestnet=false;
        }
        System.out.println("Coingecko in constructor");
    }
    
    public String get_api_url(){
            return "https://api.coingecko.com/api/v3/";
    }
    
    public String get_price_weburl(){
        return get_price_weburl(this.coin_symbol);
    }
    
    public String get_price_weburl(String coin){       
        String web_url="https://www.coingecko.com/en/coins/" + coin;
        return web_url;
    }
    
    public double get_exchange_rate_between(String other_coin){
        return get_exchange_rate_between(this.coin_symbol, other_coin);
    }
    
    public double get_exchange_rate_between(String coin, String other_coin){
        
        if (this.isTestnet){
            return (double)0;
        }
        
        // TODO: coin must be listed in https://api.coingecko.com/api/v3//coins/list
        // TODO: other_coin must be listed in https://api.coingecko.com/api/v3/simple/supported_vs_currencies
        try{
            String base_url = get_api_url();
            String url= base_url + "simple/price?ids=" + coin + "&vs_currencies=" + other_coin;
            System.out.println("Coingecko explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            System.out.println("Coingecko request content: " + content);
            
            // parse json
            JSONObject priceInfo = new JSONObject(content);
            JSONObject coinInfo = priceInfo.getJSONObject(coin);
            double rate= coinInfo.getDouble(other_coin);
            return rate;
        } catch (Exception e){
           return (double)-1; 
        }
        
    }
    
    public double get_token_exchange_rate_between(String contract, String other_coin){
        return get_token_exchange_rate_between(this.coin_symbol, contract, other_coin);
    }
    
    public double get_token_exchange_rate_between(String coin, String contract, String other_coin){
        // https://api.coingecko.com/api/v3/simple/token_price/ethereum?contract_addresses=0x95ad61b0a150d79219dcf64e1e6cc01f0b64c4ce&vs_currencies=usd
        if (this.isTestnet){
            return (double)0;
        }
        
        try{
            String base_url = get_api_url();
            String url= base_url + "simple/token_price/" + coin + "?contract_addresses="+contract + "&vs_currencies=" + other_coin;
            System.out.println("Coingecko explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            System.out.println("Coingecko request content: " + content);
            
            // parse json
            JSONObject priceInfo = new JSONObject(content);
            JSONObject contractInfo = priceInfo.getJSONObject(contract);
            double rate= contractInfo.getDouble(other_coin);
            return rate;
        } catch (Exception e){
           return (double)-1; 
        }
    }
      
}
