package org.satochip.javacryptotools.explorers;

// import org.json.JSONArray;
// import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public interface NftExplorer {
    
    // public NftExplorer(String coin_symbol, Map<String, String> apikeys){
        // super(coin_symbol, apikeys);
    // }
    
    public String get_nft_owner_weburl(String addr);
    
    public String get_nft_weburl(String contract, String tokenID);

    public List<Asset> get_nft_list(String address);
    //public List<Asset> get_nft_list(String address, String contract);

    public HashMap<String, String> get_nft_info(String contract, String tokenId);
    
    // TODO: deprecate
    public JSONObject get_nft_info_json(String contract, String tokenId);
    
}
