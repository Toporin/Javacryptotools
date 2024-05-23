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
            Double balance = data.getJSONArray("items").getJSONObject(0).getDouble("balance");

            logger.info("JAVACRYPTOTOOLS: Covalent balance: " + balance);
            System.out.println("JAVACRYPTOTOOLS: Covalent balance: " + balance);
            return balance;
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
            logger.info("JAVACRYPTOTOOLS: Covalent get_asset_list  url: " + url);

            String apikey = (String) apikeys.get("API_KEY_COVALENT");

            // send request
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", get_basic_auth(apikey));
            HttpsClient client = new HttpsClient(url, headers);
            String content = client.request();
            logger.info("JAVACRYPTOTOOLS: Covalent get_asset_list content: " + content);

            // parse json
            List<Asset> assetList = new ArrayList<Asset>();
            JSONObject reader = new JSONObject(content);

            JSONArray tokens = reader.optJSONArray("tokens");
            if (tokens == null) {
                return Collections.emptyList();
            }

            for (int i = 0; i < tokens.length(); i++) {
                JSONObject token = tokens.getJSONObject(i);

                Asset asset = new Asset();
                asset.type = AssetType.Token;
                asset.balance = token.getString("rawBalance");
                System.out.println(asset.balance);

                JSONObject tokenInfo = token.getJSONObject("tokenInfo");
                asset.name = tokenInfo.getString("name");
                asset.contract = tokenInfo.getString("address");
                asset.symbol = tokenInfo.getString("symbol");
                asset.decimals = tokenInfo.optString("decimals", "0");
                System.out.println(asset.name);
                System.out.println(asset.contract);
                System.out.println(asset.symbol);
                System.out.println(asset.decimals);

                if (tokenInfo.has("price")) {
                    JSONObject price = tokenInfo.optJSONObject("price");
                    if (price != null) {
                        asset.rate = price.getDouble("rate");
                        asset.rateCurrency = price.getString("currency");
                        asset.rateAvailable = true;
                        System.out.println(asset.rate + " " + asset.rateCurrency);
                    } else {
                        asset.rateAvailable = false;
                        System.out.println("No price rate available!");
                    }
                }

                asset.explorerLink = get_token_weburl(asset.contract);

                if (coin_symbol == "MATIC") {
                    String tokenContract = asset.contract.toLowerCase();
                    logger.info("JAVACRYPTOTOOLS: Covalent get_asset_list tokenContract1: " + tokenContract);
                    tokenContract = Polygon.toChecksumAddress(tokenContract);
                    logger.info("JAVACRYPTOTOOLS: Covalent get_asset_list tokenContract2: " + tokenContract);
                    asset.iconUrl = "https://assets-cdn.trustwallet.com/blockchains/covalent/assets/" + tokenContract + "/logo.png";
                } else if (coin_symbol == "BNB") {
                    String tokenContract = asset.contract.toLowerCase();
                    tokenContract = Polygon.toChecksumAddress(tokenContract);
                    asset.iconUrl = "https://assets-cdn.trustwallet.com/blockchains/smartchain/assets/" + tokenContract + "/logo.png";
                }

                // to get nft info, we use another method getNftList(addr: String, contract: String) from NftExplorer class
                assetList.add(asset);
                logger.info("JAVACRYPTOTOOLS: Covalent get_asset_list asset: " + asset);

            }
            return assetList;

        } catch (Exception e) {
            logger.warning("JAVACRYPTOTOOLS: Covalent get_asset_list exception: " + e);
            System.out.println("JAVACRYPTOTOOLS: Covalent get_asset_list exception: " + e);
            throw new RuntimeException("Covalent: failed to fetch Ethereum token balance: " + e);
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

