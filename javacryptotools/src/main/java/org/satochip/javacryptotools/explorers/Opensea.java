package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// https://docs.opensea.io/reference/retrieving-a-single-asset


public class Opensea extends BaseExplorer implements NftExplorer{
    
    public Opensea(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    
    public String get_api_url(){
        return "https://api.opensea.io/api/v1/";
    }
    
    public String get_address_weburl(String addr){       
        String web_url="https://example.com";
        return web_url;
    }
    
     public String get_nft_owner_weburl(String addr){
        // https://opensea.io/0x800b4dbcef65cb5d1b2f8e33d5d0bbcbffea2a8e
        String web_url="https://opensea.io/" + addr;
        return web_url;
    };
    
    public String get_nft_weburl(String contract, String tokenID){       
        String web_url="https://opensea.io/assets/" + contract  + "/" + tokenID;
        return web_url;
    }

    public List<Asset> get_nft_list(String address){
        return Collections.emptyList(); // todo
    }

    public JSONObject get_nft_info_json(String contract, String tokenID){
        /*
        https://api.opensea.io/api/v1/asset/0xb47e3cd837ddf8e4c57f05d70ab865de6e193bbb/1/?account_address=0xb47e3cd837ddf8e4c57f05d70ab865de6e193bbb
        */
        try{
            //String apikey= "";//(String) apikeys.get('API_KEY_OPENSEA');
            String base_url = get_api_url();
            String url= base_url + "asset/"+contract+"/" + tokenID + "/"; // todo? owner_address (optional)
            //String url= base_url + "asset/"+contract+"/" + tokenID + "/" + "?format=json"; // todo? owner_address (optional)
            logger.info("JAVACRYPTOTOOLS: Opensea explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Opensea request content: " + content);
            
            // parse json
            JSONObject nftInfo = new JSONObject(content);
            boolean isSuccess = nftInfo.optBoolean("success", true);
            if (!isSuccess){
                throw new RuntimeException("Opensea: failed to fetch balance!");
            }
            return nftInfo;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Opensea exception in balance: " + e);
            throw new RuntimeException("Opensea: failed to fetch NFT info!");
        }    
    }
    
    public HashMap<String, String> get_nft_info(String contract, String tokenID){
        throw new RuntimeException("Opensea: unsupported function");
    }

}
