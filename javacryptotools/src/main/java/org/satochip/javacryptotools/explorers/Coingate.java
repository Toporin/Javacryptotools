package org.satochip.javacryptotools.explorers;

import org.json.JSONObject;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.util.Map;
import java.util.logging.Level;

// https://developer.coingate.com/reference/get-rate
// Note: Coingate does not support Counterparty (XCP)
public class Coingate extends BaseExplorer implements PriceExplorer{

    public Coingate(BaseCoin coin, Map<String, String> apikeys){
        this(coin, apikeys, Level.WARNING);
    }
    public Coingate(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        super(coin, apikeys, logLevel);
    }

    public String get_api_url(){
        return "https://api.coingate.com/api/v2/";
    }

    public String get_price_weburl(){
        return "https://coingate.com/exchange-rates/";
    }

    public String get_price_weburl(String coin){
        String web_url="https://coingate.com/exchange-rates/";
        return web_url;
    }

    public double get_exchange_rate_with(String other_coin){
        if (this.coin.is_testnet){
            return 0;
        }
        return get_exchange_rate_between(this.coin_symbol, other_coin);
    }

    public double get_exchange_rate_between(String coin, String other_coin){
        logger.info("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between START");
        //https://coingate.com/currencies
        try{
            // Coingate uses ISO Symbol for currencies
            String base_url = get_api_url();
            String url= base_url + "rates/merchant/" + coin + "/" + other_coin;
            logger.info("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between url: " + url);

            HttpsClient client= new HttpsClient(url);
            String content= client.request();
            logger.info("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between content: " + content);

            // parse json
            double rate = Double.parseDouble(content);
            logger.info("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between rate: " + rate);
            return rate;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between exception: " + e);
            return (double)-1;
        }
    }

}
