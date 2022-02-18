package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public abstract class Explorer extends BaseExplorer {
    
    public Explorer(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
        System.out.println("Explorer in constructor coin_symbol: " + this.coin_symbol);
    }
    
    public abstract String get_address_weburl(String addr);
    
    public abstract double get_balance(String addr);
    
    public abstract long get_token_balance(String addr, String contract);
    
    public abstract HashMap<String, String> get_token_info(String contract);
    
}
