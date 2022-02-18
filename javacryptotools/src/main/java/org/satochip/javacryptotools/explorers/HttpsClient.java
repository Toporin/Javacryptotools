package org.satochip.javacryptotools.explorers;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.io.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

public class HttpsClient{
	
    protected static final Logger logger = Logger.getLogger("org.satochip.javacryptotools");
    
    public String https_url;
    
   // public static void main(String[] args)
   // {
        // new HttpsClient().testIt();
   // }
	
    public HttpsClient(String https_url){
        this.https_url= https_url;
    }
    
    public String request(){
        
        String content=null;
        URL url;
        try {

             url = new URL(this.https_url);
             HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                
             //dumpl all cert info
             print_https_cert(con);
                
             //dump all the content
             content= print_content(con);
			    
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
    
    
   // private void testIt(){

      // String https_url = "https://www.google.com/";
      // URL url;
      // try {

	     // url = new URL(https_url);
	     // HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
			
	     //dumpl all cert info
	     // print_https_cert(con);
			
	     //dump all the content
	     // print_content(con);
			
      // } catch (MalformedURLException e) {
	     // e.printStackTrace();
      // } catch (IOException e) {
	     // e.printStackTrace();
      // }

   // }
	
   private void print_https_cert(HttpsURLConnection con){
     
        if(con!=null){
                
            try {
                    
                logger.info("JAVACRYPTOTOOLS: HttpsClient: Response Code : " + con.getResponseCode());
                logger.info("JAVACRYPTOTOOLS: HttpsClient: Cipher Suite : " + con.getCipherSuite());
                logger.info("JAVACRYPTOTOOLS: HttpsClient: \n");
                            
                Certificate[] certs = con.getServerCertificates();
                for(Certificate cert : certs){
                    logger.info("JAVACRYPTOTOOLS: HttpsClient: Cert Type : " + cert.getType());
                    logger.info("JAVACRYPTOTOOLS: HttpsClient: Cert Hash Code : " + cert.hashCode());
                    logger.info("JAVACRYPTOTOOLS: HttpsClient: Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
                    logger.info("JAVACRYPTOTOOLS: HttpsClient: Cert Public Key Format : " + cert.getPublicKey().getFormat());
                }
                        
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }

        }
   }
	
   private String print_content(HttpsURLConnection con){
        String content=""; 
        
        if(con!=null){
            try {
               
               logger.info("JAVACRYPTOTOOLS: HttpsClient: ****** Content of the URL ********");			
               BufferedReader br = 
                new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
                        
               String input;
                        
               while ((input = br.readLine()) != null){
                  logger.info("JAVACRYPTOTOOLS: HttpsClient: " + input);
                  content+=input;
               }
               br.close();
               logger.info("JAVACRYPTOTOOLS: HttpsClient: ****** Content end reached ********");		
               
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
		return content;
   }
	
}
