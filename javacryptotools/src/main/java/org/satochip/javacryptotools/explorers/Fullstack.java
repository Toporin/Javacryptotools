package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

// Deprecated: the Fullstack.cash API now returns an html page instead of json.
// Bitcoin Cash uses the Fulcrum explorer instead. Kept for backward compatibility.
//Docs: https://api.fullstack.cash/docs/
// see also https://api.fullstack.cash/ 
// see also https://tapi.fullstack.cash/

public class Fullstack extends BaseExplorer implements Explorer{
        
    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Fullstack(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }
    public Fullstack(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
    }
    
    public String get_url(){
        if (this.coin_symbol.equals("BCH")){
            return "https://api.fullstack.cash/v5";
        } else {
            return "https://tapi.fullstack.cash/v5";
        }
    }
    
    public String get_address_weburl(String addr){        
        String web_url;
        if (this.coin_symbol.equals("BCH")){
            web_url= "https://www.blockchain.com/bch/address/" + addr;
        } else {
            web_url= "https://www.blockchain.com/bch-testnet/address/" + addr;
        }
        return web_url;
    }

    public double get_balance(String addr) {
        
        // https://api.fullstack.cash/v5/electrumx/balance/bitcoincash:qzrxy8wdjvd2qkjswuefc6exrjhy55mfpc3m0ap8t4
        // returns {"success":true,"balance":{"confirmed":0,"unconfirmed":0}}
        
        String base_url= get_url();
        String url= base_url + "/electrumx/balance/" + addr;
        logFine("request: " + url);
        
        HttpsClient client= new HttpsClient(url);
        String content= client.request();
        logResponse(content);
        
        // parse json
        JSONObject reader = new JSONObject(content);
        boolean isSuccess = reader.optBoolean("success", false);
        if (!isSuccess){
            throw new RuntimeException("Failed to fetch balance: "+ content);
        }
        JSONObject data = reader.getJSONObject("balance");
        long confirmed= data.optLong("confirmed", -1); // in satoshi // TODO: unconfirmed?
        double balance= (double) (confirmed)/(100000000);
        return balance;
    }

    public List<Asset> get_asset_list(String address){
        // unsupported
        return new ArrayList<Asset>();//return null;
    }

    public double get_token_balance(String address, String contract){
        // unsupported
        return (double)-1;
    }
    
    public HashMap<String, String> get_token_info(String contract){
        // unsupported
        return new HashMap<String, String>();
    }
    
}
