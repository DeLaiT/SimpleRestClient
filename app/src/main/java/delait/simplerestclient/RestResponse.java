package delait.simplerestclient;

import android.util.Log;

import com.google.gson.Gson;

public class RestResponse {
    private String rawResponseBody;
    private Object objectResponseBody;
    public final int status;
    public final String statusMessage;
    Gson gson;

    RestResponse(String body, int status, String statusMessage, Gson gson) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.gson = gson;

        rawResponseBody = body;
    }

    public Object getResponseBody(Class objectType) {
        if (objectResponseBody == null) {
            try {
                objectResponseBody = gson.fromJson(rawResponseBody, objectType);
            } catch (Exception e) {
                Log.w("RestResponse", "Failed to get object from json. " + e.getMessage());
            }
        }

        return objectResponseBody;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }
}
