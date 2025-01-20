package org.satochip.javacryptotools.explorers;

// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public interface PriceExplorer {

    public double get_exchange_rate_between(String other_coin);
    
    public double get_exchange_rate_between(String coin, String other_coin);

    public String get_price_weburl();
        
    public String get_price_weburl(String coin);
    
}
