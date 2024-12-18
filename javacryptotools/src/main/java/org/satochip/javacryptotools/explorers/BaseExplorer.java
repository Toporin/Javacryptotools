package org.satochip.javacryptotools.explorers;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class BaseExplorer {
    
    public static final Logger logger = Logger.getLogger("org.satochip.javacryptotools");
    public String coin_symbol;
    public Map<String, String> apikeys;

    public BaseExplorer(String coin_symbol, Map<String, String> apikeys){
        this.coin_symbol= coin_symbol;
        this.apikeys= apikeys;
        logger.setLevel(Level.WARNING);
    }

    public BaseExplorer(String coin_symbol, Map<String, String> apikeys, Level level){
        this.coin_symbol= coin_symbol;
        this.apikeys= apikeys;
        logger.setLevel(level);
    }

    public void setLoggerLevel(Level level){
        logger.setLevel(level);
    }

}
