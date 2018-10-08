package delait.simplerestclient;

import android.util.Log;

public interface ByteCallback {
    void onSuccess(byte[] restResponse, int status, String statusMessage);
    default void onSuccess(byte[] restResponse, int status, String statusMessage, long executionTime){
        onSuccess(restResponse, status, statusMessage);
    }
    default void onFailure(RestErrorResponse restErrorResponse){
    }
}
