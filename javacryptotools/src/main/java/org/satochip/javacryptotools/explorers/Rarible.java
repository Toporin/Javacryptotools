package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

// https://docs.rarible.org/
// https://docs.rarible.org/api-reference/
// https://ethereum-api.rarible.org/v0.1/doc#operation/getNftItemMetaById

public class Rarible extends BaseExplorer implements NftExplorer{
    
    public Rarible(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    
    public String get_api_url(){
        if (this.coin_symbol.equals("ETH")){
            return "https://ethereum-api.rarible.org/v0.1/"; // Main
        } else {
            return "https://ethereum-api-dev.rarible.org/v0.1/"; // Ropsten
        }
    }
    
    public String get_nft_owner_weburl(String addr){
        // https://rarible.com/user/0xb3f8dae49c7f0e94d434db6088683c12d31a621f/owned
        String web_url="https://rarible.com/user/" + addr + "/owned";
        return web_url;
    };
    
    public String get_nft_weburl(String contract, String tokenID){       
        String web_url="https://rarible.com/token/" + contract  + ":" + tokenID;
        return web_url;
    }
    
    // DEPRECATED
    public JSONObject get_nft_info_json(String contract, String tokenID){
        /*
            https://ethereum-api.rarible.org/v0.1/nft/items/{itemId}/meta
        */
        try{
            //String apikey= "";//(String) apikeys.get('API_KEY_RARIBLE');
            String base_url = get_api_url();
            String url= base_url + "nft/items/"+contract+":" + tokenID + "/meta"; 
            logger.info("JAVACRYPTOTOOLS: Rarible explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Rarible request content: " + content);
            
            //parse json
            JSONObject nftInfo = new JSONObject(content);
            int status= nftInfo.optInt("status", 200);
            if (status !=200){
                throw new RuntimeException("Rarible: failed to fetch NFT info!");
            }
            return nftInfo;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Rarible exception in balance: " + e);
            throw new RuntimeException("Rarible: failed to fetch NFT info!");
        }    
    }
    
    public HashMap<String, String> get_nft_info(String contract, String tokenID){
        /*
            https://ethereum-api.rarible.org/v0.1/nft/items/{itemId}/meta
        */
        
        HashMap<String, String> nftInfoMap= new HashMap<String, String>();
        nftInfoMap.put("nftName", "");
        nftInfoMap.put("nftDescription", "");
        nftInfoMap.put("nftImageUrl", "");
        nftInfoMap.put("nftExplorerLink", "");
        try{
            //String apikey= "";//(String) apikeys.get('API_KEY_RARIBLE');
            String base_url = get_api_url();
            String url= base_url + "nft/items/"+contract+":" + tokenID + "/meta"; 
            logger.info("JAVACRYPTOTOOLS: Rarible explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Rarible request content: " + content);
            
            // parse json
            JSONObject nftInfo = new JSONObject(content);
            int status= nftInfo.optInt("status", 200);
            if (status !=200){
                throw new RuntimeException("Rarible: failed to fetch NFT info!");
            }
            String name = nftInfo.optString("name", "");
            String description = nftInfo.optString("description", "");
            JSONObject imageInfo= nftInfo.getJSONObject("image");
            String imageUrl= "";
            String imageUrlLarge= ""; 
            if (imageInfo != null){
                JSONObject imageUrls= imageInfo.getJSONObject("url");
                if (imageUrls !=null){
                    imageUrlLarge= imageUrls.optString("ORIGINAL", "");
                    imageUrl= imageUrls.optString("PREVIEW", "");
                }
            }
            nftInfoMap.put("nftName", name);
            nftInfoMap.put("nftDescription", description);
            nftInfoMap.put("nftImageUrl", imageUrl);
            nftInfoMap.put("nftImageUrlLarge", imageUrlLarge);
            // rarible link
            String nftExplorerLink= get_nft_weburl(contract, tokenID);
            nftInfoMap.put("nftExplorerLink", nftExplorerLink);
            
            return  nftInfoMap;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Rarible exception in get_nft_info: " + e);
            return  nftInfoMap; // map has been populated before
        }    
    }
        
}
