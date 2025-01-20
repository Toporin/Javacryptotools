package org.satochip.javacryptotools.explorers;

import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;


public class Etherscan extends BaseExplorer implements Explorer{
    
    // headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'}
    
    public Etherscan(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    public Etherscan(String coin_symbol, Map<String, String> apikeys, Level level){
        super(coin_symbol, apikeys, level);
    }

    public String get_api_url(){
        if (this.coin_symbol.equals("ETH")){
            return "https://api.etherscan.io/api";
        } else {
            return "https://api-ropsten.etherscan.io/api";
        }
    }
    
    public String get_address_weburl(String addr){       
        String web_url;
        if (this.coin_symbol.equals("ETH")){
            web_url= "https://etherscan.io/address/"+addr ;
        } else {
            web_url= "https://ropsten.etherscan.io/address/"+addr;
        }
        return web_url;
    }

    public double get_balance(String addr) {
        
         /*
            https://api.etherscan.io/api
                ?module=account
                &action=balance
                &address=0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae
                &tag=latest
                &apikey=YourApiKeyToken
                
            {"status":"1","message":"OK","result":"353318436783144397866641"}
                
           */
        try{
            // add to apikeys
            String apikey= (String) apikeys.get("API_KEY_ETHERSCAN");
            String base_url = get_api_url();
            String url= base_url + "?module=account&action=balance&address=" + addr + "&tag=latest&apikey=" + apikey;
            logger.info("JAVACRYPTOTOOLS: Etherscan get_balance  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            //Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            
            //String user_agent= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";
            // Header header = new BasicHeader(HttpHeaders.USER_AGENT, user_agent);
            // List<Header> headers = Lists.newArrayList(header);
            // HttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
            // HttpUriRequest request = RequestBuilder.get().setUri(url).build();
            // String content= client.execute(request);
            // logger.info("JAVACRYPTOTOOLS: Request content: " + content);
            
            // String user_agent= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";
            // OkHttpClient client = new OkHttpClient();
            // Request request = new Request.Builder()
                        // .header("User-Agent", user_agent)
                        // .url(url)
                        // .build();
            // Response response = client.newCall(request).execute();
            // String content = response.body().string();
            logger.info("JAVACRYPTOTOOLS: Etherscan get_balance content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            int statusCode = reader.getInt("status");
            if (statusCode!=1){
                throw new RuntimeException("Etherscan: failed to fetch balance!");
            }
            long result = reader.getLong("result"); // in wei
            double balance= (double) (result)/(Math.pow(10, 18));
            logger.info("JAVACRYPTOTOOLS: Etherscan balance: " + balance);
            return balance;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Etherscan exception in balance: " + e);
            throw new RuntimeException("Etherscan: failed to fetch balance!");
        }
    }

    public List<Asset> get_asset_list(String address){
        return null;
    }
        
}
