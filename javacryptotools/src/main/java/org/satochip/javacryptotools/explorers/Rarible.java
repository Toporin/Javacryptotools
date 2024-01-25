package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

// https://docs.rarible.org/
// https://docs.rarible.org/api-reference/
// https://ethereum-api.rarible.org/v0.1/doc#operation/getNftItemMetaById

public class Rarible extends BaseExplorer implements NftExplorer{
    
    public Rarible(String coin_symbol, Map<String, String> apikeys){
        this(coin_symbol, apikeys, Level.WARNING);
    }
    public Rarible(String coin_symbol, Map<String, String> apikeys, Level logLevel){
        super(coin_symbol, apikeys, logLevel);
    }

    public String get_api_url(){
        if (this.coin_symbol.equals("ETH")){
            return "https://ethereum-api.rarible.org/v0.1/"; // Main
        } else {
            return "https://ethereum-api-dev.rarible.org/v0.1/"; // goerli?
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

    public List<Asset> get_nft_list(String address) {
        logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list START");

        List<Asset> assetList = new ArrayList<Asset>();
        if (address==null){
            return assetList;
        }

        try {
            //https://ethereum-api.rarible.org/v0.1/doc#tag/nft-item-controller/operation/getNftItemsByOwner
            String apikey= (String) apikeys.get("API_KEY_RARIBLE");
            //logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list url apikey: " + apikey);
            String base_url = get_api_url();
            String url = base_url
                    + "nft/items/byOwner?owner="
                    + address;
            logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list url: " + url);

            // send request
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("X-API-KEY", apikey);
            HttpsClient client= new HttpsClient(url, headers);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list content: " + content);

            // DEBUG
//            String url2 = base_url
//                    + "nft/ownerships/byOwner?owner="
//                    + address;
//            logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list url2: " + url2);
//            String content2= client.request();
//            logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list content2: " + content2);
//            // debug2
//            String url3 = base_url
//                    + "nft/ownerships/byOwner?owner="
//                    + address
//                    + "&collection="
//                    + contract;
//            logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list url3: " + url3);
//            String content3= client.request();
//            logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list content3: " + content3);
            //ENDBUG

            // parse json
            JSONObject reader = new JSONObject(content);
            JSONArray tokens = reader.getJSONArray("items");
            for (int i = 0; i < tokens.length(); i++) {
                JSONObject token = tokens.getJSONObject(i);

                Asset asset = new Asset();
                asset.address = address;
                asset.contract = token.getString("contract");
                logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list : asset.contract" + asset.contract);
                //token.getBigInteger("tokenId").toString(); // not supported on android?
                try{
                    //long tokenidLong = token.optLong("tokenId"); // return 0 if fails
                    long tokenidLong = token.getLong("tokenId"); // throw if fail
                    asset.tokenid = String.valueOf(tokenidLong);
                } catch (Exception e){
                    asset.tokenid = "NaN"; // todo?
                    logger.warning("JAVACRYPTOTOOLS: Rarible get_nft_list parse tokenid exception: " + e);
                }
                logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list : asset.tokenid" + asset.tokenid);
                asset.balance= "1";
                asset.decimals= "0";

                // get more info from contract + tokenid
                HashMap<String, String> nftMap = get_nft_info(asset.contract, asset.tokenid);
                logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list nftMap: " + nftMap);
                asset.nftName=  nftMap.get("nftName");
                logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list nftMap asset.nftName: " + asset.nftName);
                logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list nftMap nftMap.get(nftName): " + nftMap.get("nftName"));
                asset.nftDescription= nftMap.get("nftDescription");
                asset.nftImageLink= nftMap.get("nftImageUrl");
                asset.nftImageSmallLink= asset.nftImageLink;
                asset.nftExplorerLink= get_nft_weburl(asset.contract, asset.tokenid); //nftMap.get("nftExplorerLink");
                asset.type= AssetType.NFT;

                assetList.add(asset);
                logger.info("JAVACRYPTOTOOLS: Rarible get_nft_list nft: " + asset);
            }

            return assetList;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Rarible get_nft_list exception: " + e);
            throw new RuntimeException("JAVACRYPTOTOOLS: Rarible get_nft_list exception: " + e);
        }
    }

    public HashMap<String, String> get_nft_info(String contract, String tokenID){
        /*
            https://ethereum-api.rarible.org/v0.1/nft/items/{itemId}/meta
        */

        HashMap<String, String> nftInfoMap= new HashMap<String, String>();
//        nftInfoMap.put("nftName", "");
//        nftInfoMap.put("nftDescription", "");
//        nftInfoMap.put("nftImageUrl", "");
//        nftInfoMap.put("nftExplorerLink", "");
        try{
            String apikey= (String) apikeys.get("API_KEY_RARIBLE");
            String base_url = get_api_url();
            String url= base_url + "nft/items/"+contract+":" + tokenID + "/meta";
            logger.info("JAVACRYPTOTOOLS: Rarible explorer  url: " + url);

            // send http request
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("X-API-KEY", apikey);
            HttpsClient client= new HttpsClient(url, headers);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Rarible request content: " + content);

            // parse json
            JSONObject nftInfo = new JSONObject(content);
            int status= nftInfo.optInt("status", 200);
            if (status !=200){
                throw new RuntimeException("Rarible: failed to fetch NFT info!");
            }
            String name = nftInfo.optString("name");
            nftInfoMap.put("nftName", name);
            String description = nftInfo.optString("description");
            nftInfoMap.put("nftDescription", description);
            ///
            JSONArray metaList = nftInfo.getJSONArray("content");
            for (int i = 0; i < metaList.length(); i++) {
                JSONObject meta = metaList.getJSONObject(i);
                String type = meta.optString("@type");
                if (type!=null && type.equals("IMAGE")){
                    String imageUrl= meta.optString("url");
                    nftInfoMap.put("nftImageUrl", imageUrl);
                    nftInfoMap.put("nftImageUrlLarge", imageUrl);
                    break;
                    // todo: check 'representation' property: ORIGINAL, BIG or PREVIEW
                }
            }

            ///
            // deprecated?
//            JSONObject imageInfo= nftInfo.optJSONObject("image");
//            if (imageInfo != null){
//                JSONObject imageUrls= imageInfo.optJSONObject("url");
//                if (imageUrls !=null){
//                    String imageUrlLarge= imageUrls.optString("ORIGINAL");
//                    String imageUrl= imageUrls.optString("PREVIEW");
//                    nftInfoMap.put("nftImageUrl", imageUrl);
//                    nftInfoMap.put("nftImageUrlLarge", imageUrlLarge);
//                }
//            }
            // rarible link
            //String nftExplorerLink= get_nft_weburl(contract, tokenID);
            //nftInfoMap.put("nftExplorerLink", nftExplorerLink);

            return  nftInfoMap;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Rarible exception in get_nft_info: " + e);
            return  nftInfoMap; // map has been (partially) populated before
        }
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
    

        
}
