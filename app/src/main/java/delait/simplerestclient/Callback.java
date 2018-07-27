package delait.simplerestclient;

public interface Callback<T> {
    void onSuccess(T responseBody, int code);
    void onFailure(String errorBody, int code);
}
