package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public abstract class PriceExplorer extends BaseExplorer {
    
    public PriceExplorer(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    
    public abstract double get_exchange_rate_between(String other_coin);
    
    public abstract double get_exchange_rate_between(String coin, String other_coin);
    
    public abstract double get_token_exchange_rate_between(String contract, String other_coin);
    
    public abstract double get_token_exchange_rate_between(String coin, String contract, String other_coin);
    
    public abstract String get_price_weburl();
        
    public abstract String get_price_weburl(String coin);
    
}
