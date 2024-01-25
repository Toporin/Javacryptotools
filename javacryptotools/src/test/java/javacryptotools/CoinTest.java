package javacryptotools;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
 
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.satochip.javacryptotools.*;
import org.satochip.javacryptotools.coins.Base58;
import org.satochip.javacryptotools.coins.BaseCoin;
import org.satochip.javacryptotools.coins.Bitcoin;
import org.satochip.javacryptotools.coins.BitcoinCash;
import org.satochip.javacryptotools.coins.Litecoin;
import org.satochip.javacryptotools.coins.Ethereum;

import static org.satochip.javacryptotools.coins.Constants.*;

// test functions specifically used by SatodimeTool
// test vectors made with https://iancoleman.io/bip39/
// seed: tonight stem cause eyebrow estate smart duck wrong toe under job danger
// path: m/84'/0'/0'/0/* (using BIP84 path to get bech32 and base58 address)

class CoinTest {
    static ECParameterSpec SPEC = ECNamedCurveTable.getParameterSpec("secp256k1");
    public static String[] PUBKEY;
    public static String[] ADDRESS_LEGACY;
    public static String[] ADDRESS_SEGWIT;
    public static String[] PRIVKEY_WIF;
    
    /* BTC */
    public static final String[] PUBKEY_BTC= {
            "03b7b3957daedecee4488dcb0b8cf3f3372d64d5c559953d2a2539f55e6474c8ce",
            "03e5c1e865d21a239c6639e75586df1f0a5e59853694601e78dccb22481fad08c0",
            "03f21a3b7ff93a4396d886b04b045b8a4dfaa3e13ae169adf36a7390f65af964c0",
            "035f6cb6545543c6b69ba402e19362a71c9ff58a93f8c2d812e0a6c27c6304e5d2",
            "03e1d8b41fa14419293b29ad6f98d5bd1827ae21b5f1083a7cc001955db2ee628c",
    };
    
    public static final String[] ADDRESS_LEGACY_BTC= {
            "1Q6QXhpreAW8wDRwaL6jvdEcbbceFMw2mv",
            "1976pT5yu88hDa7HsQK76tpbyYtPTyN3cF",
            "1QC6JNGbXdmQFkBp69yFFXdYZKvXtfCeEx",
            "14Kz6dHFJjJNqj2hvQ84vSzYq78T9pmoWi",
            "1Lr8JCa936osnV288Jm3LYBKKvvvQJkdfy",
    };
    
    public static final String[] ADDRESS_SEGWIT_BTC= {
            "bc1ql4gf6wjve0enmmsvr0vrv4f0v9cnxzcnhpjnx2",
            "bc1qtr59h4kqargu5les2as8w2tumqreh58ew2ks5d",
            "bc1qle37gu93ja9csxndeu7q57g49jf5j4qsckw9g0",
            "bc1qy3lcdhn4crpev6ppejlt20kzhdg528f3t4wp27",
            "bc1qmx6deexe6r8vvn775e5hjur78g2q4n5fe0fzyn",
    };
    
    public static final String[] PRIVKEY_WIF_BTC= {
            "KzsYHPmjK3VbtFvRL4PbaEAnUePcgQjJZC1B4RjcR1AXbZbC5Yfu",
            "L4cWMhJWvJwBFv1WrwfoTZYW4EDrT33KSoYtQEfnruzgNiupUNnq",
            "L4wZXSWJNr2fWbmf2Pfh1XyFew9tSog65nxUiA6767fhot4kGBeX",
            "L1Xb8kjGUgT322K2pGpUV3EYzzUucT9hsR34mAphtY1C8RwVqLWP",
            "KxF9SWjzRygz8DP32RdPhak19aMruJiFBYyavcMXRqQT3rr2n41w",
    };
        
    @Test
    void test_btc(){
        boolean isTestnet= false;
        
        PUBKEY=PUBKEY_BTC;
        ADDRESS_LEGACY= ADDRESS_LEGACY_BTC;
        ADDRESS_SEGWIT= ADDRESS_SEGWIT_BTC;
        PRIVKEY_WIF= PRIVKEY_WIF_BTC;
        
        Bitcoin coin = new Bitcoin(isTestnet, null);
        System.out.println(coin.display_name+ " - test_address - START!");        
        assertEquals("Bitcoin", coin.display_name);
        assertEquals("BTC", coin.coin_symbol);
        assertEquals( true, coin.segwit_supported);
        assertEquals( true, coin.use_compressed_addr);
        test_coin(coin);
        System.out.println(coin.display_name+ " - test_address - FINISH!");
        System.out.println("===============================\n\n\n");
    }
    
