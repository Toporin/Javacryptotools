package org.satochip.javacryptotools.explorers;

import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Litecoin explorer based on mempool/esplora API (same API as Blockstream)
// Docs: https://litecoinspace.org/docs/api/rest

public class LitecoinSpace extends BaseExplorer implements Explorer{

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public LitecoinSpace(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }
    public LitecoinSpace(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
    }

    public String get_url(){
        if (this.coin_symbol.equals("LTC"))
            return "https://litecoinspace.org/api";
        else
            return "https://litecoinspace.org/testnet/api";
    }

    public String get_address_weburl(String addr){
        if (this.coin_symbol.equals("LTC"))
            return "https://litecoinspace.org/address/"+addr;
        else
            return "https://litecoinspace.org/testnet/address/"+addr;
    }

    public double get_balance(String addr){
        String url = get_url() + "/address/" + addr;
        logFine("request: " + url);

        HttpsClient client= new HttpsClient(url);
        String content= client.request();
        logResponse(content);

        // parse json
        JSONObject reader = new JSONObject(content);
        JSONObject chain_stats  = reader.getJSONObject("chain_stats");
        long funded_txo_sum= chain_stats.getLong("funded_txo_sum");
        long spent_txo_sum= chain_stats.getLong("spent_txo_sum");
        double  balance= (double) (funded_txo_sum - spent_txo_sum)/(100000000);
        return balance;
    }

    public List<Asset> get_asset_list(String address){
        // unsupported
        return new ArrayList<Asset>();
    }

}
