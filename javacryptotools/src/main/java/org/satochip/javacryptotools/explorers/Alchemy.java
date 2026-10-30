package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Alchemy APIs for EVM chains. Requires an API key in apikeys under 'API_KEY_ALCHEMY'.
// - native balance: JSON-RPC eth_getBalance
//   https://www.alchemy.com/docs/reference/polygon-pos-api-quickstart
// - ERC-20 tokens (with metadata & prices): Portfolio API 'Tokens By Wallet'
//   https://www.alchemy.com/docs/data/portfolio-apis/portfolio-api-endpoints/portfolio-api-endpoints/get-tokens-by-address
// - NFTs: NFT API v3 'getNFTsForOwner'
//   https://www.alchemy.com/docs/reference/nft-api-endpoints/nft-api-endpoints/nft-ownership-endpoints/get-nf-ts-for-owner-v-3
//
// Alchemy has no web explorer: web links point to the chain's Etherscan-family explorer.
// Spam NFTs (flagged by Alchemy) are not listed. The number of result pages fetched is capped,
// since addresses can hold thousands of (mostly spam) assets.

public class Alchemy extends BaseExplorer implements Explorer{

    public static final String APIKEY_NAME = "API_KEY_ALCHEMY";
    private static final int MAX_PAGES = 5;
    private static final int NFT_PAGE_SIZE = 100;

    // native token predeploy contract on Polygon, also listed as an ERC-20 token by Alchemy
    private static final String POLYGON_NATIVE_TOKEN_CONTRACT = "0x0000000000000000000000000000000000001010";

    private static final Map<String, String> NETWORKS; // coin symbol => Alchemy network id
    private static final Map<String, String> WEB_URLS;
    static {
        Map<String, String> networks = new HashMap<>();
        networks.put("POL", "polygon-mainnet");
        networks.put("POLTEST", "polygon-amoy");
        NETWORKS = Collections.unmodifiableMap(networks);

        Map<String, String> urls = new HashMap<>();
        urls.put("POL", "https://polygonscan.com/");
        urls.put("POLTEST", "https://amoy.polygonscan.com/");
        WEB_URLS = Collections.unmodifiableMap(urls);
    }

    public Alchemy(BaseCoin coin, Map<String, String> apikeys){
        this(coin, apikeys, Level.WARNING);
    }
    public Alchemy(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        super(coin, apikeys, logLevel);
    }

    public String get_network(){
        String network = NETWORKS.get(coin_symbol);
        if (network == null){
            throw new RuntimeException("Alchemy: unsupported coin " + coin_symbol);
        }
        return network;
    }

    private String getApiKey(){
        String apikey = (apikeys != null) ? apikeys.get(APIKEY_NAME) : null;
        if (apikey == null || apikey.isEmpty()){
            throw new RuntimeException("Alchemy: missing API key " + APIKEY_NAME);
        }
        return apikey;
    }

    private String getWebURL(){
        return WEB_URLS.getOrDefault(coin_symbol, "https://notfound.org/");
    }

    public String get_address_weburl(String addr){
        return getWebURL() + "address/" + addr;
    }

    private String getTokenWebURL(String contract){
        return getWebURL() + "token/" + contract;
    }

    private String getNFTWebURL(String contract, String tokenId){
        return getWebURL() + "nft/" + contract + "/" + tokenId;
    }

    // urls contain the api key: they must not be logged
    private String post(String url, JSONObject body){
        HttpsClient client = new HttpsClient(url);
        String content = client.post(body.toString());
        if (content == null || content.isEmpty()){
            throw new RuntimeException("Alchemy: request failed with HTTP code " + client.responseCode);
        }
        logger.info("JAVACRYPTOTOOLS: Alchemy response: " + content);
        return content;
    }

    private String get(String url){
        HttpsClient client = new HttpsClient(url);
        String content = client.request();
        if (content == null || content.isEmpty()){
            throw new RuntimeException("Alchemy: request failed with HTTP code " + client.responseCode);
        }
        logger.info("JAVACRYPTOTOOLS: Alchemy response: " + content);
        return content;
    }

