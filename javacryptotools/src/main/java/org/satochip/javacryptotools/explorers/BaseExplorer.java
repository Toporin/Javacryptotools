package org.satochip.javacryptotools.explorers;

import org.satochip.javacryptotools.coins.BaseCoin;

import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class BaseExplorer {

    // Shared library logger (see BaseCoin): the library never sets its level.
    // Explorers log debug info at FINE, and errors at WARNING when they handle them (e.g. returning -1).
    // Errors that are thrown are logged by the caller (BaseCoin).
    public static final Logger logger = BaseCoin.logger;
    private static final int MAX_LOGGED_CONTENT_LENGTH = 1000;

    public BaseCoin coin;
    public String coin_symbol;
    public Map<String, String> apikeys;

    public BaseExplorer(BaseCoin coin, Map<String, String> apikeys){
        this.coin = coin;
        this.coin_symbol= coin.coin_symbol;
        this.apikeys= apikeys;
    }

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public BaseExplorer(BaseCoin coin, Map<String, String> apikeys, Level level){
        this(coin, apikeys);
    }

    public BaseExplorer(String coin_symbol, Map<String, String> apikeys){
        this.coin_symbol= coin_symbol;
        this.apikeys= apikeys;
    }

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public BaseExplorer(String coin_symbol, Map<String, String> apikeys, Level level){
        this(coin_symbol, apikeys);
    }

    // Sets the level of the library logger, shared by all coins and explorers.
    // Optional: the level can also be set from the application on the logger, or on a parent logger.
    public void setLoggerLevel(Level level){
        logger.setLevel(level);
    }

    /*
     *   LOGGING HELPERS: messages are prefixed with the explorer name and coin symbol
     */

    protected void logFine(String msg){
        if (logger.isLoggable(Level.FINE)){
            logger.fine(getClass().getSimpleName() + " (" + coin_symbol + "): " + msg);
        }
    }

    protected void logWarning(String msg){
        logger.warning(getClass().getSimpleName() + " (" + coin_symbol + "): " + msg);
    }

    // log an API response at FINE level, truncated to keep logs readable
    protected void logResponse(String content){
        if (logger.isLoggable(Level.FINE)){
            String logged = (content != null && content.length() > MAX_LOGGED_CONTENT_LENGTH)
                    ? content.substring(0, MAX_LOGGED_CONTENT_LENGTH) + "... (" + content.length() + " chars)"
                    : content;
            logFine("response: " + logged);
        }
    }

}
