package org.satochip.javacryptotools.explorers;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.json.JSONObject;
import org.json.JSONArray;

import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.BaseCoin;

public class Blockscout extends BaseExplorer implements Explorer {

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Blockscout(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }
    public Blockscout(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
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
        String url = getAPIURL() + "addresses/" + addr;

        HttpsClient client= new HttpsClient(url);
        String content = client.request();
        if (isNotFound(client, content)) {
            return 0;
        }

        JSONObject json = new JSONObject(content);
        String coinBalanceWei = json.optString("coin_balance", "0");

        return convertWeiToEther(coinBalanceWei);

    }

    @Override
    public List<Asset> get_asset_list(String addr) {
        List<Asset> assetList = get_token_list(addr);
        List<Asset> nftList = get_nft_list(addr);
        assetList.addAll(nftList);

        return assetList;
    }

    public List<Asset> get_token_list(String addr) {
        List<Asset> assetList = new ArrayList<>();
        String nextPageParams = "";

        do {
            String url = getAPIURL() + "addresses/" + addr + "/tokens?type=ERC-20" + nextPageParams;
            logFine("request: " + url);

            HttpsClient client= new HttpsClient(url);
            String content = client.request();
            if (isNotFound(client, content)) {
                return assetList;
            }

            JSONObject json = new JSONObject(content);
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                Asset asset = parseAssetFromJson(item, addr);

                if (asset != null) {
                    assetList.add(asset);
                }
            }

            // pagination
            nextPageParams = parseNextPageParams(json.optJSONObject("next_page_params"), false);
            logFine("next page params: " + nextPageParams);

        } while (!nextPageParams.isEmpty());


        return assetList;
    }

    public List<Asset> get_nft_list(String addr) {
        List<Asset> assetList = new ArrayList<>();
        String nextPageParams = "";

        do {
            String url = getAPIURL() + "addresses/" + addr + "/nft" + nextPageParams;
            logFine("request: " + url);

            HttpsClient client= new HttpsClient(url);
            String content = client.request();
            if (isNotFound(client, content)) {
                return assetList;
            }

            JSONObject json = new JSONObject(content);
            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                Asset asset = parseAssetFromJson(item, addr);

                if (asset != null) {
                    assetList.add(asset);
                }
            }

            // pagination
            nextPageParams = parseNextPageParams(json.optJSONObject("next_page_params"), true);
            logFine("next page params: " + nextPageParams);

        } while (!nextPageParams.isEmpty());


        return assetList;
    }

    public Asset parseAssetFromJson(JSONObject item, String addr) {
        JSONObject token = item.getJSONObject("token");

        if (token==null) {
            return null;
        }

        Asset asset = new Asset();
        String name = token.optString("name");
        if (name!=null && !name.equals("null")){
            asset.name = name;
        }
        // recent Blockscout API versions use 'address_hash' instead of 'address'
        String contract = token.optString("address_hash", token.optString("address"));
        if (contract!=null && !contract.equals("null")){
            asset.contract = contract;
        }
        String symbol = token.optString("symbol");
        if (symbol!=null && !symbol.equals("null")){
            asset.symbol = symbol;
        }
        // warning: optString() returns "null" for JSON null values
        asset.iconUrl = token.isNull("icon_url") ? "" : token.optString("icon_url");
        asset.description = token.isNull("description") ? "" : token.optString("description");

        String type = token.optString("type", "");
        switch (type) {
            case "ERC-20":
                asset.type = AssetType.Token;
                break;
            case "ERC-721":
            case "ERC-1155":
                asset.type = AssetType.NFT;
                break;
            default:
                asset.type = AssetType.Token;
        }

        // Handle balance
        String value = item.optString("value", "0");
        if (value!=null && !value.isEmpty() && !value.equals("null")){
            String decimals = token.optString("decimals", "0");
            if (decimals!=null && !decimals.isEmpty() && !decimals.equals("null")){
                asset.balance = convertToDecimalString(value, decimals);
            } else {
                asset.balance = convertToDecimalString(value, "0");
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
                logWarning("invalid exchange rate for token " + contract + ": " + exchangeRate);
            }
        } else {
            asset.rateAvailable = false;
        }

        // Handle NFT specific data
        String image_url = item.optString("image_url");
        if (image_url!=null && !image_url.isEmpty() && !image_url.equals("null")) {
            asset.nftImageLink = image_url;
        }
        String tokenId = item.optString("id", "");
        if (tokenId!=null && !tokenId.isEmpty() && !tokenId.equals("null")) {
            asset.tokenid = tokenId;
            asset.nftExplorerLink = getNFTWebURL(contract, tokenId);
        } else {
            // can be a ERC-1155 token with multiple nfts
            asset.nftExplorerLink = getTokenWebURL(contract) + "?tab=inventory&holder_address_hash=" + addr;
        }
        JSONObject metadata = item.optJSONObject("metadata");
        if (metadata != null){
            String description = metadata.optString("description");
            if (description!=null && !description.isEmpty() && !description.equals("null")) {
                asset.nftDescription = description;
            }
            String nftName = metadata.optString("name");
            if (nftName!=null && !nftName.isEmpty() && !nftName.equals("null")) {
                asset.nftName = nftName;
            }
        }

        // Set explorer URLs
        asset.explorerLink = getTokenWebURL(contract);
        return asset;
    }

    // An empty response means the request failed. Only a 404 error (address not found in db,
    // which throws java.io.FileNotFoundException) is a valid answer, other errors are reported.
    private boolean isNotFound(HttpsClient client, String content) {
        if (content != null && !content.isEmpty()) {
            return false;
        }
        if (client.responseCode == 404) {
            return true;
        }
        throw new RuntimeException("Blockscout: request failed with HTTP code " + client.responseCode);
    }

    private double convertWeiToEther(String weiAmount) {
        try {
            BigDecimal wei = new BigDecimal(weiAmount);
            BigDecimal ether = wei.divide(BigDecimal.valueOf(10).pow(18));
            return ether.doubleValue();
        } catch (NumberFormatException e) {
            logWarning("invalid balance: " + weiAmount);
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
            logWarning("invalid token balance: " + value + " (decimals: " + decimals + ")");
            return null;
        }
    }

    private String parseNextPageParams(JSONObject params, Boolean withHeader) {
        if (params == null) {
            return "";
        }

        StringBuilder queryString = new StringBuilder();
        boolean first = true;

        // use keys() instead of keySet(), which is missing from the org.json version built into Android
        Iterator<String> keys = params.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = params.optString(key, "");

            if (first && withHeader) {
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
                logWarning("failed to encode page params: " + e);
                return "";
            }
        }

        return queryString.toString();
    }
}