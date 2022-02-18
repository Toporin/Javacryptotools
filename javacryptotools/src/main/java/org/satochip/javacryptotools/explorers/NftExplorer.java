package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

public abstract class NftExplorer extends BaseExplorer {
    
    public NftExplorer(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    
    public abstract String get_nft_owner_weburl(String addr);
    
    public abstract String get_nft_weburl(String contract, String tokenID);
    
    public abstract HashMap<String, String> get_nft_info(String contract, String tokenId);
    
    public abstract JSONObject get_nft_info_json(String contract, String tokenId);
    
}
