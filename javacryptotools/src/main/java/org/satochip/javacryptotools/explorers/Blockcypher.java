package org.satochip.javacryptotools.explorers;

import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

//Docs: https://www.blockcypher.com/dev/bitcoin

public class Blockcypher extends BaseExplorer implements Explorer{

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Blockcypher(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }
    public Blockcypher(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
    }

    public String get_base_url(){
        if (this.coin_symbol.equals("LTC")) {
            return "https://api.blockcypher.com/v1/ltc/main/";
        } else if (this.coin_symbol.equals("DOGE")) {
            return "https://api.blockcypher.com/v1/doge/main/";
        } else {
            return "";
        }
    }

    public String get_address_weburl(String addr){
        if (this.coin_symbol.equals("LTC")) {
            return "https://live.blockcypher.com/ltc/address/"+addr;
        } else if (this.coin_symbol.equals("DOGE")) {
            return "https://live.blockcypher.com/doge/address/"+addr;
        } else {
            return "https://live.blockcypher.com/";
        }
    }

    public double get_balance(String addr) {
        // https://www.blockcypher.com/dev/bitcoin/#address-api
        // https://api.blockcypher.com/v1/btc/main/addrs/1DEP8i3QJCsomS4BSMY2RpU1upv62aGvhD/balance
        String url= get_base_url() + "addrs/" + addr + "/balance";
        logFine("request: " + url);

        HttpsClient client= new HttpsClient(url);
        String content= client.request();
        logResponse(content);

        // parse json
        JSONObject reader = new JSONObject(content);
        double balanceSats= reader.getDouble("final_balance"); // TODO: unconfirmed_balance?
        double balance = balanceSats/100000000.0;
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
