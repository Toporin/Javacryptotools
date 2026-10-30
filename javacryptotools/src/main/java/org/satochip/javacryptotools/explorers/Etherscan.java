package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Etherscan API V2: a single API key for all supported EVM chains, selected with 'chainid'
// Docs: https://docs.etherscan.io/introduction
// Requires an API key in apikeys under 'API_KEY_ETHERSCAN'.
//
// The free API tier has no endpoint listing the token balances of an address, so token & NFT
// holdings are computed from the token transfer history (at most 10000 transfers per asset type).

public class Etherscan extends BaseExplorer implements Explorer{

    public static final String APIKEY_NAME = "API_KEY_ETHERSCAN";
    private static final String API_URL = "https://api.etherscan.io/v2/api";
    private static final int MAX_RESULTS = 10000; // max page size supported by Etherscan

    private static final Map<String, String> CHAIN_IDS;
    private static final Map<String, String> WEB_URLS;
    static {
        Map<String, String> ids = new HashMap<>();
        ids.put("ETH", "1");
        ids.put("ETHTEST", "11155111"); // Sepolia
        ids.put("POL", "137");
        ids.put("POLTEST", "80002"); // Amoy
        CHAIN_IDS = Collections.unmodifiableMap(ids);

        Map<String, String> urls = new HashMap<>();
        urls.put("ETH", "https://etherscan.io/");
        urls.put("ETHTEST", "https://sepolia.etherscan.io/");
        urls.put("POL", "https://polygonscan.com/");
        urls.put("POLTEST", "https://amoy.polygonscan.com/");
        WEB_URLS = Collections.unmodifiableMap(urls);
    }

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Etherscan(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }
    public Etherscan(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
    }
    public Etherscan(String coin_symbol, Map<String, String> apikeys){
        super(coin_symbol, apikeys);
    }
    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Etherscan(String coin_symbol, Map<String, String> apikeys, Level level){
        this(coin_symbol, apikeys);
    }

    public String get_api_url(){
        return API_URL;
    }

    public String get_chain_id(){
        String chainId = CHAIN_IDS.get(coin_symbol);
        if (chainId == null){
            throw new RuntimeException("Etherscan: unsupported coin " + coin_symbol);
        }
        return chainId;
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

    // send a request to the API (the api key is not logged)
    private String request(String params){
        String apikey = (apikeys != null) ? apikeys.get(APIKEY_NAME) : null;
        if (apikey == null || apikey.isEmpty()){
            throw new RuntimeException("Etherscan: missing API key " + APIKEY_NAME);
        }
        String url = get_api_url() + "?chainid=" + get_chain_id() + "&" + params;
        logFine("request: " + url);

        HttpsClient client= new HttpsClient(url + "&apikey=" + apikey);
        String content= client.request();
        if (content == null || content.isEmpty()){
            throw new RuntimeException("Etherscan: empty response");
        }
        logResponse(content);
        return content;
    }

    public double get_balance(String addr) {
        String content = request("module=account&action=balance&address=" + addr + "&tag=latest");
        return parse_balance(content);
    }

    public List<Asset> get_asset_list(String addr){
        String params = "module=account&address=" + addr + "&page=1&offset=" + MAX_RESULTS + "&sort=asc&action=";
        List<Asset> assets = parse_token_transfers(request(params + "tokentx"), addr);
        assets.addAll(parse_nft_transfers(request(params + "tokennfttx"), addr));
        assets.addAll(parse_erc1155_transfers(request(params + "token1155tx"), addr));
        return assets;
    }

    /*
     *   PARSING
     */

    public double parse_balance(String content){
        JSONObject json = new JSONObject(content);
        if (!"1".equals(json.optString("status"))){
            throw new RuntimeException("Etherscan error: " + json.optString("message") + " " + json.opt("result"));
        }
        BigDecimal wei = new BigDecimal(json.getString("result"));
        return wei.movePointLeft(18).doubleValue();
    }

    // returns the 'result' array, or an empty array if there is no transaction
    private JSONArray parseResultArray(String content){
        JSONObject json = new JSONObject(content);
        String status = json.optString("status");
        if ("1".equals(status)){
            return json.getJSONArray("result");
        }
        if ("No transactions found".equals(json.optString("message"))){
            return new JSONArray();
        }
        throw new RuntimeException("Etherscan error: " + json.optString("message") + " " + json.opt("result"));
    }

    // ERC-20: balance = sum of incoming transfers - sum of outgoing transfers
    public List<Asset> parse_token_transfers(String content, String addr){
        JSONArray txs = parseResultArray(content);
        Map<String, JSONObject> infoByContract = new LinkedHashMap<>();
        Map<String, BigInteger> balanceByContract = new HashMap<>();
        for (int i = 0; i < txs.length(); i++){
            JSONObject tx = txs.getJSONObject(i);
            String contract = tx.getString("contractAddress");
            BigInteger delta = transferDelta(tx, addr, new BigInteger(tx.optString("value", "0")));
            infoByContract.putIfAbsent(contract, tx);
            balanceByContract.merge(contract, delta, BigInteger::add);
        }

        List<Asset> assets = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : infoByContract.entrySet()){
            String contract = entry.getKey();
            BigInteger balance = balanceByContract.get(contract);
            if (balance.signum() <= 0){
                continue;
            }
            JSONObject info = entry.getValue();
            Asset asset = new Asset();
            asset.type = AssetType.Token;
            asset.address = addr;
            asset.contract = contract;
            asset.name = info.optString("tokenName", "");
            asset.symbol = info.optString("tokenSymbol", "");
            asset.decimals = info.optString("tokenDecimal", "0");
            asset.balance = toDecimalString(balance, asset.decimals);
            asset.iconUrl = "";
            asset.explorerLink = getTokenWebURL(contract);
            assets.add(asset);
        }
        return assets;
    }

