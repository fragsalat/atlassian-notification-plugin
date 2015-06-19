package eu.stagetwo.jenkins.plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * Helper class to HTTP Post requests
 */
public class Request {

    /**
     * Send request to atlassian product instance
     *
     * @param url
     * @param data
     * @param timeout
     * @param isJson
     * @throws IOException
     */
    public static void send(String url, byte[] data, int timeout) throws IOException {
        URL targetUrl = new URL(url);
        if (!targetUrl.getProtocol().startsWith("http")) {
            throw new IllegalArgumentException("Not an http(s) url: " + url);
        }
        // Verifying if the HTTP_PROXY is available
        final String httpProxyUrl = System.getenv().get("http_proxy");
        URL proxyUrl = null;
        if (httpProxyUrl != null && httpProxyUrl.length() > 0) {
            proxyUrl = new URL(httpProxyUrl);
            if (!proxyUrl.getProtocol().startsWith("http")) {
                throw new IllegalArgumentException("Not an http(s) url: " + httpProxyUrl);
            }
        }

        HttpURLConnection connection = null;
        if (proxyUrl == null) {
            connection = (HttpURLConnection) targetUrl.openConnection();
        }
        else {
            // Proxy connection to the address provided
            final int proxyPort = proxyUrl.getPort() > 0 ? proxyUrl.getPort() : 80;
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyPort));
            connection = (HttpURLConnection) targetUrl.openConnection(proxy);
        }

        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setFixedLengthStreamingMode(data.length);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.connect();

        try {
            OutputStream output = connection.getOutputStream();
            try {
                output.write(data);
                output.flush();
            }
            finally {
                output.close();
            }
        }
        finally {
            // Follow an HTTP Temporary Redirect if we get one,
            //
            // NB: Normally using the HttpURLConnection interface, we'd call
            // connection.setInstanceFollowRedirects(true) to enable 307 redirect following but
            // since we have the connection in streaming mode this does not work and we instead
            // re-direct manually.
            if (connection.getResponseCode() == 307) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                send(location, data, timeout);
            }
            else {
                connection.disconnect();
            }
        }
    }
}
