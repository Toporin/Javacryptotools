package org.satochip.javacryptotools.explorers;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CovalentNFT extends BaseExplorer implements NftExplorer {

    // Get Chain from coin symbol
    private String get_chain(String coinSymbol) {
        if ("MATIC".equals(coinSymbol)) {
            return "matic-mainnet";
        }
        return "matic-mumbai";
    }

    // Get Basic Auth Header
    private String get_basic_auth(String apiKey) {
        String username = apiKey;
        String password = "";
        String loginString = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(loginString.getBytes());
    }

    public CovalentNFT(String coin_symbol, Map<String, String> apikeys) {
        this(coin_symbol, apikeys, Level.WARNING);
    }

    public CovalentNFT(String coin_symbol, Map<String, String> apikeys, Level logLevel) {
        super(coin_symbol, apikeys, logLevel);
    }

    public String get_api_url() {
        return "https://api.covalenthq.com/v1/";
    }

    public String get_nft_owner_weburl(String addr) {
        return "https://rarible.com/user/" + addr + "/owned";
    }

    public String get_nft_weburl(String contract, String tokenID) {
        return "https://rarible.com/token/polygon/" + contract + ":" + tokenID;
    }

    public List<Asset> get_nft_list(String address) {
        logger.info("JAVACRYPTOTOOLS: Covalent get_nft_list START");
        List<Asset> assetList = new ArrayList<Asset>();
        if (address == null) {
            return assetList;
        }

        try {
            String apikey = (String) apikeys.get("API_KEY_COVALENT");
            String base_url = get_api_url();
            String url = base_url
                    + get_chain(this.coin_symbol)
                    + "/address/"
                    + address
                    + "/balances_nft/";

            // send request
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "application/json");
            headers.put("Authorization", get_basic_auth(apikey));
            HttpsClient client = new HttpsClient(url, headers);
            String content = client.request();

            // parse json
            JSONObject reader = new JSONObject(content);
            JSONObject data = reader.getJSONObject("data");
            JSONArray items = data.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject token = items.getJSONObject(i);
                assetList.addAll(get_all_nfts_data(token, address));
            }

            return assetList;
        } catch (Exception e) {
            logger.warning("JAVACRYPTOTOOLS: Covalent get_nft_list exception: " + e);
            throw new RuntimeException("JAVACRYPTOTOOLS: Covalent get_nft_list exception: " + e);
        }
    }

    private List<Asset> get_all_nfts_data(JSONObject token, String address) {
        List<Asset> assetList = new ArrayList<Asset>();
        JSONArray nftData = token.getJSONArray("nft_data");

        for (int i = 0; i < nftData.length(); i++) {
            JSONObject nftItem = nftData.getJSONObject(i);

            if (!nftItem.isNull("external_data")) {
                JSONObject externalData = nftItem.getJSONObject("external_data");
                String nftName = externalData.getString("name");
                String nftDescription = externalData.getString("description");
                String nftImageLink = externalData.getString("image");
                Asset asset = new Asset();
                asset.address = address;
                asset.contract = token.getString("contract_address");
                try {
                    long tokenidLong = token.getLong("tokenId"); // throw if fail
                    asset.tokenid = String.valueOf(tokenidLong);
                } catch (Exception e) {
                    asset.tokenid = "NaN"; // todo?
                    logger.warning("JAVACRYPTOTOOLS: Covalent get_nft_list parse tokenid exception: " + e);
                }
                logger.info("JAVACRYPTOTOOLS: Covalent get_nft_list : asset.tokenid" + asset.tokenid);
                asset.balance = "1";
                asset.decimals = "0";
                asset.nftName = nftName;
                asset.nftDescription = nftDescription;
                asset.nftImageLink = nftImageLink;
                asset.nftImageSmallLink = asset.nftImageLink;
                asset.nftExplorerLink = get_nft_weburl(asset.contract, asset.tokenid);
                asset.type = AssetType.NFT;

                assetList.add(asset);
                logger.info("JAVACRYPTOTOOLS: Covalent get_nft_list nft: " + asset);
            }
        }

        return assetList;
    }

    // NOT USED
    public HashMap<String, String> get_nft_info(String contract, String tokenID) {
        HashMap<String, String> nftInfoMap = new HashMap<String, String>();
        return nftInfoMap;
    }

    // DEPRECATED, NOT USED
    public JSONObject get_nft_info_json(String contract, String tokenID) {
        JSONObject nftInfo = new JSONObject();
        return nftInfo;
    }
}