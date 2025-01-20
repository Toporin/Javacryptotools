package org.satochip.javacryptotools.explorers;

import java.io.IOException;
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

public class Tokenscan extends BaseExplorer implements Explorer {

    public Tokenscan(BaseCoin coin, Map<String, String> apikeys){
        this(coin, apikeys, Level.WARNING);
    }
    public Tokenscan(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        super(coin, apikeys, logLevel);
    }

    // Utilities
    private String getURL() {
        Map<String, String> urls = new HashMap<>();
        urls.put("XCP", "https://tokenscan.io/");
        urls.put("XCPTEST", "https://testnet.tokenscan.io/");
        urls.put("XDP", "https://dogeparty.tokenscan.io/");
        urls.put("XDPTEST", "https://dogeparty-testnet.tokenscan.io/");

        return urls.getOrDefault(coin_symbol, "https://notfound.org/");
    }

    @Override
    public String get_address_weburl(String addr) {
        return getURL() + "address/" + addr;
    }

    private String getTokenWebURL(String contract) {
        return getURL() + "asset/" + contract;
    }

    @Override
    public double get_balance(String addr) {
        try {
            String url = getURL() + "api/address/" + addr;
            HttpsClient client= new HttpsClient(url);
            String content= client.request();

            JSONObject json = new JSONObject(content);

            String balance = json.optString("xcp_balance", "0");
            return Double.parseDouble(balance);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Tokenscan: failed to fetch balance!");
        }
    }

    @Override
    public List<Asset> get_asset_list(String addr) {
        List<Asset> assetList = new ArrayList<>();

        try {
            String url = getURL() + "api/balances/" + addr;
            HttpsClient client= new HttpsClient(url);
            String content= client.request();

            JSONObject json = new JSONObject(content);
            JSONArray items = json.getJSONArray("data");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                Asset asset = parseAssetFromJson(item, addr);
                if (asset != null) {
                    assetList.add(asset);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Tokenscan: failed to fetch asset list!");
        }

        return assetList;
    }

    private Asset parseAssetFromJson(JSONObject item, String addr) {
        String name = item.optString("asset", "");

        // Skip XCP native coin
        if ("XCP".equals(name)) {
            return null;
        }

        Asset asset = new Asset();
        asset.name = name;
        asset.contract = name;
        asset.symbol = item.optString("symbol", "");
        asset.description = item.optString("description", "");
        asset.balance = item.optString("quantity", "0");

        // Handle exchange rate
        JSONObject estimatedValue = item.optJSONObject("estimated_value");
        asset.rateAvailable = false;
        if (estimatedValue != null) {
            String usdValue = estimatedValue.optString("usd", "");
            if (usdValue!=null && !usdValue.isEmpty() && !usdValue.equals("null")) {
                asset.rate = Double.parseDouble(usdValue);
                asset.rateCurrency = "USD";
                asset.rateAvailable = true;
            }
        }

        // Handle NFT info from description
        String description = item.optString("description", "");
        if (description.startsWith("*")) {
            description = description.substring(1);
        }
        if (description.startsWith("imgur/")) {
            String blob = description.substring("imgur/".length());
            String[] blobSplit = blob.split(";");
            if (blobSplit.length > 0) {
                asset.nftImageLink = "https://i.imgur.com/" + blobSplit[0];
            }
        } else if (description.startsWith("https://") || description.startsWith("http://")) {
            if (description.endsWith(".png") ||
                    description.endsWith(".jpg") ||
                    description.endsWith(".jpeg") ||
                    description.endsWith(".gif")) {
                asset.nftImageLink = description;
            } else if (description.endsWith(".json")) {
                try {
                    HttpsClient client= new HttpsClient(description);
                    String content= client.request();

                    JSONObject json = new JSONObject(content);
                    String imageLarge = json.optString("image_large", "");
                    if (imageLarge!=null && !imageLarge.isEmpty() && !imageLarge.equals("null")) {
                        asset.nftImageLink = imageLarge;
                    } else {
                        String image = json.optString("image", "");
                        if (image!=null && !image.isEmpty() && !image.equals("null")) {
                            asset.nftImageLink = image;
                        }
                    }

                } catch (Exception e) {
                    System.out.println("Error fetching NFT info from JSON: " + e.getMessage());
                }
            } else {
                asset.description = ((asset.description != null ? asset.description : "") + description);
            }
        }

        // Set type and explorer URLs
        if (asset.nftImageLink != null && !asset.nftImageLink.isEmpty() && !asset.nftImageLink.equals("null")) {
            asset.type = AssetType.NFT;
            asset.nftExplorerLink = getTokenWebURL(asset.name);
        } else {
            asset.type = AssetType.Token;
            asset.explorerLink = getTokenWebURL(asset.name);
        }

        return asset;
    }
}
