package org.satochip.javacryptotools.explorers;

import java.util.Map;
import java.util.HashMap;

public abstract class BaseExplorer {
    
    public String coin_symbol;
    public Map<String, String> apikeys;
    
    public BaseExplorer(String coin_symbol, Map<String, String> apikeys){
        this.coin_symbol= coin_symbol;
        this.apikeys= apikeys;
    }
    
}
