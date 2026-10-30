package javacryptotools;

// Test vectors made with https://iancoleman.io/bip39/
// seed: tonight stem cause eyebrow estate smart duck wrong toe under job danger
// BTC/LTC: path m/84'/<coin>'/0'/0/* (BIP84 path to get both bech32 and base58 addresses)
// BCH/ETH: path m/0/*
// BCH testnet vectors (cashaddr, legacy & WIF) were derived from the BCH mainnet vectors
// with an independent implementation of cashaddr and base58check.

final class TestVectors {

    private TestVectors() {}

    /* BTC */
    static final String[] PUBKEY_BTC = {
            "03b7b3957daedecee4488dcb0b8cf3f3372d64d5c559953d2a2539f55e6474c8ce",
            "03e5c1e865d21a239c6639e75586df1f0a5e59853694601e78dccb22481fad08c0",
            "03f21a3b7ff93a4396d886b04b045b8a4dfaa3e13ae169adf36a7390f65af964c0",
            "035f6cb6545543c6b69ba402e19362a71c9ff58a93f8c2d812e0a6c27c6304e5d2",
            "03e1d8b41fa14419293b29ad6f98d5bd1827ae21b5f1083a7cc001955db2ee628c",
    };
    static final String[] ADDRESS_LEGACY_BTC = {
            "1Q6QXhpreAW8wDRwaL6jvdEcbbceFMw2mv",
            "1976pT5yu88hDa7HsQK76tpbyYtPTyN3cF",
            "1QC6JNGbXdmQFkBp69yFFXdYZKvXtfCeEx",
            "14Kz6dHFJjJNqj2hvQ84vSzYq78T9pmoWi",
            "1Lr8JCa936osnV288Jm3LYBKKvvvQJkdfy",
    };
    static final String[] ADDRESS_SEGWIT_BTC = {
            "bc1ql4gf6wjve0enmmsvr0vrv4f0v9cnxzcnhpjnx2",
            "bc1qtr59h4kqargu5les2as8w2tumqreh58ew2ks5d",
            "bc1qle37gu93ja9csxndeu7q57g49jf5j4qsckw9g0",
            "bc1qy3lcdhn4crpev6ppejlt20kzhdg528f3t4wp27",
            "bc1qmx6deexe6r8vvn775e5hjur78g2q4n5fe0fzyn",
    };
    static final String[] PRIVKEY_WIF_BTC = {
            "KzsYHPmjK3VbtFvRL4PbaEAnUePcgQjJZC1B4RjcR1AXbZbC5Yfu",
            "L4cWMhJWvJwBFv1WrwfoTZYW4EDrT33KSoYtQEfnruzgNiupUNnq",
            "L4wZXSWJNr2fWbmf2Pfh1XyFew9tSog65nxUiA6767fhot4kGBeX",
            "L1Xb8kjGUgT322K2pGpUV3EYzzUucT9hsR34mAphtY1C8RwVqLWP",
            "KxF9SWjzRygz8DP32RdPhak19aMruJiFBYyavcMXRqQT3rr2n41w",
    };

    /* BTCTEST (also used for LTCTEST & XCPTEST, which share coin type 1) */
    static final String[] PUBKEY_BTCTEST = {
            "02860988886ecd730c1bd2f4d5d8a015492aa656f92d7dff09ef0f951677211a9a",
            "021a3d3978e501156197af1dc22ba09fd1597251de126c27448cf67a91064f3ede",
            "021f3d54734d7ac715fba56650d4a8fa12ab64939c8256729eba78ef2188fb4a5c",
            "0381872214b49468e718ba324bfb91c9a4a9b777339b1abdc8167030a1f33f916a",
            "02393edebdbe0c8886e1954c8791094393b4b160a96e32e7d799dfb7ea65dbc0d9",
    };
    static final String[] ADDRESS_LEGACY_BTCTEST = {
            "mpinvcSCUmojQm64yDJzqfXg5NSuDCNX5k",
            "mgAYcUwkxyXq1N5crAXEcikty78ey2vBvt",
            "mghgGiTJkeJmpUyGX42QtVRFN95wnoWHxV",
            "mzRVdJPhiVVFFcr7vZv5spjgTKdk1HUh5E",
            "mnn9xeNVv9Dix2v2QMnZwY2Qhpwb6HCr4R",
    };
    static final String[] ADDRESS_SEGWIT_BTCTEST = {
            "tb1qvnmy6khtlw06w49y2wzlmxn2xu09lyr7mfd794",
            "tb1qquwqn8mq444yzraecu9rwszws4zhguggjp3ldk",
            "tb1qpnl4xuh8v4s5wftjnmm9srvchya64kje0nepyr",
            "tb1qea3qcu5vmf6js2axyyzyzx9ef3x0k84g2vh43v",
            "tb1qf75dhyp0txvey0ll9gmc6kzxtmaqpzgwqzg7uz",
    };
    static final String[] PRIVKEY_WIF_BTCTEST = {
            "cRWGHNwyxeZaw4B2XxogNsmyiahQdUdjoRFtMWew7CQbebPqHuyd",
            "cPQjd8KzYQ8T9N6XTFrQgjp9gkHCVcwukgiw8xfNf4P1zsR74Qus",
            "cSdfwpP9djT15W6XdxdvWyjqGPk3C1a38hZucUTx1pjNMKjeaJFp",
            "cQLHgHLBxdtePeXeBUhYj5rPs3CVP7GKMdQ4JmMHvz6DfjkhtWUo",
            "cUpjKR37hJX3tnkKN8Ui1stxFKp1LK8BUf1bEevFy5fwhervSfBi",
    };

