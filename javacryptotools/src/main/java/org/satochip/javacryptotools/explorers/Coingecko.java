package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

// https://www.coingecko.com/en/api/documentation
// !! coingecko uses non-standard coin symbol ('id') to reference coins in its API (e.g. "bitcoin", "bitcoin-cash", "ethereum"...)
// also it does not support fiat to fiat exchange rate (eg EUR/USD)!

public class Coingecko extends BaseExplorer implements PriceExplorer{

    private static final Map<String, String> symbolToId;
    private static final Map<String, String> nameToId;
    static {
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("BTC", "bitcoin");
        map1.put("LTC", "litecoin");
        map1.put("BCH", "bitcoin-cash");
        map1.put("ETH", "ethereum");
        map1.put("ETC", "ethereum-classic");
        map1.put("XCP", "counterparty");
        symbolToId = Collections.unmodifiableMap(map1);

        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("BTC", "btc");
        map2.put("ETH", "eth");
        map2.put("EUR", "eur");
        map2.put("USD", "usd");
        nameToId = Collections.unmodifiableMap(map2);
    }
    
    private final boolean isTestnet;
    
    public Coingecko(String coin_symbol, Map<String, String> apikeys){
        this(coin_symbol, apikeys, Level.WARNING);
    }
    public Coingecko(String coin_symbol, Map<String, String> apikeys, Level logLevel){
        super(coin_symbol, apikeys, logLevel);
        if (coin_symbol.equals("testnet")){
            this.isTestnet=true;
        } else {
            this.isTestnet=false;
        }
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
        logger.info("JAVACRYPTOTOOLS: Coingecko get_exchange_rate_between START");
        // TODO: coin must be listed in https://api.coingecko.com/api/v3//coins/list
        // TODO: other_coin must be listed in https://api.coingecko.com/api/v3/simple/supported_vs_currencies
        try{
            String id1 = Coingecko.symbolToId.get(coin);
            String id2 = Coingecko.nameToId.get(other_coin);
            if (id1 == null || id2 == null){
                logger.warning("JAVACRYPTOTOOLS: Coingecko get_exchange_rate_between unsupported pair: " + id1 + " " + id2);
                return -1;
            }

            String base_url = get_api_url();
            String url= base_url + "simple/price?ids=" + id1 + "&vs_currencies=" + id2;
            logger.info("JAVACRYPTOTOOLS: Coingecko explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Coingecko request content: " + content);
            
            // parse json
            JSONObject priceInfo = new JSONObject(content);
            JSONObject coinInfo = priceInfo.getJSONObject(id1);
            double rate= coinInfo.getDouble(id2);
            logger.info("JAVACRYPTOTOOLS: Coingecko request rate: " + rate);
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
            logger.info("JAVACRYPTOTOOLS: Coingecko explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Coingecko request content: " + content);
            
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