    /* BTCTEST */
    public static final String[]  PUBKEY_BTCTEST={
        "02860988886ecd730c1bd2f4d5d8a015492aa656f92d7dff09ef0f951677211a9a",
        "021a3d3978e501156197af1dc22ba09fd1597251de126c27448cf67a91064f3ede",
        "021f3d54734d7ac715fba56650d4a8fa12ab64939c8256729eba78ef2188fb4a5c",
        "0381872214b49468e718ba324bfb91c9a4a9b777339b1abdc8167030a1f33f916a",
        "02393edebdbe0c8886e1954c8791094393b4b160a96e32e7d799dfb7ea65dbc0d9",
    };

    public static final String[]  ADDRESS_LEGACY_BTCTEST= {
         "mpinvcSCUmojQm64yDJzqfXg5NSuDCNX5k",
        "mgAYcUwkxyXq1N5crAXEcikty78ey2vBvt",
        "mghgGiTJkeJmpUyGX42QtVRFN95wnoWHxV",
        "mzRVdJPhiVVFFcr7vZv5spjgTKdk1HUh5E",
        "mnn9xeNVv9Dix2v2QMnZwY2Qhpwb6HCr4R",
    };

    public static final String[]  ADDRESS_SEGWIT_BTCTEST={
        "tb1qvnmy6khtlw06w49y2wzlmxn2xu09lyr7mfd794",
        "tb1qquwqn8mq444yzraecu9rwszws4zhguggjp3ldk",
        "tb1qpnl4xuh8v4s5wftjnmm9srvchya64kje0nepyr",
        "tb1qea3qcu5vmf6js2axyyzyzx9ef3x0k84g2vh43v",
        "tb1qf75dhyp0txvey0ll9gmc6kzxtmaqpzgwqzg7uz",
    };

    public static final String[]  PRIVKEY_WIF_BTCTEST={
        "cRWGHNwyxeZaw4B2XxogNsmyiahQdUdjoRFtMWew7CQbebPqHuyd",
        "cPQjd8KzYQ8T9N6XTFrQgjp9gkHCVcwukgiw8xfNf4P1zsR74Qus",
        "cSdfwpP9djT15W6XdxdvWyjqGPk3C1a38hZucUTx1pjNMKjeaJFp",
        "cQLHgHLBxdtePeXeBUhYj5rPs3CVP7GKMdQ4JmMHvz6DfjkhtWUo",
        "cUpjKR37hJX3tnkKN8Ui1stxFKp1LK8BUf1bEevFy5fwhervSfBi",
    };
    
    @Test
    void test_btctest(){
        boolean isTestnet= true;
        
        PUBKEY=PUBKEY_BTCTEST;
        ADDRESS_LEGACY= ADDRESS_LEGACY_BTCTEST;
        ADDRESS_SEGWIT= ADDRESS_SEGWIT_BTCTEST;
        PRIVKEY_WIF= PRIVKEY_WIF_BTCTEST;
        
        BaseCoin coin = new Bitcoin(isTestnet, null);
        System.out.println(coin.display_name+ " - test_address - START!");        
        assertEquals("Bitcoin Testnet", coin.display_name);
        assertEquals("BTCTEST", coin.coin_symbol);
        assertEquals( true, coin.segwit_supported);
        assertEquals( true, coin.use_compressed_addr);
        test_coin(coin);
        System.out.println(coin.display_name+ " - test_address - FINISH!");
        System.out.println("===============================\n\n\n");
    }
    