    /* LTC */
    static final String[] PUBKEY_LTC = {
            "031f0ed4b5cbd756626ec5f108f19b29fff7e93670e083a21cc32265ab4f4adece",
            "036f4cc9dbdc277c673870ad95d9e250119fe264f1905deab8de1ebdefc8ea45a4",
            "0294372dfc1cf72677bb3d86d93152261483d77870de046f3b859a97cfd9aca2c4",
            "02468a479f0e51a943e13173f827f7af43c51f864c8690a988f5b21975cd2d5bbe",
            "035f96c6cb9e2f0b372976c65766882a4d2d57954aa713ec9c35109fe58d23fdbc",
    };
    static final String[] ADDRESS_LEGACY_LTC = {
            "Lc7WkFsqnS6bYrCYqdWzt5CatK2EkmLB6K",
            "LavvkpWGy9RteJXrhLYpK7d13oV8bqsaey",
            "LM7ScWi4LPQ3au34wSMUJ5C1HfNd5A6NN3",
            "LM9xJXSELgWhqYuTTsmVMw4s4pMiuZKvvH",
            "LbASsVLAwx7eZZBU7XG46XdgwJ8Uty15fa",
    };
    static final String[] ADDRESS_SEGWIT_LTC = {
            "ltc1qh9p90tp8ffkyqsh6ptpa9hrnkgzsn0ul232qjm",
            "ltc1q43y6auh4q9wzq6n047tyucejt3sdruurtlqz7z",
            "ltc1qzj6sz4j35tjup6h6ytnx66utp54qzu0yyfeher",
            "ltc1qz5hfa7s5svk7zquc3zhcjkprkk6mtvyk5k74jl",
            "ltc1q4mvr4j9apt43gpytgwk92djw76c689z6qmn50e",
    };
    static final String[] PRIVKEY_WIF_LTC = {
            "T87X7mAR3JkHb9nBSiyZnNTcnTZ31XTLbbYuP2gMF1uCpyxa6kgN",
            "T4A27wtfX4W7cFVDiLhNAYyqco3SdqEDSYFvrET8PGxwP8qMdGmn",
            "T9ywaRQ5iWuTH366NNzS6nKNUDX3XQda3KBH9iBmLgDqCiLMH1NT",
            "T4EoWASE91AoELW3wCs4Z7wxFuaieHd99eHeSswBVyg21fRPrLpF",
            "T3svmt7X7wWsWTWQ6e7vSj7S14KVLQSkbsEZ7rrYNJdjLZSYpAAo",
    };

    /* LTCTEST (same keys as BTCTEST) */
    static final String[] ADDRESS_SEGWIT_LTCTEST = {
            "tltc1qvnmy6khtlw06w49y2wzlmxn2xu09lyr7zp0q4u",
            "tltc1qquwqn8mq444yzraecu9rwszws4zhguggtfnpal",
            "tltc1qpnl4xuh8v4s5wftjnmm9srvchya64kjekmml52",
            "tltc1qea3qcu5vmf6js2axyyzyzx9ef3x0k84gny4tp9",
            "tltc1qf75dhyp0txvey0ll9gmc6kzxtmaqpzgwe22qvt",
    };