    /*
     *   API
     */

    public double get_balance(String addr){
        try {
            String url = "https://" + get_network() + ".g.alchemy.com/v2/" + getApiKey();
            JSONObject body = new JSONObject();
            body.put("jsonrpc", "2.0");
            body.put("id", 1);
            body.put("method", "eth_getBalance");
            body.put("params", new JSONArray().put(addr).put("latest"));
            return parse_balance(post(url, body));
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Alchemy exception in balance: " + e);
            throw new RuntimeException("Alchemy: failed to fetch balance!");
        }
    }

    public List<Asset> get_asset_list(String addr){
        try {
            List<Asset> assets = get_token_list(addr);
            assets.addAll(get_nft_list(addr));
            return assets;
        } catch (Exception e){
            logger.warning("JAVACRYPTOTOOLS: Alchemy exception in asset list: " + e);
            throw new RuntimeException("Alchemy: failed to fetch asset list!");
        }
    }

    public List<Asset> get_token_list(String addr){
        String url = "https://api.g.alchemy.com/data/v1/" + getApiKey() + "/assets/tokens/by-address";
        List<Asset> assets = new ArrayList<>();
        String pageKey = null;
        int page = 0;
        do {
            JSONObject body = new JSONObject();
            body.put("addresses", new JSONArray().put(new JSONObject()
                    .put("address", addr)
                    .put("networks", new JSONArray().put(get_network()))));
            body.put("withMetadata", true);
            body.put("withPrices", true);
            body.put("includeNativeTokens", false);
            body.put("includeErc20Tokens", true);
            if (pageKey != null){
                body.put("pageKey", pageKey);
            }
            JSONObject json = new JSONObject(post(url, body));
            assets.addAll(parse_token_list(json, addr));
            pageKey = json.getJSONObject("data").optString("pageKey", null);
            page++;
        } while (pageKey != null && !pageKey.isEmpty() && page < MAX_PAGES);
        return assets;
    }

    public List<Asset> get_nft_list(String addr){
        String baseUrl = "https://" + get_network() + ".g.alchemy.com/nft/v3/" + getApiKey()
                + "/getNFTsForOwner?owner=" + addr + "&withMetadata=true&pageSize=" + NFT_PAGE_SIZE;
        List<Asset> assets = new ArrayList<>();
        String pageKey = null;
        int page = 0;
        do {
            String url = (pageKey == null) ? baseUrl : baseUrl + "&pageKey=" + urlEncode(pageKey);
            JSONObject json = new JSONObject(get(url));
            assets.addAll(parse_nft_list(json, addr));
            pageKey = json.isNull("pageKey") ? null : json.optString("pageKey", null);
            page++;
        } while (pageKey != null && !pageKey.isEmpty() && page < MAX_PAGES);
        return assets;
    }

    /*
     *   PARSING
     */

    public double parse_balance(String content){
        JSONObject json = new JSONObject(content);
        if (json.has("error")){
            throw new RuntimeException("Alchemy error: " + json.get("error"));
        }
        BigInteger wei = parseHexQuantity(json.getString("result"));
        return new BigDecimal(wei).movePointLeft(18).doubleValue();
    }

