package delait.simplerestclient;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RestRequest {
    public RestClient client;
    public RequestType type = RequestType.GET;
    private Object requestBody = null;
    private List<RestRequestHeader> headers = new ArrayList<>();
    private URL url;

    private static final String TAG = "RestRequest";

    private RestRequest() { }

    public void executeAsyncRaw(final Callback<String> callback){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    setupConnection(connection);
                    tryToSendRequestBody(connection);
                    handleRawResponse(connection, callback);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    callback.onFailure(e.getMessage(), 0);
                }
            }
        });
    }

    public void executeAsync(final Class objectClass, final Callback<Object> callback){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    setupConnection(connection);
                    tryToSendRequestBody(connection);
                    handleObjectResponse(connection, callback, objectClass);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    callback.onFailure(e.getMessage(), 0);
                }
            }
        });
    }

    private void setupConnection(HttpURLConnection connection) throws Exception {
        connection.setRequestMethod(getRequestTypeFromEnum(type));
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setReadTimeout(client.timeout);
        connection.setConnectTimeout(client.timeout);

        for(int i = 0; i < headers.size(); i++){
            RestRequestHeader header = headers.get(i);
            connection.addRequestProperty(header.key, header.value);
        }
    }

    private void tryToSendRequestBody(HttpURLConnection connection) throws Exception {
        if (requestBody != null) {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            if(requestBody instanceof String)
                wr.writeBytes((String)requestBody);
            else
                wr.writeBytes(new Gson().toJson(requestBody));
            wr.close();
        }
    }

    private void handleRawResponse(HttpURLConnection connection, Callback<String> callback) throws Exception {
        String response = readResponse(connection);

        if(connection.getResponseCode() < 300){
            Log.i(TAG, getRequestTypeFromEnum(type) + " result: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            callback.onSuccess(response, connection.getResponseCode());
        }
        else {
            Log.w(TAG, getRequestTypeFromEnum(type) + " result: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            callback.onFailure(response, connection.getResponseCode());
        }
    }

    private void handleObjectResponse(HttpURLConnection connection, Callback<Object> callback, Class objectClass) throws Exception {
        String response = readResponse(connection);

        if(connection.getResponseCode() < 300){
            Log.i(TAG, getRequestTypeFromEnum(type) + " result: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            callback.onSuccess(new Gson().fromJson(response, objectClass), connection.getResponseCode());
        }
        else {
            Log.w(TAG, getRequestTypeFromEnum(type) + " result: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            callback.onFailure(response, connection.getResponseCode());
        }
    }

    private String readResponse(HttpURLConnection connection) throws Exception{
        InputStream is = connection.getResponseCode() < 300 ? connection.getInputStream() : connection.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        reader.close();

        return response.toString();
    }

    private String getRequestTypeFromEnum(RequestType requestType){
        switch (requestType){
            case GET:
                return "GET";
            case POST:
                return "POST";
            case DELETE:
                return "DELETE";
            case PUT:
                return "PUT";
            default:
                return "GET";
        }
    }

    public static class Builder{
        private RestRequest request = new RestRequest();

        public Builder(RestClient client, String fileName){
            try {
                request.client = client;
                request.url = new URL(client.url, fileName);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        public Builder requestType(RequestType requestType){
            request.type = requestType;
            return Builder.this;
        }

        public Builder requestBody(Object body){
            request.requestBody = body;
            return Builder.this;
        }

        public Builder addHeaders(RestRequestHeader... headers){
            request.headers.addAll(Arrays.asList(headers));
            return Builder.this;
        }

        public RestRequest build(){
            return request;
        }
    }
}
