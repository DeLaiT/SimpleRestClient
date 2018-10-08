package delait.simplerestclient;

import android.util.Log;

import com.google.gson.Gson;

public class RestResponse {
    private String rawResponseBody;
    private Object objectResponseBody;
    public final int status;
    public final String statusMessage;
    private Gson gson;
    public long executionTime;

    public RestResponse(String body, int status, String statusMessage, Gson gson, long executionTime) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.gson = gson;

        rawResponseBody = body;
        this.executionTime = executionTime;
    }

    public Object getResponseBody(Class objectType) {
        if (objectResponseBody == null) {
            try {
                objectResponseBody = gson.fromJson(rawResponseBody, objectType);
            } catch (Exception e) {
                Log.e("RestResponse", "Failed to get object from json. ");
                e.printStackTrace();
            }
        }

        return objectResponseBody;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }

    public String toString(){
        return status + " " + statusMessage + " " + executionTime + " ms";
    }
}
