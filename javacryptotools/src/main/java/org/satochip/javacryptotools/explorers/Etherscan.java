package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// import okhttp3.MediaType;
// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.RequestBody;
// import okhttp3.Response; 

import java.util.Map;
import java.util.HashMap;


public class Etherscan extends Explorer{
    
    // headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36'}
    
    public Etherscan(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
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
            System.out.println("Etherscan explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            //Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            
            //String user_agent= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";
            // Header header = new BasicHeader(HttpHeaders.USER_AGENT, user_agent);
            // List<Header> headers = Lists.newArrayList(header);
            // HttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
            // HttpUriRequest request = RequestBuilder.get().setUri(url).build();
            // String content= client.execute(request);
            // System.out.println("Request content: " + content);
            
            // String user_agent= "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36";
            // OkHttpClient client = new OkHttpClient();
            // Request request = new Request.Builder()
                        // .header("User-Agent", user_agent)
                        // .url(url)
                        // .build();
            // Response response = client.newCall(request).execute();
            // String content = response.body().string();
            System.out.println("Request content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            int statusCode = reader.getInt("status");
            if (statusCode!=1){
                throw new RuntimeException("Etherscan: failed to fetch balance!");
            }
            long result = reader.getLong("result"); // in wei
            double balance= (double) (result)/(Math.pow(10, 18));
            // System.out.println("balance: " + balance);
            return balance;
        } catch (Exception e){
            System.out.println("Exception in balance: " + e);
            throw new RuntimeException("Etherscan: failed to fetch balance!");
        }
    }
    
    public long get_token_balance(String address, String contract){

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
            System.out.println("Etherscan explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            System.out.println("Request content: " + content);
            
            // parse json
            JSONObject reader = new JSONObject(content);
            int statusCode = reader.getInt("status");
            if (statusCode!=1){
                throw new RuntimeException("Etherscan: Failed to fetch balance!");
            }
            long result = reader.getLong("result"); //
            // System.out.println("balance: " + result);
            //double balance= (double) (result)/(Math.pow(10, 18)); // most ERC20 use 18 decimals but etherscan does not offer reliable way to find out...
            return result;
        } catch (Exception e){
            System.out.println("Exception in balance: " + e);
            throw new RuntimeException("Etherscan: failed to fetch Ethereum token balance!");
        }    
    }
    
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
            System.out.println("Ethplorer explorer  url: " + url);
            
            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            System.out.println("Request content: " + content);
            
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
            // System.out.println("name: " + name);
            // System.out.println("symbol: " + symbol);
            // System.out.println("decimals: " + decimals);
            return tokenInfo;
        } catch (Exception e){
            System.out.println("Exception in get_token_info: " + e);
            return tokenInfo;
        }  
    }
        
}