    /* LTC */
    public static final String[] PUBKEY_LTC={
    "031f0ed4b5cbd756626ec5f108f19b29fff7e93670e083a21cc32265ab4f4adece",
    "036f4cc9dbdc277c673870ad95d9e250119fe264f1905deab8de1ebdefc8ea45a4",
    "0294372dfc1cf72677bb3d86d93152261483d77870de046f3b859a97cfd9aca2c4",
    "02468a479f0e51a943e13173f827f7af43c51f864c8690a988f5b21975cd2d5bbe",
    "035f96c6cb9e2f0b372976c65766882a4d2d57954aa713ec9c35109fe58d23fdbc",
    };
    public static final String[] ADDRESS_LEGACY_LTC={
    "Lc7WkFsqnS6bYrCYqdWzt5CatK2EkmLB6K",
    "LavvkpWGy9RteJXrhLYpK7d13oV8bqsaey",
    "LM7ScWi4LPQ3au34wSMUJ5C1HfNd5A6NN3",
    "LM9xJXSELgWhqYuTTsmVMw4s4pMiuZKvvH",
    "LbASsVLAwx7eZZBU7XG46XdgwJ8Uty15fa",
    };
    public static final String[] ADDRESS_SEGWIT_LTC={
    "ltc1qh9p90tp8ffkyqsh6ptpa9hrnkgzsn0ul232qjm",
    "ltc1q43y6auh4q9wzq6n047tyucejt3sdruurtlqz7z",
    "ltc1qzj6sz4j35tjup6h6ytnx66utp54qzu0yyfeher",
    "ltc1qz5hfa7s5svk7zquc3zhcjkprkk6mtvyk5k74jl",
    "ltc1q4mvr4j9apt43gpytgwk92djw76c689z6qmn50e",
    };
    public static final String[] PRIVKEY_WIF_LTC={
    "T87X7mAR3JkHb9nBSiyZnNTcnTZ31XTLbbYuP2gMF1uCpyxa6kgN",
    "T4A27wtfX4W7cFVDiLhNAYyqco3SdqEDSYFvrET8PGxwP8qMdGmn",
    "T9ywaRQ5iWuTH366NNzS6nKNUDX3XQda3KBH9iBmLgDqCiLMH1NT",
    "T4EoWASE91AoELW3wCs4Z7wxFuaieHd99eHeSswBVyg21fRPrLpF",
    "T3svmt7X7wWsWTWQ6e7vSj7S14KVLQSkbsEZ7rrYNJdjLZSYpAAo",
    };
    
    @Test
    void test_ltc(){
        boolean isTestnet= false;
        
        PUBKEY=PUBKEY_LTC;
        ADDRESS_LEGACY= ADDRESS_LEGACY_LTC;
        ADDRESS_SEGWIT= ADDRESS_SEGWIT_LTC;
        PRIVKEY_WIF= PRIVKEY_WIF_LTC;
        
        BaseCoin coin = new Litecoin(isTestnet, null);
        System.out.println(coin.display_name+ " - test_address - START!");        
        assertEquals("Litecoin", coin.display_name);
        assertEquals("LTC", coin.coin_symbol);
        assertEquals( true, coin.segwit_supported);
        assertEquals( true, coin.use_compressed_addr);
        test_coin(coin);
        System.out.println(coin.display_name+ " - test_address - FINISH!");
        System.out.println("===============================\n\n\n");
    }
    
