package org.satochip.javacryptotools.explorers;

// import org.json.JSONArray;
// import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public interface NftExplorer {
    
    // public NftExplorer(String coin_symbol, Map<String, String> apikeys){
        // super(coin_symbol, apikeys);
    // }
    
    public String get_nft_owner_weburl(String addr);
    
    public String get_nft_weburl(String contract, String tokenID);
    
    public HashMap<String, String> get_nft_info(String contract, String tokenId);
    
    // TODO: deprecate
    public JSONObject get_nft_info_json(String contract, String tokenId);
    
}