    // parse a Portfolio API 'tokens by address' response
    public List<Asset> parse_token_list(JSONObject json, String addr){
        JSONObject data = json.getJSONObject("data");
        JSONArray tokens = data.getJSONArray("tokens");
        JSONObject error = json.optJSONObject("error");
        if (error != null && tokens.length() == 0){
            throw new RuntimeException("Alchemy error: " + error);
        }

        List<Asset> assets = new ArrayList<>();
        for (int i = 0; i < tokens.length(); i++){
            JSONObject token = tokens.getJSONObject(i);
            String contract = optString(token, "tokenAddress");
            if (contract == null || contract.equalsIgnoreCase(POLYGON_NATIVE_TOKEN_CONTRACT)){
                continue; // native coin
            }
            BigInteger balance = parseHexQuantity(token.optString("tokenBalance", "0x0"));
            if (balance.signum() <= 0){
                continue;
            }

            JSONObject metadata = token.optJSONObject("tokenMetadata");
            Asset asset = new Asset();
            asset.type = AssetType.Token;
            asset.address = addr;
            asset.contract = contract;
            int decimals = 0;
            if (metadata != null){
                asset.name = optString(metadata, "name");
                asset.symbol = optString(metadata, "symbol");
                decimals = metadata.optInt("decimals", 0);
                String logo = optString(metadata, "logo");
                asset.iconUrl = (logo != null) ? logo : "";
            } else {
                asset.iconUrl = "";
            }
            asset.decimals = Integer.toString(decimals);
            asset.balance = new BigDecimal(balance).movePointLeft(decimals).stripTrailingZeros().toPlainString();

            JSONArray prices = token.optJSONArray("tokenPrices");
            asset.rateAvailable = false;
            if (prices != null){
                for (int j = 0; j < prices.length(); j++){
                    JSONObject price = prices.getJSONObject(j);
                    if ("usd".equalsIgnoreCase(price.optString("currency"))){
                        try {
                            asset.rate = Double.parseDouble(price.getString("value"));
                            asset.rateCurrency = "USD";
                            asset.rateAvailable = true;
                        } catch (NumberFormatException e){
                            logger.warning("JAVACRYPTOTOOLS: Alchemy failed to parse price: " + price);
                        }
                        break;
                    }
                }
            }
            asset.explorerLink = getTokenWebURL(contract);
            assets.add(asset);
        }
        return assets;
    }

    // parse a NFT API v3 'getNFTsForOwner' response
    public List<Asset> parse_nft_list(JSONObject json, String addr){
        JSONArray nfts = json.getJSONArray("ownedNfts");
        List<Asset> assets = new ArrayList<>();
        for (int i = 0; i < nfts.length(); i++){
            JSONObject nft = nfts.getJSONObject(i);
            JSONObject contractInfo = nft.optJSONObject("contract");
            if (contractInfo != null && contractInfo.optBoolean("isSpam", false)){
                continue;
            }

            String contract = (contractInfo != null) ? optString(contractInfo, "address") : null;
            String tokenId = optString(nft, "tokenId");
            Asset asset = new Asset();
            asset.type = AssetType.NFT;
            asset.address = addr;
            asset.contract = contract;
            asset.tokenid = tokenId;
            asset.balance = nft.optString("balance", "1");
            if (contractInfo != null){
                asset.name = optString(contractInfo, "name");
                asset.symbol = optString(contractInfo, "symbol");
                JSONObject openSea = contractInfo.optJSONObject("openSeaMetadata");
                if (asset.name == null && openSea != null){
                    asset.name = optString(openSea, "collectionName");
                }
            }
            asset.nftName = optString(nft, "name");
            asset.nftDescription = optString(nft, "description");
            JSONObject image = nft.optJSONObject("image");
            if (image != null){
                String imageUrl = optString(image, "cachedUrl");
                asset.nftImageLink = (imageUrl != null) ? imageUrl : optString(image, "originalUrl");
                asset.nftImageSmallLink = optString(image, "thumbnailUrl");
            }
            asset.iconUrl = "";
            if (contract != null){
                asset.explorerLink = getTokenWebURL(contract);
                if (tokenId != null){
                    asset.nftExplorerLink = getNFTWebURL(contract, tokenId);
                }
            }
            assets.add(asset);
        }
        return assets;
    }

    // returns null for missing, JSON null or empty values
    private static String optString(JSONObject json, String key){
        if (json.isNull(key)){
            return null;
        }
        String value = json.optString(key, "");
        return value.isEmpty() ? null : value;
    }

    private static String urlEncode(String value){
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e){
            throw new RuntimeException(e); // UTF-8 is always supported
        }
    }

    private static BigInteger parseHexQuantity(String hex){
        String digits = (hex.startsWith("0x") || hex.startsWith("0X")) ? hex.substring(2) : hex;
        return digits.isEmpty() ? BigInteger.ZERO : new BigInteger(digits, 16);
    }
}
