/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.satochip.javacryptotools;

import org.satochip.javacryptotools.explorers.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
//import com.google.common.base.Preconditions;

public abstract class BaseCoin {

    public String coin_symbol;
    public String display_name;
    public boolean enabled;
    public boolean segwit_supported;
    public int magicbyte;
    public int script_magicbyte;
    public String segwit_hrp;
    
    public boolean token_supported;
    public boolean nft_supported;
    public Explorer explorer;
    public NftExplorer nftExplorer;
    public PriceExplorer priceExplorer;
    public HashMap<String, String> apikeys= null;

    public boolean is_testnet;
    public boolean use_compressed_addr;
    public int wif_prefix;
    public int hd_path;
    public Map<String, Integer> wif_script_types;
    public Map<String, Integer> xprv_headers;
    public Map<String, Integer> xpub_headers;
    

    public String encodePrivkey(byte[] privkey){

        //Preconditions.checkArgument(privkey.length == 32, "Private keys must be 32 bytes");
        byte[] bytes;
        if (use_compressed_addr) { //"wif_compressed"
            // Keys that have compressed public components have an extra 1 byte on the end in dumped form.
            bytes = new byte[33];
            System.arraycopy(privkey, 0, bytes, 0, 32);
            bytes[32] = 1;
        } else { // "wif"
            bytes= privkey;
        }

        return Base58.encodeChecked(wif_prefix, bytes);
    }
    
    public boolean useCompressedAddress(){
        return use_compressed_addr;
    }
    
    public String pubToAddress(byte[] pubkey){
        // by default, return segwit address if supported
        if (isSegwitSupported()){
            return pubToSegwitAddress(pubkey);
        } else {
            return pubToLegacyAddress(pubkey);
        }
    }
    
    public String pubToLegacyAddress(byte[] pubkey){
    
        byte[] bytes;
        if (use_compressed_addr){
            
            if (pubkey.length==65){
                // compress pubkey
                bytes = new byte[33];
                int parity= pubkey[64]%2;
                System.arraycopy(pubkey, 0, bytes, 0, 33);
                bytes[0] = (parity==0)?  (byte)0x02 : (byte)0x03;
            } else {
                bytes= pubkey;
            }
            
        } else {
            if (pubkey.length==65){
                bytes= pubkey;
            } else {
                // should not happen!
                throw new AddressFormatException("Wrong pubkey size!");
            }
        }

        byte[] pubKeyHash = Utils.sha256hash160(bytes);
        return Base58.encodeChecked(magicbyte, pubKeyHash);
    } 
    
    
    public boolean isSegwitSupported(){
        return segwit_supported;
    }
    
    public String pubToSegwitAddress(byte[] pubkey)  throws AddressFormatException{
    
        byte[] bytes;
        if (use_compressed_addr){
            
            if (pubkey.length==65){
                // compress pubkey
                bytes = new byte[33];
                int parity= pubkey[64]%2;
                System.arraycopy(pubkey, 0, bytes, 0, 33);
                bytes[0] = (parity==0)?  (byte)0x02 : (byte)0x03;
            } else {
                bytes= pubkey;
            }
            
        } else {
            if (pubkey.length==65){
                bytes= pubkey;
            } else {
                // should not happen!
                throw new AddressFormatException("Wrong pubkey size!");
            }
        }
        
        int witnessVersion=0;
        byte[] pubKeyHash = Utils.sha256hash160(bytes);
        byte[] encoded= encode(witnessVersion, pubKeyHash);
        return Bech32.encode(Bech32.Encoding.BECH32, segwit_hrp, encoded);
    
    }

    // for segwit address
    private static byte[] encode(int witnessVersion, byte[] witnessProgram) throws AddressFormatException {
        byte[] convertedProgram = convertBits(witnessProgram, 0, witnessProgram.length, 8, 5, true);
        byte[] bytes = new byte[1 + convertedProgram.length];
        bytes[0] = (byte) (witnessVersion & 0xff);
        System.arraycopy(convertedProgram, 0, bytes, 1, convertedProgram.length);
        return bytes;
    }
    
    // for segwit address
    private static byte[] convertBits(final byte[] in, final int inStart, final int inLen, final int fromBits,
            final int toBits, final boolean pad) throws AddressFormatException {
        int acc = 0;
        int bits = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        final int maxv = (1 << toBits) - 1;
        final int max_acc = (1 << (fromBits + toBits - 1)) - 1;
        for (int i = 0; i < inLen; i++) {
            int value = in[i + inStart] & 0xff;
            if ((value >>> fromBits) != 0) {
                throw new AddressFormatException(
                        String.format("Input value '%X' exceeds '%d' bit size", value, fromBits));
            }
            acc = ((acc << fromBits) | value) & max_acc;
            bits += fromBits;
            while (bits >= toBits) {
                bits -= toBits;
                out.write((acc >>> bits) & maxv);
            }
        }
        if (pad) {
            if (bits > 0)
                out.write((acc << (toBits - bits)) & maxv);
        } else if (bits >= fromBits || ((acc << (toBits - bits)) & maxv) != 0) {
            throw new AddressFormatException("Could not convert bits, invalid padding");
        }
        return out.toByteArray();
    }
    
    /*
    *   EXPLORER METHODS
    */
    
    public double getBalance(String addr){
        
        System.out.println("In BaseCoin get_balance addr: " + addr);
        return explorer.get_balance(addr);
        
    }
    
    public String getAddressWeburl(String addr){
        System.out.println("In BaseCoin getAddressWeburl addr: " + addr);
        if (explorer!=null){
            return explorer.get_address_weburl(addr);
        } else {
            return "";
        }
    }
    
    public long getTokenBalance(String addr, String contract){
        if (explorer!=null){ 
            return explorer.get_token_balance(addr, contract);
        } else {
            return (long)-1;
        }
    }
    
    public HashMap<String, String> getTokenInfo(String contract){
        if (explorer!=null){ 
            return explorer.get_token_info(contract);
        } else {
            return new HashMap<String, String>();
        }
    }
    
    public HashMap<String, String> getNftInfo(String contract, String tokenID){
        if (nftExplorer!=null){
            return nftExplorer.get_nft_info(contract, tokenID);
        } else {
            return new HashMap<String, String>();
        }
    }
    
    public JSONObject getNftInfoJson(String contract, String tokenID){
        if (nftExplorer!=null){
            return nftExplorer.get_nft_info_json(contract, tokenID);
        } else {
            return new JSONObject();
        }
    }
    
    public double get_exchange_rate_between(String other_coin){
        if (priceExplorer!=null){
            return  priceExplorer.get_exchange_rate_between(other_coin);
        }
        else {
            return (double) -1;
        }
    }
    
    public double get_token_exchange_rate_between(String contract, String other_coin){
        if ((priceExplorer!=null) && (token_supported)){
            return  priceExplorer.get_token_exchange_rate_between(contract, other_coin);
        }
        else {
            return (double) -1;
        }
    }
    
    
}