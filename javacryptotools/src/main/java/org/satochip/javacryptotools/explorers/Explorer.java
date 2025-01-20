package org.satochip.javacryptotools.explorers;

// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;

import org.satochip.javacryptotools.coins.Asset;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public interface Explorer {

    public String get_address_weburl(String addr);
    
    public double get_balance(String addr);

    public List<Asset> get_asset_list(String addr);

}
