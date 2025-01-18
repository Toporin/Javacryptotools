package org.satochip.javacryptotools.explorers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.json.JSONObject;
import org.json.JSONArray;

import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.BaseCoin;

public class Blockscout extends BaseExplorer implements Explorer {

    public Blockscout(BaseCoin coin, Map<String, String> apikeys){
        this(coin, apikeys, Level.WARNING);
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout init coin_symbol: " + coin.coin_symbol);
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout init coin_symbol2: " + coin_symbol);
    }
    public Blockscout(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        super(coin, apikeys, logLevel);
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout init2 logLevel: " + logLevel);
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout init2 logger.getLevel(): " + logger.getLevel());
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout init2 coin_symbol: " + coin.coin_symbol);
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout init2 coin_symbol2: " + coin_symbol);
    }

    private String getWebURL() {
        Map<String, String> urls = new HashMap<>();
        urls.put("ETH", "https://eth.blockscout.com/");
        urls.put("ETHTEST", "https://eth-sepolia.blockscout.com/");
        urls.put("ETC", "https://etc.blockscout.com/");
        urls.put("ETCTEST", "https://etc-mordor.blockscout.com/");
        urls.put("BASE", "https://base.blockscout.com/");
        urls.put("BASETEST", "https://base-sepolia.blockscout.com/");
        urls.put("POL", "https://polygon.blockscout.com/");
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout getWebURL coin_symbol: " + coin_symbol);
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout getWebURL url: " + urls.getOrDefault(coin_symbol, "https://notfound.org/"));
        return urls.getOrDefault(coin_symbol, "https://notfound.org/");
    }

    private String getAPIURL() {
        return getWebURL() + "api/v2/";
    }

    @Override
    public String get_address_weburl(String addr) {
        return getWebURL() + "address/" + addr;
    }

    private String getTokenWebURL(String contract) {
        return getWebURL() + "token/" + contract;
    }

    private String getNFTWebURL(String contract, String tokenId) {
        return getWebURL() + "token/" + contract + "/instance/" + tokenId;
    }

    @Override
    public double get_balance(String addr) {
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout get_balance coin_symbol: " + coin_symbol);
        try {
            String url = getAPIURL() + "addresses/" + addr;
            HttpsClient client= new HttpsClient(url);
            String content= client.request();

            JSONObject json = new JSONObject(content);
            String coinBalanceWei = json.optString("coin_balance", "0");

            return convertWeiToEther(coinBalanceWei);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Blockscout: failed to fetch balance!");
        }
    }

    @Override
    public List<Asset> get_asset_list(String addr) {
        logger.info("JAVACRYPTOTOOLS: Blockscout get_asset_list start");
        logger.warning("JAVACRYPTOTOOLS: Blockscout start");
        System.out.println("PRINT JAVACRYPTOTOOLS: Blockscout start");
        System.out.println("DEBUG JAVACRYPTOTOOLS blockscout get_asset_list coin_symbol: " + coin_symbol);


        List<Asset> assetList = new ArrayList<>();
        String nextPageParams = "";

        try {
            do {
                String url = getAPIURL() + "addresses/" + addr + "/tokens" + nextPageParams;
                logger.warning("JAVACRYPTOTOOLS: Blockscout get_asset_list url: " + url);

                HttpsClient client= new HttpsClient(url);
                String content = client.request();
                logger.info("JAVACRYPTOTOOLS: Blockscout request content: " + content);
                logger.warning("JAVACRYPTOTOOLS: Blockscout request content: " + content);
                System.out.println("PRINT JAVACRYPTOTOOLS: Blockscout request content: " + content);

                JSONObject json = new JSONObject(content);
                JSONArray items = json.getJSONArray("items");
                System.out.println("PRINT JAVACRYPTOTOOLS: Blockscout get_asset_list items: " + items);


                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    System.out.println("PRINT JAVACRYPTOTOOLS: Blockscout get_asset_list item: " + item);

                    Asset asset = parseAssetFromJson(item, addr);
                    System.out.println("PRINT JAVACRYPTOTOOLS: Blockscout get_asset_list asset: " + asset);

                    if (asset != null) {
                        assetList.add(asset);
                    }
                }

                // pagination
                nextPageParams = parseNextPageParams(json.optJSONObject("next_page_params"));
                System.out.println("PRINT JAVACRYPTOTOOLS: Blockscout get_asset_list nextPageParams: " + nextPageParams);

            } while (!nextPageParams.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Blockscout: failed to fetch asset list!");
        }

        return assetList;
    }

    private Asset parseAssetFromJson(JSONObject item, String addr) {
        JSONObject token = item.getJSONObject("token");
        String addressContract = token.optString("address", "");
        String type = token.optString("type", "");

        if (addressContract.isEmpty() || type.isEmpty()) {
            return null;
        }

        Asset asset = new Asset();
        asset.name = token.optString("name", "");
        asset.contract = addressContract;
        asset.iconUrl = token.optString("icon_url", "");

        switch (type) {
            case "ERC-20":
                asset.type = AssetType.Token;
                break;
            case "ERC-721":
            case "ERC-1155":
                asset.type = AssetType.NFT;
                break;
            default:
                return null;
        }

        // Handle balance
        String value = item.optString("value", "0");
        if (value!=null && !value.isEmpty() && !value.equals("null")){
            String decimals = token.optString("decimals", "0");
            if (decimals!=null && !decimals.isEmpty() && !decimals.equals("null")){
                asset.balance = convertToDecimalString(value, decimals);
            }
        }

        // Handle exchange rate - warning: null is returned as "null" string!
        String exchangeRate = token.optString("exchange_rate", "");
        if (exchangeRate!=null && !exchangeRate.isEmpty() && !exchangeRate.equals("null")) {
            try{
                asset.rate = Double.parseDouble(exchangeRate);
                asset.rateCurrency = "USD";
                asset.rateAvailable = true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            asset.rateAvailable = false;
        }

        // Handle NFT specific data
        if (item.has("token_instance")) {
            JSONObject tokenInstance = item.optJSONObject("token_instance");
            if (tokenInstance != null){
                String tokenId = tokenInstance.optString("id", "");
                if (tokenId!=null && !tokenId.isEmpty() && !tokenId.equals("null")) {
                    asset.tokenid = tokenId;
                    asset.nftExplorerLink = getNFTWebURL(addressContract, tokenId);
                } else {
                    // can be a ERC-1155 token with multiple nfts
                    asset.nftExplorerLink = getTokenWebURL(addressContract) + "?tab=inventory&holder_address_hash=" + addr;
                }
                asset.nftImageLink = tokenInstance.optString("image_url", "");
            }

        }

        // Set explorer URLs
        asset.explorerLink = getTokenWebURL(addressContract);
        return asset;
    }

    private double convertWeiToEther(String weiAmount) {
        try {
            BigDecimal wei = new BigDecimal(weiAmount);
            BigDecimal ether = wei.divide(BigDecimal.valueOf(10).pow(18));
            return ether.doubleValue();
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String convertToDecimalString(String value, String decimals) {
        try {
            BigDecimal amount = new BigDecimal(value);
            int decimalsInt = Integer.parseInt(decimals);
            BigDecimal divisor = BigDecimal.valueOf(10).pow(decimalsInt);
            return amount.divide(divisor).toString();
        } catch (NumberFormatException e) {
            return null; //"NaN";
        }
    }

    private String parseNextPageParams(JSONObject params) {
        if (params == null) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            String value = params.optString(key, "");

            if (first) {
                queryString.append("?");
                first = false;
            } else {
                queryString.append("&");
            }

            try {
                queryString.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }

        return queryString.toString();
    }
}