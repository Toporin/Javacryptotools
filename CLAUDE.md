# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the JAR
./gradlew clean build

# Run all tests
./gradlew test

# Run a specific test class (package prefix required)
./gradlew test --tests "javacryptotools.CoinTest"

# Run a specific test method
./gradlew test --tests "javacryptotools.CoinTest.test_btc"

# Force re-run when Gradle skips due to no changes
./gradlew test --rerun-tasks
```

The output JAR is produced at `javacryptotools/build/libs/javacryptotools-<version>.jar`.

## Architecture

This is a single-module Gradle project (`javacryptotools/`) with the main source under `src/main/java/org/satochip/javacryptotools/` and tests under `src/test/java/javacryptotools/`.

### Three-layer structure

**`coins/`** — Core coin abstractions and cryptographic utilities
- `BaseCoin` is the abstract base class all coins extend. It holds coin parameters (magic bytes, HD path, SegWit HRP, xprv/xpub headers), manages lists of attached `Explorer` and `PriceExplorer` objects, and implements the public-facing API (`pubToAddress`, `pubToSegwitAddress`, `encodePrivkey`, `getBalance`, `getAssetList`, `get_exchange_rate_with`).
- Concrete implementations: `Bitcoin`, `Litecoin`, `BitcoinCash`, `Ethereum`, `Polygon`, `Counterparty`, `UnsupportedCoin`.
- Supporting utilities: `Base58`, `Bech32`, `CashAddress`, `Sha256Hash`, `Utils` (hash160).

**`explorers/`** — Pluggable blockchain and price data sources
- `Explorer` interface: `get_balance`, `get_asset_list`, `get_address_weburl`.
- `PriceExplorer` interface: `get_exchange_rate_with`, `get_exchange_rate_between`, `get_price_weburl`.
- `BaseExplorer` abstract class holds the coin reference and API keys.
- Coins hold ordered lists of explorers; `BaseCoin` tries each in sequence and returns the first successful result.
- `HttpsClient` is the shared HTTP utility used by all explorers.

**`utils/`** — Low-level utilities (`Hash` for Keccak-256, `Numeric` for hex, `Strings`).

### Coin instantiation pattern

```java
HashMap<String, String> apikeys = new HashMap<>();
apikeys.put("etherscan", "YOUR_KEY");
Bitcoin coin = new Bitcoin(/*isTestnet=*/false, apikeys);
```

Each coin constructor wires up its appropriate explorers (e.g., `Bitcoin` attaches `Blockstream`; `Litecoin` attaches `Blockcypher` and `LitecoinSpace`; `Ethereum` and `Polygon` attach `Blockscout`; `Counterparty` attaches `Tokenscan`). Price explorers are `Coingate`, or `CoinCombined` for Counterparty.

### Adding a new coin

1. Extend `BaseCoin`, set all coin parameters in the constructor.
2. Override `pubToAddress` if the address derivation differs from standard P2PKH/P2WPKH.
3. Instantiate and attach appropriate `Explorer` / `PriceExplorer` objects to `this.explorers` / `this.priceExplorers`.
4. Add SLIP44 path constant to `Constants.java`.

### Adding a new explorer

1. Extend `BaseExplorer` and implement `Explorer` and/or `PriceExplorer`.
2. Use `HttpsClient` for all HTTP requests.
3. Attach an instance in the relevant coin constructor(s).

## Key dependencies

| Dependency | Purpose |
|---|---|
| Bouncy Castle (`bcprov-jdk15to18`) | Cryptographic primitives, EC operations |
| Guava (android variant) | Utilities |
| `org.json` | JSON parsing in explorers |
| JUnit Jupiter 5 | Tests |

## Tests

Tests live in `src/test/java/javacryptotools/` and run offline (no network access):

- `CoinTest`: address generation (SegWit, legacy, CashAddr, EIP-55) and WIF encoding for every coin on mainnet and testnet. Test vectors are in `TestVectors`, derived from the BIP39 seed `tonight stem cause eyebrow estate smart duck wrong toe under job danger`.
- `CoinPropertiesTest`: coin metadata, attached explorers, address web urls, testnet exchange rates.
- `ExplorerFallbackTest`: `BaseCoin` explorer fallback logic, using fake explorers.
- `AssetParsingTest`: Blockscout/Tokenscan token & NFT parsing, using recorded API responses in `src/test/resources/`.
- `EncodingTest`: Base58, Bech32, CashAddress and SLIP44 constants.

When adding a coin, add it to the case lists in `CoinTest` and `CoinPropertiesTest`.
