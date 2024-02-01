package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.Ethereum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;


public class Ethplorer extends BaseExplorer implements Explorer{
    
    // headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'}
    
    public Ethplorer(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    public Ethplorer(String coin_symbol, Map<String, String> apikeys, Level level){
        super(coin_symbol, apikeys, level);
    }

    public String get_api_url(){
        if (this.coin_symbol.equals("ETH")){
            return "https://api.ethplorer.io/";
        } else if (this.coin_symbol.equals("BNB")){
            return "https://api.binplorer.com/";
        } else if (this.coin_symbol.equals("tETH")){
            return "https://goerli-api.ethplorer.io/";
        } else {
            //todo throw CoinError.UnsupportedCoinError
            return "https://api.ethplorer.io/";
        }
    }
    
    public String get_address_weburl(String addr){       
        String web_url;
        if (this.coin_symbol.equals("ETH")){
            web_url= "https://etherscan.io/address/" + addr ;
        } else if (this.coin_symbol.equals("BNB")){
            return "https://binplorer.com/address/" + addr;
        } else {
            web_url= "https://goerli.etherscan.io/address/" + addr;
        }
        return web_url;
    }

   public String get_token_weburl(String contract){
        // https://ethplorer.io/address/{address}
        return this.get_address_weburl(contract);
    }

    public double get_balance(String addr) {

        try{
            // add to apikeys
            String apikey= (String) apikeys.get("API_KEY_ETHPLORER");
            String base_url = get_api_url();
            String url= base_url
                    + "getAddressInfo/"
                    + addr
                    + "?apiKey="
                    + apikey;
            logger.info("JAVACRYPTOTOOLS: Ethplorer get_balance  url: " + url);
            System.out.println("JAVACRYPTOTOOLS: Ethplorer get_balance  url: " + url);

            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Ethplorer get_balance content: " + content);
            System.out.println("JAVACRYPTOTOOLS: Ethplorer get_balance content: " + content);

            // parse json
            JSONObject reader = new JSONObject(content);
            Double balance = reader.getJSONObject("ETH").getDouble("balance");
            logger.info("JAVACRYPTOTOOLS: Ethplorer balance: " + balance);
            System.out.println("JAVACRYPTOTOOLS: Ethplorer balance: " + balance);
            return balance;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Ethplorer exception in balance: " + e);
            System.out.println("JAVACRYPTOTOOLS: Ethplorer exception in balance: " + e);
            throw new RuntimeException("Ethplorer: failed to fetch balance!" + e);
        }
    }
    
    public List<Asset> get_asset_list(String address){

        //https://api.ethplorer.io/getAddressInfo/0x2Ff9d7c0b98E0DeC39bF15568fe0864967583C44?apiKey=freekey
        
        try{
            // craft api request
            String apikey= (String) apikeys.get("API_KEY_ETHPLORER");
            String urlString = this.get_api_url()
                + "getAddressInfo/"
                + address
                + "?apiKey="
                + apikey;
            logger.info("JAVACRYPTOTOOLS: Ethplorer get_asset_list  url: " + urlString);

            // send request
            HttpsClient client= new HttpsClient(urlString);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Ethplorer get_asset_list content: " + content);

            // parse json
            List<Asset> assetList = new ArrayList<Asset>();
            JSONObject reader = new JSONObject(content);
            
            JSONArray tokens = reader.optJSONArray("tokens");
            if (tokens == null){return Collections.emptyList();}
            for (int i = 0; i < tokens.length(); i++){
                JSONObject token = tokens.getJSONObject(i);

                Asset asset = new Asset();
                asset.address = address;
                asset.type = AssetType.Token; // by default
                asset.balance = token.getString("rawBalance");
                System.out.println(asset.balance);

                JSONObject tokenInfo = token.getJSONObject("tokenInfo");
                asset.name = tokenInfo.getString("name");
                asset.contract = tokenInfo.getString("address");
                asset.symbol = tokenInfo.getString("symbol");
                asset.decimals = tokenInfo.optString("decimals", "0");
                System.out.println(asset.name);
                System.out.println(asset.contract);
                System.out.println(asset.symbol);
                System.out.println(asset.decimals);

                if (tokenInfo.has("price")){
                    JSONObject price = tokenInfo.optJSONObject("price");
                    if (price != null){
                        asset.rate = price.getDouble("rate");
                        asset.rateCurrency = price.getString("currency");
                        asset.rateAvailable = true;
                        System.out.println(asset.rate + " "  + asset.rateCurrency);
                    } else {
                        asset.rateAvailable = false;
                        System.out.println("No price rate available!");
                    }
                }

                asset.explorerLink = get_token_weburl(asset.contract);

                // if contract is known, fetch icon url from trustwallet CDN
                // TODO!
                 if (coin_symbol == "ETH"){
                     String tokenContract = asset.contract.toLowerCase();
                     logger.info("JAVACRYPTOTOOLS: Ethplorer get_asset_list tokenContract1: " + tokenContract);
                     tokenContract = Ethereum.toChecksumAddress(tokenContract);
                     logger.info("JAVACRYPTOTOOLS: Ethplorer get_asset_list tokenContract2: " + tokenContract);
                     asset.iconUrl = "https://assets-cdn.trustwallet.com/blockchains/ethereum/assets/" + tokenContract + "/logo.png";
                 } else if (coin_symbol == "BNB") {
                     String tokenContract = asset.contract.toLowerCase();
                     tokenContract = Ethereum.toChecksumAddress(tokenContract);
                     asset.iconUrl = "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/" + tokenContract + "/logo.png";
                 }

                // to get nft info, we use another method getNftList(addr: String, contract: String) from NftExplorer class
                assetList.add(asset);
                logger.info("JAVACRYPTOTOOLS: Ethplorer get_asset_list asset: " + asset);

            }

            return assetList;

        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Ethplorer get_asset_list exception: " + e);
            System.out.println("JAVACRYPTOTOOLS: Ethplorer get_asset_list exception: " + e);
            throw new RuntimeException("Ethplorer: failed to fetch Ethereum token balance: " + e);
        }
    }


    // todo deprecate?
    public double get_token_balance(String address, String contract){

        /* 
            https://api.etherscan.io/api
               ?module=account
               &action=tokenbalance
               &contractaddress=0x57d90b64a1a57749b0f932f1a3395792e12e7055
               &address=0xe04f27eb70e025b78871a2ad7eabe85e61212761
               &tag=latest&apikey=YourApiKeyToken
               
               {"status":"1","message":"OK","result":"135499"}
         */
    
        try{
            String apikey= (String) apikeys.get("API_KEY_ETHERSCAN");
            String base_url = get_api_url();
            String url= base_url + "?module=account&action=tokenbalance&contractaddress="+contract+"&address=" + address + "&tag=latest&apikey=" + apikey;
            logger.info("JAVACRYPTOTOOLS: Etherscan get_token_balance  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Etherscan get_token_balance content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            int statusCode = reader.getInt("status");
            if (statusCode!=1){
                throw new RuntimeException("Etherscan: Failed to fetch balance!");
            }
            long balanceLong = reader.getLong("result"); //
            logger.info("JAVACRYPTOTOOLS: Etherscan balance: " + balanceLong);
            // divide by decimals
            //double balance= (double) (balanceLong)/(Math.pow(10, 18)); // most ERC20 use 18 decimals but etherscan does not offer reliable way to find out...
            HashMap<String, String> tokenInfo= get_token_info(contract);
            String decimalsTxt= tokenInfo.get("decimals");
            int decimals= Integer.parseInt(decimalsTxt);
            double balance= (double) (balanceLong) / (Math.pow(10, decimals));
            return balance;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Etherscan exception in balance: " + e);
            throw new RuntimeException("Etherscan: failed to fetch Ethereum token balance!");
        }    
    }
    
    // todo deprecate?
    public HashMap<String, String> get_token_info(String contract){
        
        /* https://github.com/EverexIO/Ethplorer/wiki/Ethplorer-API#get-token-info
            
            {"error":{"code":1,"message":"Invalid API key"}}
        */
        
        HashMap<String, String> tokenInfo= new HashMap<String, String>();
        tokenInfo.put("name", "(unknown)");
        tokenInfo.put("symbol", "(unknown)");
        tokenInfo.put("decimals", "-1");
        try{
            String apikey= (String) apikeys.get("API_KEY_ETHPLORER");
            String base_url = "https://api.ethplorer.io";
            String url=  base_url + "/getTokenInfo/" + contract + "?apiKey=" + apikey;
            logger.info("JAVACRYPTOTOOLS: Ethplorer get_token_info  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Etherscan get_token_info content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            if (reader.has("error")){
                JSONObject error  = reader.getJSONObject("error");
                String msg= error.optString("message", "Unknown error");
                throw new RuntimeException("Failed to fetch token balance: "+ msg);
            }
            
            String name= reader.optString("name", "(unknown)");
            String symbol= reader.optString("symbol", "(unknown)");
            String decimals= reader.optString("decimals", "-1");
            tokenInfo.put("name", name);
            tokenInfo.put("symbol", symbol);
            tokenInfo.put("decimals", decimals);
            logger.info("JAVACRYPTOTOOLS: Etherscan name: " + name);
            logger.info("JAVACRYPTOTOOLS: Etherscan symbol: " + symbol);
            logger.info("JAVACRYPTOTOOLS: Etherscan decimals: " + decimals);
            return tokenInfo;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Etherscan exception in get_token_info: " + e);
            return tokenInfo;
        }  
    }
        
}