    // ERC-721: an nft is owned if its last transfer was to addr (transfers sorted in ascending order)
    public List<Asset> parse_nft_transfers(String content, String addr){
        JSONArray txs = parseResultArray(content);
        Map<String, JSONObject> owned = new LinkedHashMap<>();
        for (int i = 0; i < txs.length(); i++){
            JSONObject tx = txs.getJSONObject(i);
            String key = tx.getString("contractAddress") + "/" + tx.getString("tokenID");
            if (tx.optString("from").equalsIgnoreCase(addr)){
                owned.remove(key);
            }
            if (tx.optString("to").equalsIgnoreCase(addr)){
                owned.put(key, tx);
            }
        }

        List<Asset> assets = new ArrayList<>();
        for (JSONObject tx : owned.values()){
            assets.add(parseNft(tx, addr, "1"));
        }
        return assets;
    }

    // ERC-1155: balance per (contract, tokenId) = sum of incoming - sum of outgoing transfers
    public List<Asset> parse_erc1155_transfers(String content, String addr){
        JSONArray txs = parseResultArray(content);
        Map<String, JSONObject> infoByKey = new LinkedHashMap<>();
        Map<String, BigInteger> balanceByKey = new HashMap<>();
        for (int i = 0; i < txs.length(); i++){
            JSONObject tx = txs.getJSONObject(i);
            String key = tx.getString("contractAddress") + "/" + tx.getString("tokenID");
            BigInteger delta = transferDelta(tx, addr, new BigInteger(tx.optString("tokenValue", "0")));
            infoByKey.putIfAbsent(key, tx);
            balanceByKey.merge(key, delta, BigInteger::add);
        }

        List<Asset> assets = new ArrayList<>();
        for (Map.Entry<String, JSONObject> entry : infoByKey.entrySet()){
            BigInteger balance = balanceByKey.get(entry.getKey());
            if (balance.signum() > 0){
                assets.add(parseNft(entry.getValue(), addr, balance.toString()));
            }
        }
        return assets;
    }

    private Asset parseNft(JSONObject tx, String addr, String balance){
        String contract = tx.getString("contractAddress");
        String tokenId = tx.getString("tokenID");
        Asset asset = new Asset();
        asset.type = AssetType.NFT;
        asset.address = addr;
        asset.contract = contract;
        asset.name = tx.optString("tokenName", "");
        asset.symbol = tx.optString("tokenSymbol", "");
        asset.tokenid = tokenId;
        asset.balance = balance;
        asset.iconUrl = "";
        asset.explorerLink = getTokenWebURL(contract);
        asset.nftExplorerLink = getNFTWebURL(contract, tokenId);
        return asset;
    }

    private static BigInteger transferDelta(JSONObject tx, String addr, BigInteger value){
        BigInteger delta = BigInteger.ZERO;
        if (tx.optString("to").equalsIgnoreCase(addr)){
            delta = delta.add(value);
        }
        if (tx.optString("from").equalsIgnoreCase(addr)){
            delta = delta.subtract(value);
        }
        return delta;
    }

    private static String toDecimalString(BigInteger value, String decimals){
        int decimalsInt;
        try {
            decimalsInt = Integer.parseInt(decimals);
        } catch (NumberFormatException e){
            decimalsInt = 0;
        }
        return new BigDecimal(value).movePointLeft(decimalsInt).stripTrailingZeros().toPlainString();
    }

}
