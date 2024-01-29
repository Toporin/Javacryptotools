package org.satochip.javacryptotools.coins;


public class Asset {

	public AssetType type = AssetType.Token;

	public String address = null;
	public String balance = null;
	public String decimals = "0";

	// Token info
	public String contract = null;
	public String name = null;
	public String symbol = null;
	public String description = null;

	// rate info
	public Boolean rateAvailable = false;
	public Double rate = 0.0;
	public String rateCurrency = "";
	public String valueInFirstCurrency = null; // typically, in native currency
	public String firstCurrency = null;
	public String valueInSecondCurrency = null;
	public String secondCurrency = null;

	// Links
	public String explorerLink = null;
	public String iconUrl = null;

	// NFT
	public String nftName = null;
	public String tokenid = null;
	public String nftDescription = null;
	public String nftExplorerLink = null;
	public String nftImageLink = null;
	public String nftImageSmallLink = null;

	public Asset(){
        //
    }

	@Override
	public String toString() {

		String str="ASSET:\n";
		str+= "name: "+this.name+"\n";
		str+= "type: "+this.type+"\n";
		str+= "symbol: "+this.symbol+"\n";
		str+= "address(owner): "+this.address+"\n";
		str+= "contract: "+this.contract+"\n";
		str+= "tokenid: "+this.tokenid+"\n";
		str+= "balance: "+this.balance+"\n";
		if (rateAvailable){
			str+= "rate: "+this.rate+" "+this.rateCurrency+"\n";
		}
		str+= "explorerLink: "+this.explorerLink+"\n";
		str+= "iconUrl: "+this.iconUrl+"\n";

		if (this.type == AssetType.NFT){
			str+= "nftName: "+this.nftName+"\n";
			str+= "tokenid: "+this.tokenid+"\n";
			str+= "nftDescription: "+this.nftDescription+"\n";
			str+= "nftImageLink: "+this.nftImageLink+"\n";
			str+= "nftImageSmallLink: "+this.nftImageSmallLink+"\n";
			str+= "nftExplorerLink: "+this.nftExplorerLink+"\n";
		}

		return str;
	}
}