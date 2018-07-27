package pl.drutex.restclient;

public class RestRequestHeader {
    public String key;
    public String value;

    public RestRequestHeader(String key, String value){
        this.key = key;
        this.value = value;
    }

    public RestRequestHeader(String key, Object value){
        this.key = key;
        this.value = String.valueOf(value);
    }
}