    /* LTCTEST */
    public static final String[] PUBKEY_LTCTEST={
        "02860988886ecd730c1bd2f4d5d8a015492aa656f92d7dff09ef0f951677211a9a",
        "021a3d3978e501156197af1dc22ba09fd1597251de126c27448cf67a91064f3ede",
        "021f3d54734d7ac715fba56650d4a8fa12ab64939c8256729eba78ef2188fb4a5c",
        "0381872214b49468e718ba324bfb91c9a4a9b777339b1abdc8167030a1f33f916a",
        "02393edebdbe0c8886e1954c8791094393b4b160a96e32e7d799dfb7ea65dbc0d9",
    };
    public static final String[] ADDRESS_LEGACY_LTCTEST={
        "mpinvcSCUmojQm64yDJzqfXg5NSuDCNX5k",
        "mgAYcUwkxyXq1N5crAXEcikty78ey2vBvt",
        "mghgGiTJkeJmpUyGX42QtVRFN95wnoWHxV",
        "mzRVdJPhiVVFFcr7vZv5spjgTKdk1HUh5E",
        "mnn9xeNVv9Dix2v2QMnZwY2Qhpwb6HCr4R",
    };
    public static final String[] ADDRESS_SEGWIT_LTCTEST={
        "tltc1qvnmy6khtlw06w49y2wzlmxn2xu09lyr7zp0q4u",
        "tltc1qquwqn8mq444yzraecu9rwszws4zhguggtfnpal",
        "tltc1qpnl4xuh8v4s5wftjnmm9srvchya64kjekmml52",
        "tltc1qea3qcu5vmf6js2axyyzyzx9ef3x0k84gny4tp9",
        "tltc1qf75dhyp0txvey0ll9gmc6kzxtmaqpzgwe22qvt",
    };
    public static final String[] PRIVKEY_WIF_LTCTEST={
        "cRWGHNwyxeZaw4B2XxogNsmyiahQdUdjoRFtMWew7CQbebPqHuyd",
        "cPQjd8KzYQ8T9N6XTFrQgjp9gkHCVcwukgiw8xfNf4P1zsR74Qus",
        "cSdfwpP9djT15W6XdxdvWyjqGPk3C1a38hZucUTx1pjNMKjeaJFp",
        "cQLHgHLBxdtePeXeBUhYj5rPs3CVP7GKMdQ4JmMHvz6DfjkhtWUo",
        "cUpjKR37hJX3tnkKN8Ui1stxFKp1LK8BUf1bEevFy5fwhervSfBi",
    };
    
    @Test
    void test_ltctest(){
        boolean isTestnet= true;
        
        PUBKEY=PUBKEY_LTCTEST;
        ADDRESS_LEGACY= ADDRESS_LEGACY_LTCTEST;
        ADDRESS_SEGWIT= ADDRESS_SEGWIT_LTCTEST;
        PRIVKEY_WIF= PRIVKEY_WIF_LTCTEST;
        
        BaseCoin coin = new Litecoin(isTestnet, null);
        System.out.println(coin.display_name+ " - test_address - START!");        
        assertEquals("Litecoin Testnet", coin.display_name);
        assertEquals("LTCTEST", coin.coin_symbol);
        assertEquals( true, coin.segwit_supported);
        assertEquals( true, coin.use_compressed_addr);
        test_coin(coin);
        System.out.println(coin.display_name+ " - test_address - FINISH!");
        System.out.println("===============================\n\n\n");
    }
    
    /* BCH */
    // path: m/0/*
    public static final String[] PUBKEY_BCH={
        "037f38c987d3e7ca6534b87588bc26c8c77739316c6af2b01ca5879c8d292472c2",
        "020649a9b59a1f986efed9320fe61f9b1ae217e35b37a71bfe166d695742987b6d",
        "0384c82879d42884922dfd3a9a1875730a6f642360fee8d28adb9f60c340713b85",
        "03c056e24e61951169616e6a8e019ff54849822d65b9d947361b6bf05203ad8d15",
        "02a931823029e1e305880d8bdc17f2a413c7c7cdc9eefffc4ace0777ef9d944977",
    };
    public static final String[] ADDRESS_BCH={
        "1Cr354KVskWhpFtEigcztiWaqvJi35Hrfn",
        "18y7pcKS3zLEByHtHVwqqep59jSUduumMR",
        "1KVpBLfQVQYQzm27uDvEL13HeDd9PWqzJ9",
        "1Cj1qKrMQDBLaBFsUqZtxUKRe8fckR9hBk",
        "1BBSYAkhtNCZWrSSBZaQzwseEbH9YYXjeL",
    };
    // TODO: move to cashaddress
    public static final String[] ADDRESS_BCH_CASHADDR={
        "bitcoincash:qzq77lnqvtk8afrsjr2qqcha39lhm4wcmq5e75xsrg",
        "bitcoincash:qptkth3meaxcwla5rgy4yxdqtck47ptt75k74y0y92",
        "bitcoincash:qr9w2jeq3qnn8k2ty7h8vvaa54lfelcjys05gczr8n",
        "bitcoincash:qzqfhr9dapn4qvgcufnulgu8lstk5zey2cdpepg8mg",
        "bitcoincash:qph640pg8rtdcf4wfys3ngfacpd9r7kt6vgrkfj820",
    };
    public static final String[] ADDRESS_SEGWIT_BCH={};
    public static final String[] PRIVKEY_WIF_BCH={
        "L21CkkjKvmcWr5k9nEv5kYKD55QRFnU3D2q7T9QZQmqb1fz9N3e4",
        "Kz5BumoRSaovsfgPN13FsFp3jxjt2zmKtWfoWp2etF9Rp2adWVAP",
        "L2awcdfoabd43SzCJxcVmnSsewuazMFpfPhLMTreAqLuotcNBfYG",
        "L2dbLNq6UMCCvpCimw2nyCeAYG8HxAHmxAUTSQhzFemD1yzoaNiS",
        "L2RfBWTtdAGnTyLApZKWekgueyVh7twuJ9DxF3dkWYgj8WxC4JsW",
    };
    
