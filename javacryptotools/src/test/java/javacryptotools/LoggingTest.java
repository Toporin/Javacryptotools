package javacryptotools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.satochip.javacryptotools.coins.BaseCoin;
import org.satochip.javacryptotools.coins.Bitcoin;
import org.satochip.javacryptotools.coins.BitcoinCash;
import org.satochip.javacryptotools.coins.Counterparty;
import org.satochip.javacryptotools.coins.Ethereum;
import org.satochip.javacryptotools.coins.Litecoin;
import org.satochip.javacryptotools.coins.Polygon;
import org.satochip.javacryptotools.coins.UnsupportedCoin;
import org.satochip.javacryptotools.explorers.CoinCombined;
import org.satochip.javacryptotools.explorers.HttpsClient;

// Offline tests for the logging policy: the library never sets the logger level,
// errors are logged once at WARNING, debug info at FINE, and API keys are never logged.
class LoggingTest {

    static class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();
        @Override public void publish(LogRecord record) { records.add(record); }
        @Override public void flush() {}
        @Override public void close() {}

        List<String> messages(Level level) {
            return records.stream().filter(r -> r.getLevel() == level)
                    .map(LogRecord::getMessage).collect(Collectors.toList());
        }
    }

    final Logger logger = Logger.getLogger(BaseCoin.LOGGER_NAME);
    final CapturingHandler handler = new CapturingHandler();
    Level savedLevel;

    @BeforeEach
    void setUp() {
        savedLevel = logger.getLevel();
        logger.setLevel(Level.FINE);
        logger.addHandler(handler);
    }

    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
        logger.setLevel(savedLevel);
    }

    @SuppressWarnings("deprecation")
    @Test
    void constructorsDoNotChangeLoggerLevel() {
        // the application may configure a parent logger: the library logger must inherit its level
        logger.setLevel(null);
        new Bitcoin(false, null);
        new Litecoin(true, null);
        new BitcoinCash(false, null);
        new Ethereum(false, null);
        new Polygon(true, null);
        new Counterparty(false, null);
        new UnsupportedCoin(false, null);
        new CoinCombined(new Bitcoin(false, null), null);
        // deprecated constructors: the level argument is ignored
        new Bitcoin(false, null, Level.WARNING);
        new Ethereum(false, null, Level.INFO);
        new CoinCombined(new Bitcoin(false, null), null, Level.SEVERE);
        assertNull(logger.getLevel());

        logger.setLevel(Level.FINEST);
        new Litecoin(false, null, Level.WARNING);
        assertEquals(Level.FINEST, logger.getLevel());
    }

    @Test
    void explicitLevelSetterIsAvailable() {
        new Bitcoin(false, null).setLoggerLevel("debug");
        assertEquals(Level.FINE, logger.getLevel());
        new Bitcoin(false, null).setLoggerLevel(Level.WARNING);
        assertEquals(Level.WARNING, logger.getLevel());
    }

    @Test
    void failingExplorerLogsOneWarningWithCause() {
        Bitcoin coin = new Bitcoin(false, null);
        coin.explorers = new ArrayList<>();
        coin.explorers.add(new ExplorerFallbackTest.FailingExplorer());

        assertEquals(-1, coin.getBalance("addr"));

        List<String> warnings = handler.messages(Level.WARNING);
        assertEquals(1, warnings.size(), "warnings: " + warnings);
        String warning = warnings.get(0);
        assertTrue(warning.startsWith("Bitcoin (BTC): FailingExplorer failed to get balance"), warning);
        assertTrue(warning.contains("down"), "cause missing: " + warning);
    }

    @Test
    void successLogsFineOnly() {
        Bitcoin coin = new Bitcoin(false, null);
        coin.explorers = new ArrayList<>();
        coin.explorers.add(new ExplorerFallbackTest.FixedExplorer(1.5, null));

        assertEquals(1.5, coin.getBalance("addr"));

        assertTrue(handler.messages(Level.WARNING).isEmpty());
        assertEquals(1, handler.messages(Level.FINE).size());
        assertTrue(handler.messages(Level.FINE).get(0).startsWith("Bitcoin (BTC): balance from FixedExplorer"));
    }

    @Test
    void noFineRecordsWhenLevelIsWarning() {
        logger.setLevel(Level.WARNING);
        Bitcoin coin = new Bitcoin(false, null);
        coin.explorers = new ArrayList<>();
        coin.explorers.add(new ExplorerFallbackTest.FixedExplorer(1.5, null));

        coin.getBalance("addr");

        assertTrue(handler.records.isEmpty());
    }

    @Test
    void priceExplorerReturningMinusOneIsNotLoggedTwice() {
        Bitcoin coin = new Bitcoin(false, null);
        coin.priceExplorers = new ArrayList<>();
        coin.priceExplorers.add(new ExplorerFallbackTest.FixedPriceExplorer(-1, false));

        assertEquals(-1, coin.get_exchange_rate_with("USD"));

        // the price explorer is responsible for logging the error before returning -1
        assertTrue(handler.messages(Level.WARNING).isEmpty());
    }

    @Test
    void httpsClientFailureDoesNotLogUrl() {
        // connection refused: no network access needed
        String secret = "SECRET_API_KEY_123";
        HttpsClient client = new HttpsClient("https://localhost:1/v2/" + secret + "?apikey=" + secret);

        String content = client.request();

        assertTrue(content == null || content.isEmpty());
        List<String> warnings = handler.messages(Level.WARNING);
        assertEquals(1, warnings.size(), "warnings: " + warnings);
        assertTrue(warnings.get(0).contains("localhost"), warnings.get(0));
        for (LogRecord record : handler.records) {
            assertFalse(record.getMessage().contains(secret), "API key logged: " + record.getMessage());
        }
    }
}
