package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

public class Xchain extends BaseExplorer implements Explorer, NftExplorer{
    
    public Xchain(String coin_symbol, Map<String, String> apikeys){
        this(coin_symbol, apikeys, Level.WARNING);
    }
    public Xchain(String coin_symbol, Map<String, String> apikeys, Level logLevel){
        super(coin_symbol, apikeys, logLevel);
    }

    public String get_api_url(){
        if (this.coin_symbol.equals("XCP"))
            return  "https://xchain.io/";
        else if (this.coin_symbol.equals("XCPTEST"))
            return "https://testnet.xchain.io/";
        else if (this.coin_symbol.equals("XDP"))
            return "https://dogeparty.xchain.io/";
        else if (this.coin_symbol.equals("XDPTEST"))
            return "https://dogeparty-testnet.xchain.io/";
        else
            return "https://notfound.org/";
    }
    
    /* EXPLORER INTERFACE*/
    
    public String get_address_weburl(String addr){
        String base_url = get_api_url();
        return base_url + "address/" + addr;
    }

    public String get_token_weburl(String contract){
        String base_url = get_api_url();
        return base_url + "asset/" + contract;
    }

    public double get_balance(String addr){
        try{
            String base_url = get_api_url();
            String url = base_url + "api/address/"+  addr;
            logger.info("JAVACRYPTOTOOLS: Xchain get_balance  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Xchain get_balance content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            String balance_str= reader.optString("xcp_balance", "-1");
            double balance= Double.parseDouble(balance_str);
            return balance;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Xchain get_balance exception: " + e);
            throw new RuntimeException("Xchain get_balance exception: " + e);
        }   
    }

    public List<Asset> get_asset_list(String address){

        try {
            // craft https request
            String urlString = this.get_api_url() + "api/balances/" + address;
            logger.info("JAVACRYPTOTOOLS: Xchain get_asset_list  url: " + urlString);

            // send request
            HttpsClient client= new HttpsClient(urlString);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Xchain get_asset_list content: " + content);

            // parse json
            List<Asset> assetList = new ArrayList<Asset>();
            JSONObject reader = new JSONObject(content);
            JSONArray tokens = reader.getJSONArray("data");
            logger.info("JAVACRYPTOTOOLS: Xchain get_asset_list number of assets: " + tokens.length());
            for (int i = 0; i < tokens.length(); i++){
                JSONObject token = tokens.getJSONObject(i);

                String name = token.getString("asset");
                if (name.equals("XCP")) {
                    continue;
                }

                Asset asset = new Asset();
                asset.address = address;
                asset.type = AssetType.Token; // by default
                asset.name = name;
                logger.info("JAVACRYPTOTOOLS: Xchain get_asset_list asset: " + name);
                asset.contract = name;
                asset.symbol = name;
                asset.description = token.optString("description");
                asset.balance = token.getString("quantity");
                logger.info("JAVACRYPTOTOOLS: Xchain get_asset_list asset: " + asset.balance);
                asset.decimals = "0"; // not needed in xcp
                asset.explorerLink = this.get_token_weburl(asset.contract);

                // rate if available
                asset.rateAvailable = false;
                JSONObject priceObject = token.optJSONObject("estimated_value");
                if (priceObject != null){
                    String rateString = priceObject.optString("usd");
                    if (rateString != null){
                        try{
                            Double rate = Double.valueOf(rateString);
                            asset.rate = rate;
                            asset.rateCurrency = "USD";
                            asset.rateAvailable = true;
                        } catch (NumberFormatException e){
                            asset.rateAvailable = false;
                            logger.warning("Xchain get_asset_list failed to parse rate: " + e);
                        }
                    }

                }

                // parse description to recover image link if any
                if (asset.description != null){
                    String[] urlStrings = parse_description_field(asset.description);
                    if (urlStrings[0] != null || urlStrings[1] != null){
                        asset.nftImageLink = urlStrings[0];
                        asset.nftImageSmallLink = urlStrings[1];
                        asset.nftName = asset.name;
                        asset.nftDescription = asset.description;
                        asset.nftExplorerLink = asset.explorerLink;
                        asset.type = AssetType.NFT;
                    }
                }

                assetList.add(asset);
                logger.info("JAVACRYPTOTOOLS: Xchain get_asset_list new asset: " + asset);
            }

            return assetList;

        } catch (Exception e) {
            logger.warning("JAVACRYPTOTOOLS: Xchain get_asset_list exception: " + e);
            System.out.println("JAVACRYPTOTOOLS: Xchain get_asset_list exception: " + e);
            throw new RuntimeException("Xchain get_asset_list exception: " + e);
        }
    }

    // TODO: change from long to double in interface!
    // TODO: deprecate
    public double get_token_balance(String address, String contract){
        try{
            String base_url = get_api_url();
            String url = base_url + "api/balances/"+  address;
            logger.info("JAVACRYPTOTOOLS: xchain.io get_token_balance  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: xchain.io get_token_balance content: " + content);
            
            // parse json
            String balance_str="0";
            JSONObject reader = new JSONObject(content);
            JSONArray data_array= reader.getJSONArray("data");
            for (int i=0; i< data_array.length(); i++){
                JSONObject obj= data_array.getJSONObject(i);
                String asset= obj.optString("asset", "");
                if ( asset.equals(contract) ){
                    balance_str= obj.optString("quantity", "0");
                    break;
                }
            }
            double balance= Double.parseDouble(balance_str);
            logger.info("JAVACRYPTOTOOLS: xchain.io get_token_balance value: " + balance);
            return balance;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: xchain.io exception in balance: " + e);
            throw new RuntimeException("xchain.io: failed to fetch balance!");
        }   
    }
    
    // not really useful...
    // TODO: deprecate
    public HashMap<String, String> get_token_info(String contract){
        HashMap<String, String> tokenInfo= new HashMap<String, String>();
        tokenInfo.put("name", contract);
        tokenInfo.put("symbol", "");
        tokenInfo.put("decimals", "0");
        return tokenInfo;
    }
    
    /* EXPLORER INTERFACE*/
    
    public String get_nft_owner_weburl(String addr){
        String base_url = get_api_url();
        return base_url + "address/" + addr;
    }

    public String  get_nft_weburl(String contract, String tokenid){
        // tokenid is not used in xcp
        String base_url = get_api_url();
        return base_url + "asset/" + contract;
    }

    // take an asset (of type Token or NFT), and returns a list (possibly empty)
    // of NFTs based on this asset owner address and contract
    public List<Asset> get_nft_list(String address) {
        // for Xchain, all the needed info are already populated in get_asset_list()
        //throw new RuntimeException("Unsupported");
        return get_asset_list(address);
    }

    // TODO deprecate
    public HashMap<String, String> get_nft_info(String contract, String tokenId){
        try{
            // tokenid is not used...
            String base_url = get_api_url();
            String url = base_url + "api/asset/"+  contract;
            logger.info("JAVACRYPTOTOOLS: xchain.io explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: xchain.io request content: " + content);
            
            // default values
            HashMap<String, String> nftInfo= new HashMap<String, String>();
            nftInfo.put("nftName", contract);
            nftInfo.put("nftDescription", "");
            nftInfo.put("nftImageUrl", "");
            nftInfo.put("nftImageUrlLarge", "");
            nftInfo.put("nftExplorerLink", "");
            
            // parse json
            JSONObject reader = new JSONObject(content);
            String asset_longname= reader.optString("asset_longname", "");
            String supply= reader.optString("supply", "(unknown)");
            String divisible="";
            try{
                Boolean is_divisible= reader.optBoolean("divisible");
                divisible=String.valueOf(is_divisible);
            } catch (Exception ex){ // JSONException
                divisible= "(unknown)";
            }
            String locked="";
            try{
                Boolean is_locked= reader.optBoolean("locked");
                locked=String.valueOf(is_locked);
            } catch (Exception ex){ // JSONException
                locked= "(unknown)";
            }
            String description=  "Divisible: " + divisible + "\n" +
                                                "Locked: " + locked + "\n" + 
                                                "Supply: " + supply + "\n";
            
            // parse description field
            description+= "Description: "  + reader.optString("description", "") + "\n";
            String link= reader.optString("description", "");
            logger.info("JAVACRYPTOTOOLS: xchain.io request description: " + link);
            if (link.startsWith("*")){
                link= link.substring(1);
            }
            
            if (link.startsWith("imgur/")){
                link= link.substring(6); // remove "imgur/"
                String[] parts= link.split(";", 1);
                link=  "https://i.imgur.com/" + parts[0];
                nftInfo.put("nftImageUrl", link);
                nftInfo.put("nftImageUrlLarge", link);
            }
            else if ( link.startsWith("https://") || link.startsWith("http://")){
                if ( link.endsWith(".png") || 
                        link.endsWith(".jpg") ||
                        link.endsWith(".jpeg") ||
                        link.endsWith(".gif") ){
                    nftInfo.put("nftImageUrl", link);
                    nftInfo.put("nftImageUrlLarge", link);
                }
                else if (link.endsWith(".json")){
                    HttpsClient client2= new HttpsClient(link);
                    String content2= client2.request();
                    logger.info("JAVACRYPTOTOOLS: xchain.io request content2: " + content2);
                    JSONObject reader2 = new JSONObject(content2);
                    String nft_image_url= reader2.optString("image", "");
                    String nft_image_large_url= reader2.optString("image_large", "");
                    nftInfo.put("nftImageUrl", nft_image_url);
                    nftInfo.put("nftImageUrlLarge", nft_image_large_url);
                    
                    // add json info to description?
                    //description+= "JSON: "  + content2 + "\n";
                }
            }
            nftInfo.put("nftDescription", description);
            return nftInfo;
            
        }catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: xchain.io exception in get_nft_info: " + e);
            throw new RuntimeException("xchain.io: failed to fetch nft_info!");
        }   
    }
    
