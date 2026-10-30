
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [0.4.1 - unreleased]:

* Update Gradle wrapper scripts to match Gradle 8.10
* Enable verbose test logging
* Add CLAUDE.md
* Fix testnet flag (is_testnet) never being set: testnet coins now return an exchange rate of 0
* Switch Ethereum testnet from Ropsten to Sepolia (breaking: symbol 'ROP' is now 'ETHTEST', display name 'Sepolia Testnet')
* Add LitecoinSpace explorer: adds Litecoin testnet support and a fallback explorer for Litecoin mainnet
* Fix token contract & explorer links in Blockscout explorer (API now uses 'address_hash')
* Fix string comparison in Blockcypher explorer
* Rework test suite: offline tests for all coins (mainnet & testnet), explorer urls, explorer fallback, token/NFT parsing and encodings
* Switch Polygon testnet from Mumbai to Amoy (breaking: symbol 'MUMBAI' is now 'POLTEST', display name 'Polygon Amoy Testnet'), using Etherscan explorer
* Rewrite Etherscan explorer for API V2 (ETH, Sepolia, Polygon, Amoy), with token & NFT holdings computed from transfer history; requires API_KEY_ETHERSCAN
* Add Etherscan as fallback explorer for Polygon mainnet (polygon.blockscout.com is behind a Cloudflare challenge)
* Fix Blockscout explorer returning a zero balance / empty asset list when a request fails
* Add network integration tests (./gradlew integrationTest) using API keys from api_keys.ini (template: api_keys(sample).ini)

## [0.4.0]:

* Simplify API & remove unused code
* Add Blockscout & Tokenscan explorer

## [0.3.1]:

* Update ticker from MATIC to POL for Polygon 

## [0.3.0]:

* Added support for Polygon:

* Added Covalent explorer for Polygon
* Added CovalentNFT NFT explorer for Polygon

## [0.2.0]: 

* Add listing assets support:
  * get a list of Tokens & NFTs associated with a given address
  * For each asset, populate an object Asset with info on a best effort basis

* Add Ethplorer explorer for Ethereum
* Add Blockcypher explorer for Litecoin (& Dogecoin)
* Blockstream, Fullstack, Sochain: get_asset_list() returns empty list instead of null
* Improve logging
* add Checksumed address support for ethereum and forks

* Improve price explorer support:
  * Add Coingate price explorer
  * Add CoinCombined price explorer (uses Coingate & Coingecko)
  * Add exchange rate & valuation data in Asset
  * Counterparty: add Coincombined as default price explorer
  * Ethereum: add Coincombined as default price explorer


## [0.1.0]: 

Add Counterparty (XCP) and UnsupportedCoin (???) support.
UnsupportedCoin is the default coin when support does not exist yet. 
    
## [0.0.4]: 
    
* Use java.util.logger instead of System.out.println traces
* Use Coin.setLoggerLevel() to define level of logging (INFO for debug, or WARNING otherwise)
* Add UnsupportedCoin class, used for coins that are not (yet) supported
* Clean code a bit

## [0.0.3]: 

add PriceExplorer: allows to get exchange rate of coins versus most common currencies using coingecko API

## [0.0.2]: 

Add static Set defining the Coin (chain) that supports token (and NFC)

## [0.0.1]: 

Initial commit