    /* BCH (path m/0/*) */
    static final String[] PUBKEY_BCH = {
            "037f38c987d3e7ca6534b87588bc26c8c77739316c6af2b01ca5879c8d292472c2",
            "020649a9b59a1f986efed9320fe61f9b1ae217e35b37a71bfe166d695742987b6d",
            "0384c82879d42884922dfd3a9a1875730a6f642360fee8d28adb9f60c340713b85",
            "03c056e24e61951169616e6a8e019ff54849822d65b9d947361b6bf05203ad8d15",
            "02a931823029e1e305880d8bdc17f2a413c7c7cdc9eefffc4ace0777ef9d944977",
    };
    static final String[] ADDRESS_LEGACY_BCH = {
            "1Cr354KVskWhpFtEigcztiWaqvJi35Hrfn",
            "18y7pcKS3zLEByHtHVwqqep59jSUduumMR",
            "1KVpBLfQVQYQzm27uDvEL13HeDd9PWqzJ9",
            "1Cj1qKrMQDBLaBFsUqZtxUKRe8fckR9hBk",
            "1BBSYAkhtNCZWrSSBZaQzwseEbH9YYXjeL",
    };
    static final String[] ADDRESS_CASHADDR_BCH = {
            "bitcoincash:qzq77lnqvtk8afrsjr2qqcha39lhm4wcmq5e75xsrg",
            "bitcoincash:qptkth3meaxcwla5rgy4yxdqtck47ptt75k74y0y92",
            "bitcoincash:qr9w2jeq3qnn8k2ty7h8vvaa54lfelcjys05gczr8n",
            "bitcoincash:qzqfhr9dapn4qvgcufnulgu8lstk5zey2cdpepg8mg",
            "bitcoincash:qph640pg8rtdcf4wfys3ngfacpd9r7kt6vgrkfj820",
    };
    static final String[] PRIVKEY_WIF_BCH = {
            "L21CkkjKvmcWr5k9nEv5kYKD55QRFnU3D2q7T9QZQmqb1fz9N3e4",
            "Kz5BumoRSaovsfgPN13FsFp3jxjt2zmKtWfoWp2etF9Rp2adWVAP",
            "L2awcdfoabd43SzCJxcVmnSsewuazMFpfPhLMTreAqLuotcNBfYG",
            "L2dbLNq6UMCCvpCimw2nyCeAYG8HxAHmxAUTSQhzFemD1yzoaNiS",
            "L2RfBWTtdAGnTyLApZKWekgueyVh7twuJ9DxF3dkWYgj8WxC4JsW",
    };

    /* BCHTEST (same keys as BCH) */
    static final String[] ADDRESS_LEGACY_BCHTEST = {
            "msMzN7QUgmwxbNMrSFbNidiuhuuQzbLv6K",
            "moV57fQQs1mUy5mW14vDfa2Q1j3BaAaNWE",
            "mz1mUPkPJRyfmsVjcntc9vFcWDDrNdjqtn",
            "msEy8NwLDEcbMHjVCQYGnPXkW8GKeDjCvU",
            "mqhPqDqghPdpHxv3u8Ynps5y6asrU4RVTi",
    };
    static final String[] ADDRESS_CASHADDR_BCHTEST = {
            "bchtest:qzq77lnqvtk8afrsjr2qqcha39lhm4wcmqst6ny8y5",
            "bchtest:qptkth3meaxcwla5rgy4yxdqtck47ptt75jv3rdnzk",
            "bchtest:qr9w2jeq3qnn8k2ty7h8vvaa54lfelcjystxvlq5q0",
            "bchtest:qzqfhr9dapn4qvgcufnulgu8lstk5zey2cfnax2su5",
            "bchtest:qph640pg8rtdcf4wfys3ngfacpd9r7kt6vv3jwssdn",
    };
    static final String[] PRIVKEY_WIF_BCHTEST = {
            "cSNCDfjBMqJn1XDRAejD7rpGhJhpvEZjH4yaZZs4utVbGR1uWYw5",
            "cQSBNgoGseWC379ekQrPEaK7NC3HhSs1xYpGdEVAPMoS4mbP27uY",
            "cSww5Yff1fKKCtTThNRd96wwHBCzeoMWjRqoTtK9fwzv4dfNU4G5",
            "cSzaoHpwuQtU6FfzALqvLX9EAVRhccPU2CcvYqAVkmRDGj2YqviS",
            "cSneeRTk4Dy3dQoSCy8e25ByHCo6nM3bNBNRMU6G1fLjPG3EC5V2",
    };

    /* ETH (path m/0/*, same keys as BCH) - also valid for Polygon and EVM testnets */
    static final String[] PUBKEY_ETH = PUBKEY_BCH;
    static final String[] ADDRESS_ETH = {
            "0x83da5a7e7e02e88237a6af11598e8322a12ccda1",
            "0x3273bcf2b748ea196663ae900b7cf5c3a5b9b912",
            "0xb62990a87649b658d0f49158ed68ab8921442354",
            "0xe2a7152113cc018ec24f88c67fc8ce1c47b989a5",
            "0xc29bb40b3265fa3c0925b38916d1c0c92e54e5a4",
    };
    static final String[] ADDRESS_ETH_CHECKSUM = {
            "0x83da5A7e7E02E88237a6AF11598e8322a12CCda1",
            "0x3273BcF2b748Ea196663Ae900B7Cf5C3a5b9B912",
            "0xb62990a87649B658D0f49158ed68Ab8921442354",
            "0xe2A7152113cC018EC24F88C67fC8CE1C47B989a5",
            "0xc29bB40B3265Fa3c0925B38916D1C0C92e54E5A4",
    };
}