    // TODO: deprecate
    public JSONObject get_nft_info_json(String contract, String tokenId){
        throw new RuntimeException("xchain.io: unsupported function");
    }


    // utility to parse a counterparty description field
    // based on some heuristics
    public String[] parse_description_field(String description){

        String nftImageLink = null;
        String nftImageSmallLink = null;

        String link= description;
        logger.info("JAVACRYPTOTOOLS: Xchain parse_description_field: " + description);
        if (link.startsWith("*")){
            link= link.substring(1);
        }

        if (link.startsWith("imgur/")){
            link= link.substring(6); // remove "imgur/"
            String[] parts= link.split(";", 1);
            link=  "https://i.imgur.com/" + parts[0];
            nftImageLink = link;
            nftImageSmallLink = link;
        }
        else if ( link.startsWith("https://") || link.startsWith("http://")){
            if ( link.endsWith(".png") ||
                    link.endsWith(".jpg") ||
                    link.endsWith(".jpeg") ||
                    link.endsWith(".gif") ){
                nftImageLink = link;
                nftImageSmallLink = link;
            }
            else if (link.endsWith(".json")){
                HttpsClient client2= new HttpsClient(link);
                String content2= client2.request();
                logger.info("JAVACRYPTOTOOLS: Xchain content2: " + content2);
                JSONObject reader2 = new JSONObject(content2);
                nftImageSmallLink = reader2.optString("image");
                nftImageLink = reader2.optString("image_large");
            }
        }

        String[] stringArray = new String[2];
        stringArray[0]= nftImageLink;
        stringArray[1]= nftImageSmallLink;
        return stringArray;
    }
    
    
}
