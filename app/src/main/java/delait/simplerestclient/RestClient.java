package delait.simplerestclient;

import java.net.MalformedURLException;
import java.net.URL;

public class RestClient {
    URL url;
    public int timeout = 10*1000;

    void setURL(String url){
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public RestClient(String url){
        setURL(url);
    }

    public RestClient(String url, int timeout){
        setURL(url);
        this.timeout = timeout;
    }
}
