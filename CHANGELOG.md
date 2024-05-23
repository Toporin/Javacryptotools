
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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