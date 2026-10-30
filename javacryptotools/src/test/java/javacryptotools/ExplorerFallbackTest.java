package javacryptotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.BaseCoin;
import org.satochip.javacryptotools.coins.Bitcoin;
import org.satochip.javacryptotools.coins.UnsupportedCoin;
import org.satochip.javacryptotools.explorers.Explorer;
import org.satochip.javacryptotools.explorers.PriceExplorer;

// Offline tests for the explorer fallback logic in BaseCoin, using fake explorers
class ExplorerFallbackTest {

    static class FailingExplorer implements Explorer {
        int calls = 0;
        public String get_address_weburl(String addr) { calls++; throw new RuntimeException("down"); }
        public double get_balance(String addr) { calls++; throw new RuntimeException("down"); }
        public List<Asset> get_asset_list(String addr) { calls++; throw new RuntimeException("down"); }
    }

    static class FixedExplorer implements Explorer {
        final double balance;
        final List<Asset> assets;
        int calls = 0;
        FixedExplorer(double balance, List<Asset> assets) {
            this.balance = balance;
            this.assets = assets;
        }
        public String get_address_weburl(String addr) { calls++; return "https://fixed/" + addr; }
        public double get_balance(String addr) { calls++; return balance; }
        public List<Asset> get_asset_list(String addr) { calls++; return assets; }
    }

    static class FixedPriceExplorer implements PriceExplorer {
        final double rate;
        final boolean fail;
        String lastCoin, lastOtherCoin;
        FixedPriceExplorer(double rate, boolean fail) {
            this.rate = rate;
            this.fail = fail;
        }
        public double get_exchange_rate_with(String other_coin) { throw new UnsupportedOperationException(); }
        public double get_exchange_rate_between(String coin, String other_coin) {
            lastCoin = coin;
            lastOtherCoin = other_coin;
            if (fail) throw new RuntimeException("down");
            return rate;
        }
        public String get_price_weburl() { return ""; }
        public String get_price_weburl(String coin) { return ""; }
    }

    BaseCoin coin;

    @BeforeEach
    void setUp() {
        coin = new Bitcoin(false, null);
        coin.explorers = new ArrayList<>();
        coin.priceExplorers = new ArrayList<>();
    }

    @Test
    void firstSuccessfulExplorerIsUsed() {
        FailingExplorer failing = new FailingExplorer();
        List<Asset> assets = Collections.singletonList(new Asset());
        FixedExplorer first = new FixedExplorer(1.5, assets);
        FixedExplorer second = new FixedExplorer(2.5, null);
        coin.explorers.add(failing);
        coin.explorers.add(first);
        coin.explorers.add(second);

        assertEquals(1.5, coin.getBalance("addr"));
        assertSame(assets, coin.getAssetList("addr"));
        assertEquals("https://fixed/addr", coin.getAddressWeburl("addr"));
        assertEquals(3, failing.calls);
        assertEquals(3, first.calls);
        assertEquals(0, second.calls);
    }

    @Test
    void allExplorersFailing_returnsDefaults() {
        coin.explorers.add(new FailingExplorer());
        coin.explorers.add(new FailingExplorer());

        assertEquals(-1, coin.getBalance("addr"));
        assertNull(coin.getAssetList("addr"));
        assertEquals("", coin.getAddressWeburl("addr"));
    }

    @Test
    void noExplorer_returnsDefaults() {
        UnsupportedCoin unsupported = new UnsupportedCoin(false, null);
        assertEquals(-1, unsupported.getBalance("addr"));
        assertNull(unsupported.getAssetList("addr"));
        assertEquals("", unsupported.getAddressWeburl("addr"));
        assertEquals(-1, unsupported.get_exchange_rate_with("USD"));
    }

    @Test
    void priceExplorer_negativeRateOrExceptionFallsThrough() {
        FixedPriceExplorer failing = new FixedPriceExplorer(0, true);
        FixedPriceExplorer unavailable = new FixedPriceExplorer(-1, false);
        FixedPriceExplorer working = new FixedPriceExplorer(65000.0, false);
        coin.priceExplorers.add(failing);
        coin.priceExplorers.add(unavailable);
        coin.priceExplorers.add(working);

        assertEquals(65000.0, coin.get_exchange_rate_with("USD"));
        assertEquals("BTC", working.lastCoin);
        assertEquals("USD", working.lastOtherCoin);
    }

    @Test
    void priceExplorer_allFailing_returnsMinusOne() {
        coin.priceExplorers.add(new FixedPriceExplorer(0, true));
        coin.priceExplorers.add(new FixedPriceExplorer(-1, false));

        assertEquals(-1, coin.get_exchange_rate_with("EUR"));
        assertEquals(-1, coin.get_exchange_rate_between("ETH", "EUR"));
    }

    @Test
    void priceExplorer_testnetCoinSkipsRequest() {
        BaseCoin testnet = new Bitcoin(true, null);
        FixedPriceExplorer explorer = new FixedPriceExplorer(65000.0, false);
        testnet.priceExplorers = new ArrayList<>();
        testnet.priceExplorers.add(explorer);

        assertEquals(0, testnet.get_exchange_rate_with("USD"));
        assertNull(explorer.lastCoin);
    }
}
