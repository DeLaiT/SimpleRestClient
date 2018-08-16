package delait.simplerestclient;

public interface Callback {
    void onSuccess(RestResponse restResponse);
    void onFailure(RestErrorResponse restErrorResponse);
}
