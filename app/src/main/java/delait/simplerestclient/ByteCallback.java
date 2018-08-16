package delait.simplerestclient;

public interface ByteCallback {
    void onSuccess(byte[] restResponse, int status, String statusMessage);
    void onFailure(RestErrorResponse restErrorResponse);
}
