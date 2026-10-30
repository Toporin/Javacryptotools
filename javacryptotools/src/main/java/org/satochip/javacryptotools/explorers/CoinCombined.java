package org.satochip.javacryptotools.explorers;

import java.util.logging.Level;
import java.util.Map;

import org.satochip.javacryptotools.coins.BaseCoin;

// combines coingate & coingecko api depending on coin pair
// fiat/fiat uses coingate
// xcp uses coingecko

public class CoinCombined extends BaseExplorer implements PriceExplorer{

    private final PriceExplorer coingate;
    private final PriceExplorer coingecko;

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public CoinCombined(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }

    public CoinCombined(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
        coingate = new Coingate(coin, apikeys);
        coingecko = new Coingecko(coin, apikeys);
    }

    public String get_api_url(){
        return "https://api.coingate.com/api/v2/";
    }

    public String get_price_weburl(){
        return "https://coingate.com/exchange-rates/";
    }

    public String get_price_weburl(String coin){
        return "https://coingate.com/exchange-rates/";
    }

    public double get_exchange_rate_with(String other_coin){
        if (this.coin.is_testnet){
            return 0;
        }
        return get_exchange_rate_between(this.coin_symbol, other_coin);
    }

    public double get_exchange_rate_between(String coin, String other_coin){
        //https://coingate.com/currencies
        try{
            if (coin.equals("XCP")){
                return coingecko.get_exchange_rate_between(coin, other_coin);
            } else {
                return coingate.get_exchange_rate_between(coin, other_coin);
            }

        } catch (Exception e){
            logWarning("failed to get " + coin + "/" + other_coin + " rate: " + e);
            return -1;
        }
    }

}