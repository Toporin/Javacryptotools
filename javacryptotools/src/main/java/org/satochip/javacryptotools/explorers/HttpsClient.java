package org.satochip.javacryptotools.explorers;

import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

public class HttpsClient{

    protected static final Logger logger = BaseExplorer.logger;

    public String https_url;
    public HashMap<String,String> headers = null;
    public int responseCode = -1; // HTTP status code of the last request, or -1 if unavailable

    public HttpsClient(String https_url){
        this.https_url= https_url;
    }
    public HttpsClient(String https_url, HashMap<String,String> headers){
        this.https_url= https_url;
        this.headers= headers;
    }

    // send a GET request, returns the response content, or an empty string (or null) on failure
    public String request(){
        HttpsURLConnection con = null;
        try {
            URL url = new URL(this.https_url);
            con = (HttpsURLConnection)url.openConnection();
            setHeaders(con);
            String content = read_content(con);
            if (responseCode == -1){
                responseCode = getResponseCode(con);
            }
            return content;
        } catch (IOException e) {
            logFailure(con, e);
            return null;
        }
    }

    // send a POST request with a json body, returns the response content, or an empty string (or null) on failure
    public String post(String json_body){
        HttpsURLConnection con = null;
        try {
            URL url = new URL(this.https_url);
            con = (HttpsURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            setHeaders(con);
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json_body.getBytes(StandardCharsets.UTF_8));
            }
            String content = read_content(con);
            if (responseCode == -1){
                responseCode = getResponseCode(con);
            }
            return content;
        } catch (IOException e) {
            logFailure(con, e);
            return null;
        }
    }

    // header values (e.g. API keys) must not be logged
    private void setHeaders(HttpsURLConnection con){
        if (headers != null){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    // returns an empty string if the response content cannot be read (e.g. HTTP error)
    private String read_content(HttpsURLConnection con) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            String input;
            while ((input = br.readLine()) != null){
                content.append(input);
            }
        } catch (IOException e) {
            logFailure(con, e);
        }
        return content.toString();
    }

    // returns -1 if there is no response (e.g. connection failure)
    private static int getResponseCode(HttpsURLConnection con){
        if (con == null){
            return -1;
        }
        try {
            return con.getResponseCode();
        } catch (IOException e) {
            return -1;
        }
    }

    // Only the host is logged: urls may contain API keys, and so may exception messages (which include the url).
    private void logFailure(HttpsURLConnection con, IOException e){
        String host;
        try {
            host = new URL(this.https_url).getHost();
        } catch (IOException ex) {
            host = "(invalid url)";
        }
        if (responseCode == -1){
            responseCode = getResponseCode(con);
        }
        logger.warning("HttpsClient: request to " + host + " failed"
                + (responseCode != -1 ? " with HTTP code " + responseCode : "")
                + ": " + e.getClass().getSimpleName());
    }

}
