package delait.simplerestclient;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URL;

public class RestClient {
    URL url;
    public int timeout = 10*1000;
    public Gson gson = new Gson();

    void setURL(String url){
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private RestClient(String url){
        setURL(url);
    }


    static class Builder{
        RestClient restClient;

        public Builder(String url){
            restClient = new RestClient(url);
        }

        public Builder setTimeout(int timeout){
            restClient.timeout = timeout;
            return this;
        }

        public Builder setGson(Gson gson){
            restClient.gson = gson;
            return this;
        }

        public RestClient build(){
            return restClient;
        }
    }
}
