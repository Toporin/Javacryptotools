package javacryptotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.AssetType;
import org.satochip.javacryptotools.coins.Counterparty;
import org.satochip.javacryptotools.coins.Ethereum;
import org.satochip.javacryptotools.explorers.Blockscout;
import org.satochip.javacryptotools.explorers.Tokenscan;

// Offline tests for token & NFT parsing, using recorded API responses from src/test/resources
class AssetParsingTest {

    static final String ETH_OWNER = "0x00000000219ab540356cbb839cbe05303d7705fa";
    static final String XCP_OWNER = "1BurnPepexxxxxxxxxxxxxxxxxxxAK33R";

    static JSONObject loadFixture(String name) throws IOException {
        try (InputStream in = AssetParsingTest.class.getResourceAsStream("/" + name)) {
            assertNotNull(in, "missing fixture " + name);
            return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    static List<Asset> parseBlockscout(String fixture) throws IOException {
        Blockscout explorer = new Blockscout(new Ethereum(false, null), null);
        JSONArray items = loadFixture(fixture).getJSONArray("items");
        List<Asset> assets = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            assets.add(explorer.parseAssetFromJson(items.getJSONObject(i), ETH_OWNER));
        }
        return assets;
    }

    static List<Asset> parseTokenscan(String fixture) throws IOException {
        Tokenscan explorer = new Tokenscan(new Counterparty(false, null), null);
        JSONArray items = loadFixture(fixture).getJSONArray("data");
        List<Asset> assets = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            assets.add(explorer.parseAssetFromJson(items.getJSONObject(i), XCP_OWNER));
        }
        return assets;
    }

    /*
     *   BLOCKSCOUT
     */

    @Test
    void blockscout_erc20TokenWithRate() throws IOException {
        Asset usdt = parseBlockscout("blockscout_tokens.json").get(0);
        assertEquals(AssetType.Token, usdt.type);
        assertEquals("Tether", usdt.name);
        assertEquals("USDT", usdt.symbol);
        assertEquals("0xdAC17F958D2ee523a2206206994597C13D831ec7", usdt.contract);
        assertEquals("4986.362222", usdt.balance); // 6 decimals
        assertTrue(usdt.rateAvailable);
        assertEquals(0.998951, usdt.rate);
        assertEquals("USD", usdt.rateCurrency);
        assertTrue(usdt.iconUrl.startsWith("https://"));
        assertEquals("https://eth.blockscout.com/token/0xdAC17F958D2ee523a2206206994597C13D831ec7", usdt.explorerLink);
    }

    @Test
    void blockscout_erc20TokenWith18Decimals() throws IOException {
        Asset weth = parseBlockscout("blockscout_tokens.json").get(1);
        assertEquals("WETH", weth.symbol);
        assertEquals("0.01000641", weth.balance);
    }

    @Test
    void blockscout_erc20TokenWithNullFields() throws IOException {
        Asset token = parseBlockscout("blockscout_tokens.json").get(2);
        assertEquals(AssetType.Token, token.type);
        assertEquals("0x1111111111111111111111111111111111111111", token.contract);
        assertEquals("42", token.balance); // null decimals => 0 decimals
        assertFalse(token.rateAvailable);
        assertEquals("", token.iconUrl);
    }

    @Test
    void blockscout_erc721WithMetadata() throws IOException {
        Asset nft = parseBlockscout("blockscout_nfts.json").get(0);
        assertEquals(AssetType.NFT, nft.type);
        assertEquals("Vitalik RGB", nft.name);
        assertEquals("vRGB", nft.symbol);
        assertEquals("0x010D1B562B3515839563f84d0AEbA146fD92Df7a", nft.contract);
        assertEquals("1", nft.tokenid);
        assertEquals("1", nft.balance);
        assertEquals("Vitalik RGB", nft.nftName);
        assertEquals("Mint first\nReveal after\nHold tight", nft.nftDescription);
        assertTrue(nft.nftImageLink.startsWith("https://"));
        assertEquals("https://eth.blockscout.com/token/0x010D1B562B3515839563f84d0AEbA146fD92Df7a/instance/1", nft.nftExplorerLink);
        assertFalse(nft.rateAvailable);
    }

    @Test
    void blockscout_erc721WithoutMetadata() throws IOException {
        Asset nft = parseBlockscout("blockscout_nfts.json").get(1);
        assertEquals(AssetType.NFT, nft.type);
        assertEquals("746", nft.tokenid);
        assertNull(nft.nftImageLink);
        assertNull(nft.nftName);
        assertNull(nft.nftDescription);
    }

    @Test
    void blockscout_erc1155WithoutTokenId() throws IOException {
        Asset nft = parseBlockscout("blockscout_nfts.json").get(2);
        assertEquals(AssetType.NFT, nft.type);
        assertNull(nft.tokenid);
        assertEquals("3", nft.balance);
        assertEquals("https://eth.blockscout.com/token/0x2222222222222222222222222222222222222222"
                + "?tab=inventory&holder_address_hash=" + ETH_OWNER, nft.nftExplorerLink);
    }

    /*
     *   TOKENSCAN
     */

    @Test
    void tokenscan_skipsNativeXcp() throws IOException {
        assertNull(parseTokenscan("tokenscan_balances.json").get(0));
    }

    @Test
    void tokenscan_tokenWithRate() throws IOException {
        Asset token = parseTokenscan("tokenscan_balances.json").get(1);
        assertEquals(AssetType.Token, token.type);
        assertEquals("A6900000000000001774", token.name);
        assertEquals("A6900000000000001774", token.contract);
        assertEquals("33.00000000", token.balance);
        assertEquals("LP token for PEPECASH/XCP pool", token.description);
        assertTrue(token.rateAvailable);
        assertEquals(10.75, token.rate);
        assertEquals("USD", token.rateCurrency);
        assertEquals("https://tokenscan.io/asset/A6900000000000001774", token.explorerLink);
    }

    @Test
    void tokenscan_assetWithoutImageIsToken() throws IOException {
        Asset token = parseTokenscan("tokenscan_balances.json").get(2);
        assertEquals("BAGHOLDRPEPE", token.name);
        assertEquals(AssetType.Token, token.type);
        assertNull(token.nftImageLink);
    }

    @Test
    void tokenscan_imgurDescriptionIsNft() throws IOException {
        Asset nft = parseTokenscan("tokenscan_balances.json").get(3);
        assertEquals(AssetType.NFT, nft.type);
        assertEquals("https://i.imgur.com/AbCdEf1.png", nft.nftImageLink);
        assertEquals("https://tokenscan.io/asset/IMGURPEPE", nft.nftExplorerLink);
        assertFalse(nft.rateAvailable);
    }

    @Test
    void tokenscan_imageUrlDescriptionIsNft() throws IOException {
        Asset nft = parseTokenscan("tokenscan_balances.json").get(4);
        assertEquals(AssetType.NFT, nft.type);
        assertEquals("https://example.com/pepe.png", nft.nftImageLink);
        assertFalse(nft.rateAvailable);
    }
}
