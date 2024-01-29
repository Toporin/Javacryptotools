package org.satochip.javacryptotools.explorers;

import java.util.logging.Level;
import org.json.JSONObject;
import java.util.Map;

// combines coingate & coingecko api depending on coin pair
// fiat/fiat uses coingate
// xcp uses coingecko

public class CoinCombined extends BaseExplorer implements PriceExplorer{

    private final boolean isTestnet;
    private PriceExplorer coingate;
    private PriceExplorer coingecko;

    public CoinCombined(String coin_symbol, Map<String, String> apikeys){
        this(coin_symbol, apikeys, Level.WARNING);
    }
    public CoinCombined(String coin_symbol, Map<String, String> apikeys, Level logLevel){
        super(coin_symbol, apikeys, logLevel);
        if (coin_symbol.equals("testnet")){
            this.isTestnet=true;
        } else {
            this.isTestnet=false;
        }
        coingate = new Coingate(coin_symbol, apikeys, logLevel);
        coingecko = new Coingecko(coin_symbol, apikeys, logLevel);
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

    public double get_exchange_rate_between(String other_coin){
        return get_exchange_rate_between(this.coin_symbol, other_coin);
    }

    public double get_exchange_rate_between(String coin, String other_coin){
        logger.info("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between START");
        //https://coingate.com/currencies
        try{
            if (coin.equals("XCP")){
                return coingecko.get_exchange_rate_between(coin, other_coin);
            } else {
                return coingate.get_exchange_rate_between(coin, other_coin);
            }

        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Coingate get_exchange_rate_between exception: " + e);
            return (double)-1;
        }
    }

    public double get_token_exchange_rate_between(String contract, String other_coin){
        return get_token_exchange_rate_between(this.coin_symbol, contract, other_coin);
    }

    public double get_token_exchange_rate_between(String coin, String contract, String other_coin){
        //unsupported
        return -1;
    }
}
