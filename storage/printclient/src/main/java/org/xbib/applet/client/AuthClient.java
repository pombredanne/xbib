package org.xbib.applet.client;


import java.net.URL;

public class AuthClient extends AbstractClient {

    public AuthClient(URL documentBase, String path, String sessionID) {
        super(documentBase, path, sessionID);
    }

/*    public boolean auth(String name, char[] pass) throws IOException {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(Base64.encodeString("GET" + documentBase + name).getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] result = mac.doFinal(new String(pass).getBytes("UTF-8"));
            String signature = Base64.encodeBytes(result);
            URL url = new URL(documentBase, path + ";jessionid=" + sessionID
                    + "?name=" + name
                    + "&signature=" + signature);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            conn.connect();
            conn.disconnect();
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }*/

}
