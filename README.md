# Javacryptotools - java library for simple crypto coins integration

[![](https://jitpack.io/v/Toporin/Javacryptotools.svg)](https://jitpack.io/#Toporin/Javacryptotools)

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

## Installation

The library is published on [JitPack](https://jitpack.io/#Toporin/Javacryptotools) and can be used
in Android applications and desktop Java applications (Java 8 or higher).

Replace `<version>` with a release tag (e.g. `v0.4.1`), a commit hash, or `<branch>-SNAPSHOT`.

### Gradle

Add the JitPack repository. With a recent Gradle setup (e.g. Android projects), in `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Or, with an older setup, in the `repositories` section of your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

Then add the dependency in your module `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.Toporin:Javacryptotools:<version>'
}
```

With the Kotlin DSL (`build.gradle.kts`), use `maven("https://jitpack.io")` and
`implementation("com.github.Toporin:Javacryptotools:<version>")`.

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Toporin</groupId>
    <artifactId>Javacryptotools</artifactId>
    <version>VERSION</version>
</dependency>
```

### Android notes

- Explorer methods (`getBalance`, `getAssetList`, `get_exchange_rate_with`...) perform blocking network requests:
  call them from a background thread, not from the main thread.
- Add the `android.permission.INTERNET` permission to your `AndroidManifest.xml`.

### Build from source

```
./gradlew clean build
```

The jar file is generated in `javacryptotools/build/libs/`. It can also be published to your local Maven repository
(`~/.m2`) with `./gradlew publishToMavenLocal` (coordinates `com.github.Toporin:javacryptotools:<version>`).

### Publishing a new release

JitPack builds the library on demand from GitHub, using the configuration in `jitpack.yml`
(JDK 17, offline unit tests run before publishing). To publish a new release:

1. update `version` in `javacryptotools/build.gradle` and the `CHANGELOG.md`;
2. create and push a git tag, e.g. `git tag v0.4.1 && git push origin v0.4.1` (optionally, create a GitHub release);
3. open https://jitpack.io/#Toporin/Javacryptotools and click "Get it" for the new tag to trigger the build
   (or simply request the new version from a consumer project). Build logs are available on the same page.

## Logging

The library uses `java.util.logging` with the logger `org.satochip.javacryptotools`
(constant `BaseCoin.LOGGER_NAME`). The library never sets the logger level: the application configures it,
either on this logger or on a parent logger such as `org.satochip`. Levels used:

* `WARNING`: errors, including third-party API failures (e.g. an explorer failing, with the fallback to the next one)
* `FINE`: debug info (API requests, truncated responses, results)

API keys are never logged. Example:

```java
Logger logger = Logger.getLogger("org.satochip.javacryptotools");
logger.setLevel(Level.FINE); // or Level.WARNING for errors only, Level.OFF to disable
```

On Android, the default `java.util.logging` handler maps `FINE` to `Log.DEBUG`, which logcat may filter out:
to display debug logs, add your own `Handler` (e.g. forwarding records to `android.util.Log` or to an in-app log).

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