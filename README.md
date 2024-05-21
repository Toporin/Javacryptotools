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
* Fullstack](https://fullstack.cash/)
* Etherscan](https://etherscan.io/)
* Rarible](https://rarible.com/)
* Opensea](https://opensea.io/)
* Coingecko](https://www.coingecko.com/)

These APIs are used to get info such as balances, NFT name, description and image preview.

Remark: some explorer APIs require an API key to work correctly. API keys are provided through a hashmap<string:key, string:value>.

## Build and Usage

You can build the jar file with ```./gradlew clean build```

You can import the different libraries in your Gradle project by placing the .jar library in a folder (e.g. 'libs')
and by adding the following line in the *dependencies* section of your *build.gradle* file:

```api files('libs/javacryptotools-0.2.0.jar')```

In the future, the library will also be available on Maven central repository.

## License and attribution

This project is based on the python library [Pybitcointools](https://github.com/Toporin/pybitcointools) and released under the LGPL v3 license.