    @Test
    void test_bch(){
        boolean isTestnet= false;
        
        PUBKEY=PUBKEY_BCH;
        ADDRESS_LEGACY= ADDRESS_BCH_CASHADDR;
        ADDRESS_SEGWIT= ADDRESS_SEGWIT_BCH;
        PRIVKEY_WIF= PRIVKEY_WIF_BCH;
        
        BaseCoin coin = new BitcoinCash(isTestnet, null, Level.WARNING);
        System.out.println(coin.display_name+ " - test_address - START!");        
        assertEquals("Bitcoin Cash", coin.display_name);
        assertEquals("BCH", coin.coin_symbol);
        assertEquals( false, coin.segwit_supported);
        assertEquals( true, coin.use_compressed_addr);
        test_coin(coin);
        System.out.println(coin.display_name+ " - test_address - FINISH!");
        System.out.println("===============================\n\n\n");
    }
    
    /* ETH*/
    public static final String[] PUBKEY_ETH={
        "037f38c987d3e7ca6534b87588bc26c8c77739316c6af2b01ca5879c8d292472c2",
        "020649a9b59a1f986efed9320fe61f9b1ae217e35b37a71bfe166d695742987b6d",
        "0384c82879d42884922dfd3a9a1875730a6f642360fee8d28adb9f60c340713b85",
        "03c056e24e61951169616e6a8e019ff54849822d65b9d947361b6bf05203ad8d15",
        "02a931823029e1e305880d8bdc17f2a413c7c7cdc9eefffc4ace0777ef9d944977",
    };
    public static final String[] ADDRESS_ETH_CHECKSUM={
        "0x83da5A7e7E02E88237a6AF11598e8322a12CCda1",
        "0x3273BcF2b748Ea196663Ae900B7Cf5C3a5b9B912",
        "0xb62990a87649B658D0f49158ed68Ab8921442354",
        "0xe2A7152113cC018EC24F88C67fC8CE1C47B989a5",
        "0xc29bB40B3265Fa3c0925B38916D1C0C92e54E5A4",
    };
    public static final String[] ADDRESS_ETH={
        "0x83da5a7e7e02e88237a6af11598e8322a12ccda1",
        "0x3273bcf2b748ea196663ae900b7cf5c3a5b9b912",
        "0xb62990a87649b658d0f49158ed68ab8921442354",
        "0xe2a7152113cc018ec24f88c67fc8ce1c47b989a5",
        "0xc29bb40b3265fa3c0925b38916d1c0c92e54e5a4",
    };
    public static final String[] ADDRESS_SEGWIT_ETH={};
    public static final String[] PRIVKEY_ETH={
        "0x8ec0b10753bb2c7c8462f3328afb86bb45f2c766f8c2e2bc4fd93b13ae4732ea",
        "0x5520cfb01a87374da0989fcbe4d7b5fe99f262bb93ce8adfff4273a6e62ff127",
        "0xa01bfb418815a1c8063caa9503b4b6e60897b4c5d1deefc2fd61fc6ac0d61c6a",
        "0xa179055a8bba685407579bf8a1465bb52323095a5dde5ce6a6ff00720afeea27",
        "0x9b55672fd34e5d0b597fe801910c23186e6f8b03443ddeb5b6a4652a089637ff",
    };
    public static final String[] PRIVKEY_WIF_ETH={}; //not used in ETH?
        
