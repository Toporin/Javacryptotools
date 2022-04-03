package org.satochip.javacryptotools.coins;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Constants {
  
    // Prevents instanciation of class
    private Constants() {}
    
    public static final int BTC= 0x80000000;
    public static final int LTC= 0x80000002;
    public static final int DOGE= 0x80000003;
    public static final int DASH= 0x80000005;
    public static final int XCP= 0x80000009;
    public static final int ETH= 0x8000003c;
    public static final int ETC= 0x8000003d;
    public static final int RBTC= 0x80000089;
    public static final int BCH= 0x80000091;
    public static final int BSC= 0x80000207;
    
    // map coin symbol => slip44
    public static final Map<String, Integer> MAP_SLIP44_BY_SYMBOL;
    static {
        Map<String, Integer> tmpmap= new HashMap<String, Integer>();
        tmpmap.put("BTC", BTC);
        tmpmap.put("LTC", LTC);
        tmpmap.put("DOGE", DOGE);
        tmpmap.put("DASH", DASH);
        tmpmap.put("XCP", XCP);
        tmpmap.put("ETH", ETH);
        tmpmap.put("ETC", ETC);
        tmpmap.put("RBTC", RBTC);
        tmpmap.put("BCH", BCH);
        tmpmap.put("BSC", BSC);
        MAP_SLIP44_BY_SYMBOL = Collections.unmodifiableMap(tmpmap);
    }
  
    // map slip44 => symbol name
    public static final Map<Integer, String> MAP_SYMBOL_BY_SLIP44;
    static {
        Map<Integer, String> tmpmap= new HashMap<Integer, String>();
        tmpmap.put(BTC, "BTC");
        tmpmap.put(LTC, "LTC");
        tmpmap.put(DOGE, "DOGE");
        tmpmap.put(DASH, "DASH");
        tmpmap.put(XCP, "XCP");
        tmpmap.put(ETH, "ETH");
        tmpmap.put(ETC, "ETC");
        tmpmap.put(RBTC, "RBTC");
        tmpmap.put(BCH, "BCH");
        tmpmap.put(BSC, "BSC");
        MAP_SYMBOL_BY_SLIP44 = Collections.unmodifiableMap(tmpmap);
    }
    
    public static final String[] SUPPORTS_TOKEN = {"XCP", "ETH", "ETC", "BSC"};
    public static final Set<String> SUPPORTS_TOKEN_SET = new HashSet<>(Arrays.asList(SUPPORTS_TOKEN));
    
}