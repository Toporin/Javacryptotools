package org.satochip.javacryptotools.explorers;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.Polygon;


public class Covalent extends BaseExplorer implements Explorer {

    // Logger
    private static final Logger logger = Logger.getLogger(Covalent.class.getName());

    // Constructor
    public Covalent(String coinSymbol, Map<String, String> apiKeys) {
        super(coinSymbol, apiKeys);
    }

    public Covalent(String coin_symbol, Map<String, String> apikeys, Level level) {
        super(coin_symbol, apikeys, level);
    }

    // Get Basic Auth Header
    private String get_basic_auth(String apiKey) {
        String username = apiKey;
        String password = "";
        String loginString = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(loginString.getBytes());
    }

    // Convert balance to double
    private Double convert_balance_to_double(String balance, int decimals) {
        try {
            BigDecimal balanceDecimal = new BigDecimal(balance);
            BigDecimal divisor = BigDecimal.ONE.scaleByPowerOfTen(decimals);
            BigDecimal balanceDouble = balanceDecimal.divide(divisor);
            return balanceDouble.doubleValue();
        } catch (NumberFormatException e) {
            logger.warning("Error: Balance is not a valid number.");
            return null;
        }
    }

    // Get Chain from coin symbol
    private String get_chain(String coinSymbol) {
        if ("MATIC".equals(coinSymbol)) {
            return "matic-mainnet";
        }
        return "matic-mumbai";
    }

    public String get_api_url() {
        return "https://api.covalenthq.com/v1/";
    }

    public String get_address_weburl(String addr) {
        return "https://polygonscan.com/address/" + addr;
    }

    public String get_token_weburl(String contract) {
        return "https://polygonscan.com/token/" + contract;
    }

    public double get_balance(String addr) {
        try {
            String base_url = get_api_url();
            String url = base_url
                    + get_chain(this.coin_symbol)
                    + "/address/"
                    + addr
                    + "/balances_native/";

            logger.info("JAVACRYPTOTOOLS: Covalent get_balance  url: " + url);

            String apikey = (String) apikeys.get("API_KEY_COVALENT");

            // send request
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", get_basic_auth(apikey));

            HttpsClient client = new HttpsClient(url, headers);
            String content = client.request();

            JSONObject reader = new JSONObject(content);
            JSONObject data = reader.getJSONObject("data");
            int decimals = data.getJSONArray("items").getJSONObject(0).getInt("contract_decimals");
            String balance = data.getJSONArray("items").getJSONObject(0).getString("balance");
            Double balanceResult = convert_balance_to_double(balance, decimals);

            return balanceResult;
        } catch (Exception e) {
            logger.warning("JAVACRYPTOTOOLS: Covalent exception in balance: " + e);
            System.out.println("JAVACRYPTOTOOLS: Covalent exception in balance: " + e);
            throw new RuntimeException("Covalent: failed to fetch balance!" + e);
        }
    }

    public List<Asset> get_asset_list(String addr) {
        try {
            String base_url = get_api_url();
            String url = base_url
                    + get_chain(this.coin_symbol)
                    + "/address/"
                    + addr
                    + "/balances_v2/";

            String apikey = (String) apikeys.get("API_KEY_COVALENT");

            // send request
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", get_basic_auth(apikey));
            HttpsClient client = new HttpsClient(url, headers);
            String content = client.request();

            // parse json
            List<Asset> assetList = new ArrayList<Asset>();
            JSONObject reader = new JSONObject(content);

            JSONObject data = reader.getJSONObject("data");
            JSONArray items = data.getJSONArray("items");
            if (items == null) {
                return Collections.emptyList();
            }

            for (int i = 0; i < items.length(); i++) {
                JSONObject itemInfo = items.getJSONObject(i);

                // Do not add Matic token to avoid duplicate
                if (itemInfo.getString("contract_name").equals("Matic Token")) {
                    continue;
                }
                // for unknown reason, explorer sometimes returns token with 0 balance
                if (itemInfo.getString("balance").equals("0")) {
                    continue;
                }   

                Asset asset = new Asset();
                asset.type = AssetType.Token;
                System.out.println(asset.balance);

                asset.name = itemInfo.getString("contract_name");
                asset.contract = itemInfo.getString("contract_address");
                asset.symbol = itemInfo.getString("contract_ticker_symbol");
                asset.decimals = itemInfo.optString("contract_decimals", "0");
                asset.balance = itemInfo.getString("balance");
                if (!itemInfo.isNull("quote_rate")) {
                    asset.rate = itemInfo.getDouble("quote_rate");
                    asset.rateCurrency = data.getString("quote_currency");
                    asset.rateAvailable = true;
                }

                System.out.println(asset.name);
                System.out.println(asset.contract);
                System.out.println(asset.symbol);
                System.out.println(asset.decimals);

                asset.explorerLink = get_token_weburl(asset.contract);
                asset.iconUrl = itemInfo.getJSONObject("logo_urls").getString("token_logo_url");

                // to get nft info, we use another method getNftList(addr: String, contract: String) from NftExplorer class
                assetList.add(asset);
                logger.warning("JAVACRYPTOTOOLS: Covalent get_asset_list asset: " + asset);
            }
            return assetList;

        } catch (Exception e) {
            logger.warning("JAVACRYPTOTOOLS: Covalent get_asset_list exception: " + e);
            System.out.println("JAVACRYPTOTOOLS: Covalent get_asset_list exception: " + e);
            throw new RuntimeException("Covalent: failed to fetch Matic token balance: " + e);
        }
    }

    public double get_token_balance(String addr, String contract) {
        return 0.0;
    }

    public HashMap<String, String> get_token_info(String contract) {
        HashMap<String, String> tokenInfo = new HashMap<String, String>();
        return tokenInfo;
    }
}

