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
    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Ethplorer(String coin_symbol, Map<String, String> apikeys, Level level){
        this(coin_symbol, apikeys);
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
                    + addr;
            logFine("request: " + url); // without api key

            HttpsClient client= new HttpsClient(url + "?apiKey=" + apikey);
            String content= client.request();
            logResponse(content);

            // parse json
            JSONObject reader = new JSONObject(content);
            Double balance = reader.getJSONObject("ETH").getDouble("balance");
            return balance;
        } catch (Exception e){
            throw new RuntimeException("failed to fetch balance: " + e, e);
        }
    }
    
    public List<Asset> get_asset_list(String address){

        //https://api.ethplorer.io/getAddressInfo/0x2Ff9d7c0b98E0DeC39bF15568fe0864967583C44?apiKey=freekey
        
        try{
            // craft api request
            String apikey= (String) apikeys.get("API_KEY_ETHPLORER");
            String urlString = this.get_api_url()
                + "getAddressInfo/"
                + address;
            logFine("request: " + urlString); // without api key

            // send request
            HttpsClient client= new HttpsClient(urlString + "?apiKey=" + apikey);
            String content= client.request();
            logResponse(content);

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

                JSONObject tokenInfo = token.getJSONObject("tokenInfo");
                asset.name = tokenInfo.getString("name");
                asset.contract = tokenInfo.getString("address");
                asset.symbol = tokenInfo.getString("symbol");
                asset.decimals = tokenInfo.optString("decimals", "0");

                if (tokenInfo.has("price")){
                    JSONObject price = tokenInfo.optJSONObject("price");
                    if (price != null){
                        asset.rate = price.getDouble("rate");
                        asset.rateCurrency = price.getString("currency");
                        asset.rateAvailable = true;
                    } else {
                        asset.rateAvailable = false;
                    }
                }

                asset.explorerLink = get_token_weburl(asset.contract);

                // if contract is known, fetch icon url from trustwallet CDN
                // TODO!
                 if (coin_symbol == "ETH"){
                     String tokenContract = asset.contract.toLowerCase();
                     tokenContract = Ethereum.toChecksumAddress(tokenContract);
                     asset.iconUrl = "https://assets-cdn.trustwallet.com/blockchains/ethereum/assets/" + tokenContract + "/logo.png";
                 } else if (coin_symbol == "BNB") {
                     String tokenContract = asset.contract.toLowerCase();
                     tokenContract = Ethereum.toChecksumAddress(tokenContract);
                     asset.iconUrl = "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/" + tokenContract + "/logo.png";
                 }

                // to get nft info, we use another method getNftList(addr: String, contract: String) from NftExplorer class
                assetList.add(asset);

            }

            return assetList;

        } catch (Exception e){
            throw new RuntimeException("failed to fetch token balances: " + e, e);
        }
    }

}
