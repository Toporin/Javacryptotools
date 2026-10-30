# Javacryptotools - java library for simple crypto coins integration

Simple java library that allows to integrate crypto coins info into java & android project.

Coins supported currently:
* Bitcoin
* Litecoin
* Bitcoin-Cash
* Ethereum and ERC20

Planned:
* Dogecoin
* Dash
* Ethereum Classic
* Binance Smart Chain
* And more!

Each *coin* has its own class derived from the *BaseCoin* class. 
Each coin class provides the following functionnalities:
* Compute address from pubkey (legacy and segwit if supported)
* Encode privkey to WIF format
* Various blockchain info: support segwit, use compressed pubkey, supports token (smart contract) and NFT, ...

Each coin can also have various *Explorer* objects attached to it. These explorers are derived from the *BaseExplorer* class and provide different information related to a coin:
* Explorer: provides balance, and other blockchain info
* NftExplorer: providesinfo about NFT assets on the blockchain such as name, description, url, image preview
* PriceExplorer: provides info such as exchange rateswith common (fiat) currencies

Explorer classes use API from the following services:
* [Blockstream](https://blockstream.com/)
* Sochain](https://sochain.com/)
* [Fulcrum](https://github.com/cculianu/Fulcrum) (Electrum protocol servers)
* [Alchemy](https://www.alchemy.com/)
* Etherscan](https://etherscan.io/)
* Rarible](https://rarible.com/)
* Opensea](https://opensea.io/)
* Coingecko](https://www.coingecko.com/)

These APIs are used to get info such as balances, NFT name, description and image preview.

Remark: some explorer APIs require an API key to work correctly. API keys are provided through a hashmap<string:key, string:value>.
See [api_keys(sample).ini](api_keys(sample).ini) for the list of supported keys, e.g.:

```java
HashMap<String, String> apikeys = new HashMap<>();
apikeys.put("API_KEY_ALCHEMY", "YOUR_ALCHEMY_API_KEY"); // required for Polygon
Polygon coin = new Polygon(true, apikeys);
```

## Build and Usage

You can build the jar file with ```./gradlew clean build```

You can import the different libraries in your Gradle project by placing the .jar library in a folder (e.g. 'libs')
and by adding the following line in the *dependencies* section of your *build.gradle* file:

```api files('libs/javacryptotools-0.2.0.jar')```

In the future, the library will also be available on Maven central repository.

## Tests

Tests are located in `javacryptotools/src/test/java/javacryptotools/` and use JUnit 5.
They are split in two groups: offline unit tests and online integration tests.

### Offline unit tests

Offline tests do not require network access or API keys. They cover address generation and WIF encoding
for all supported coins (mainnet & testnet), coin metadata, explorer urls, explorer fallback logic,
token/NFT parsing (using recorded API responses in `javacryptotools/src/test/resources/`) and low-level encodings.

```
./gradlew test
```

Useful options:

```
./gradlew test --tests "javacryptotools.CoinTest"                         # run a single test class
./gradlew test --tests "javacryptotools.CoinTest.ethereum_toChecksumAddress_followsEip55"  # run a single test
./gradlew test --rerun-tasks                                              # force re-run when Gradle reports the task as up-to-date
```

### Online integration tests

Integration tests (JUnit tag `network`, class `NetworkTest`) send real requests to the third-party APIs used by the library,
and check balances, token & NFT lists and fiat exchange rates for each supported coin. They require network access,
and some of them require API keys:

1. Copy the API keys template to `api_keys.ini` in the project root (this file is ignored by git, never commit your keys):
   ```
   cp "api_keys(sample).ini" api_keys.ini
   ```
2. Edit `api_keys.ini` and replace the placeholders with your own keys (e.g. `API_KEY_ALCHEMY` for Polygon).
   Tests requiring a missing key are skipped instead of failing.
3. Run the integration tests:
   ```
   ./gradlew integrationTest
   ```

Integration tests are never considered up-to-date by Gradle, so they always run.
Results depend on external services: a failure may be caused by a temporary outage or rate limit of a third-party API.

To run both offline and online tests: `./gradlew test integrationTest`.

Test reports are generated in `javacryptotools/build/reports/tests/test/index.html`
and `javacryptotools/build/reports/tests/integrationTest/index.html`.

## License and attribution

This project is based on the python library [Pybitcointools](https://github.com/Toporin/pybitcointools) and released under the LGPL v3 license.