package org.satochip.javacryptotools.explorers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.satochip.javacryptotools.coins.Asset;
import org.satochip.javacryptotools.coins.BaseCoin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// Bitcoin Cash explorer using public Electrum protocol servers (Fulcrum) over TLS.
// Balance is fetched with the blockchain.address.get_balance RPC.
// Servers are tried in random order until one answers.
// TLS certificates and hostnames are verified, so servers with self-signed certificates are not listed.
// Same approach as FulcrumExplorer in pybitcointools.

public class Fulcrum extends BaseExplorer implements Explorer{

    public static final Map<String, List<String>> SERVERS; // coin symbol => list of "host:port"
    static {
        Map<String, List<String>> servers = new HashMap<>();
        servers.put("BCH", Collections.unmodifiableList(Arrays.asList(
                "bch.imaginary.cash:50002",
                "electrum.imaginary.cash:50002",
                "cashnode.bch.ninja:50002",
                "bch.loping.net:50002",
                "blackie.c3-soft.com:50002",
                "bch.crypto.mldlabs.com:50002",
                "node.minisatoshi.cash:50002")));
        servers.put("BCHTEST", Collections.unmodifiableList(Arrays.asList( // chipnet
                "chipnet.bch.ninja:50002",
                "chipnet.imaginary.cash:50002",
                "chipnet.c3-soft.com:64002",
                "cbch.loping.net:62102")));
        SERVERS = Collections.unmodifiableMap(servers);
    }
    private static final int TIMEOUT_MS = 10000; // per server

    /** @deprecated the level is ignored: configure the logger from the application (see BaseCoin.LOGGER_NAME). */
    @Deprecated
    public Fulcrum(BaseCoin coin, Map<String, String> apikeys, Level logLevel){
        this(coin, apikeys);
    }
    public Fulcrum(BaseCoin coin, Map<String, String> apikeys){
        super(coin, apikeys);
    }

    public List<String> get_servers(){
        return SERVERS.getOrDefault(coin_symbol, Collections.<String>emptyList());
    }

    public String get_address_weburl(String addr){
        if (this.coin_symbol.equals("BCH")){
            return "https://blockchair.com/bitcoin-cash/address/" + addr;
        }
        return ""; // unsupported for testnet
    }

    public double get_balance(String addr){
        JSONObject result = (JSONObject) rpc_call("blockchain.address.get_balance", new JSONArray().put(addr));
        return parse_balance(result);
    }

    public List<Asset> get_asset_list(String addr){
        // unsupported
        return new ArrayList<Asset>();
    }

    public double parse_balance(JSONObject result){
        long confirmed = result.getLong("confirmed"); // in satoshi
        return (double) confirmed / 100000000;
    }

    // send a RPC to the first server that answers
    public Object rpc_call(String method, JSONArray params){
        List<String> servers = new ArrayList<>(get_servers());
        Collections.shuffle(servers);
        for (String server : servers){
            int sep = server.lastIndexOf(':');
            String host = server.substring(0, sep);
            int port = Integer.parseInt(server.substring(sep + 1));
            try {
                logFine("request: " + method + " to " + server);
                Object result = rpc_call_server(host, port, method, params);
                logResponse(String.valueOf(result));
                return result;
            } catch (IOException | RuntimeException e){ // includes socket, TLS and JSON errors
                logWarning("failed to recover data from " + server + ": " + e);
            }
        }
        throw new RuntimeException("Fulcrum: no server available for " + coin_symbol);
    }

    // open a TLS session with one server and send a single RPC after the version handshake
    private Object rpc_call_server(String host, int port, String method, JSONArray params) throws IOException {
        Socket rawSocket = new Socket();
        try {
            rawSocket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            rawSocket.setSoTimeout(TIMEOUT_MS);
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket socket = (SSLSocket) factory.createSocket(rawSocket, host, port, true)) {
                SSLParameters sslParams = socket.getSSLParameters();
                sslParams.setEndpointIdentificationAlgorithm("HTTPS"); // verify hostname
                socket.setSSLParameters(sslParams);
                socket.startHandshake();

                Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                Object[][] requests = {
                        {"server.version", new JSONArray().put("javacryptotools").put("1.4")},
                        {method, params}
                };
                Object result = null;
                for (int requestId = 0; requestId < requests.length; requestId++){
                    JSONObject payload = new JSONObject();
                    payload.put("jsonrpc", "2.0");
                    payload.put("id", requestId);
                    payload.put("method", requests[requestId][0]);
                    payload.put("params", requests[requestId][1]);
                    writer.write(payload.toString() + "\n");
                    writer.flush();

                    JSONObject response;
                    while (true){ // skip notifications, if any
                        String line = reader.readLine();
                        if (line == null){
                            throw new IOException("connection closed by server");
                        }
                        response = new JSONObject(line);
                        if (response.optInt("id", -1) == requestId){
                            break;
                        }
                    }
                    if (response.has("error") && !response.isNull("error")){
                        throw new RuntimeException("RPC error: " + response.get("error"));
                    }
                    result = response.opt("result");
                }
                return result;
            }
        } finally {
            rawSocket.close();
        }
    }

}