    @Test
    void test_eth(){
        boolean isTestnet= false;
        
        PUBKEY=PUBKEY_ETH;
        ADDRESS_LEGACY= ADDRESS_ETH;
        ADDRESS_SEGWIT= ADDRESS_SEGWIT_ETH;
        PRIVKEY_WIF= PRIVKEY_WIF_ETH;
        
        BaseCoin coin = new Ethereum(isTestnet, null, Level.WARNING);
        System.out.println(coin.display_name+ " - test_address - START!");        
        assertEquals("Ethereum", coin.display_name);
        assertEquals("ETH", coin.coin_symbol);
        assertEquals( false, coin.segwit_supported);
        assertEquals( false, coin.use_compressed_addr);
        test_coin(coin);
        System.out.println(coin.display_name+ " - test_address - FINISH!");
        System.out.println("===============================\n\n\n");
    }
    
    /************************************
                    SUBFUNCTIONS
    ************************************/
    
    void test_coin(BaseCoin coin){
        
        for (int i=0; i<PUBKEY.length; i++){
            String pubkey_hex= PUBKEY[i];
            byte[] pubkey_bytes= Hex.decode(pubkey_hex);
            
            // uncompress pubkey if necessary
            if (!coin.use_compressed_addr && pubkey_bytes.length<65){
                pubkey_bytes= compressedToUncompressed(pubkey_bytes);
                System.out.println("pubkey_bytes (uncompressed)= " + Hex.toHexString(pubkey_bytes)); 
            }
            
            String addr= coin.pubToAddress(pubkey_bytes);
            if (coin.segwit_supported){
                assertEquals(addr, ADDRESS_SEGWIT[i]);
            } else {
                assertEquals(addr, ADDRESS_LEGACY[i]);
            }
            
            // String addrLegacy= coin.pubToLegacyAddress(pubkey_bytes);
            // assertEquals(addrLegacy, ADDRESS_LEGACY[i]);
            if (PRIVKEY_WIF.length>i){
                byte[] privkey_bytes= wif2priv(PRIVKEY_WIF[i]);
                String privkey_wif= coin.encodePrivkey(privkey_bytes);
                System.out.println("privkey_wif= " + privkey_wif); 
                System.out.println("PRIVKEY_WIF[i]= " + PRIVKEY_WIF[i]); 
                assertEquals(privkey_wif, PRIVKEY_WIF[i]);
            } else {
                System.out.println("WIF not supported => skipping test_WIF"); 
            }
            
            String url= coin.getAddressWeburl(addr);
            System.out.println("URL= " + url); 
        }
    }
    
    // convert wif String to byte[]
    public byte[] wif2priv(String private_key_WIF){
        
        byte[] decoded;
        try{
            decoded= Base58.decode(private_key_WIF);
            System.out.println("privkey decoded= " + Hex.toHexString(decoded));
        } catch (Exception e){
            System.out.println("Exception in wif2priv(): "+ e);
            decoded= new byte[0];
        }
        
        byte[] privkey= new byte[decoded.length-6];
        System.arraycopy(decoded, 1, privkey, 0, privkey.length); // discard 1st byte & last 5 bytes
        System.out.println("trimmed decoded= " + Hex.toHexString(privkey));
        
        if (privkey.length== 33){
            byte[] privkey_new= new byte[32];
            System.arraycopy(privkey, 1, privkey_new, 0, privkey_new.length);
            privkey=privkey_new;
        }
        
        return privkey;
    }
    
    // uncompress public key
    static byte[] compressedToUncompressed(byte[] compKey) {
        ECPoint point = SPEC.getCurve().decodePoint(compKey);
        byte[] x = point.getXCoord().getEncoded();
        byte[] y = point.getYCoord().getEncoded();
        byte[] compByte= new byte[] {0x04};

        // concat 0x04, x, and y, make sure x and y has 32-bytes:
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        byte[] uncompressed= new byte[65];
        try{
            outputStream.write( compByte );
            outputStream.write( x );
            outputStream.write( y );
            uncompressed= outputStream.toByteArray( );
        } catch (Exception e){
            System.out.println("Exception during pubkey uncompression: " + e);
        }
        return uncompressed;
    }
    
    
    
    
    
}