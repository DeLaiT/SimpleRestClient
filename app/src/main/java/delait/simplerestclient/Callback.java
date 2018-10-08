package delait.simplerestclient;

import android.util.Log;

public interface Callback {
    void onSuccess(RestResponse restResponse);
    default void onFailure(RestErrorResponse restErrorResponse){
    }
}
