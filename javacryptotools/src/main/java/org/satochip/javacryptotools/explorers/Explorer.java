package org.satochip.javacryptotools.explorers;

// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;

import org.satochip.javacryptotools.coins.Asset;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public interface Explorer {
    
    // public Explorer(String coin_symbol, Map<String, String> apikeys){
        // super(coin_symbol, apikeys);
    // }
    
    public String get_address_weburl(String addr);
    
    public double get_balance(String addr);

    public List<Asset> get_asset_list(String addr);

    public double get_token_balance(String addr, String contract);
    
    public HashMap<String, String> get_token_info(String contract);
    
}
