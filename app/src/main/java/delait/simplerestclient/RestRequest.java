package delait.simplerestclient;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

    private RestRequest() {
    }


    public void executeAsync(final Callback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    setupConnection(connection);
                    tryToSendRequestBody(connection);
                    handleResponse(connection, callback);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    callback.onFailure(new RestErrorResponse(
                            e.getMessage(), 0, "An exception occurred"));
                }
            }
        });
    }

    public void getBytesAsync(final ByteCallback byteCallback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    setupConnection(connection);
                    tryToSendRequestBody(connection);
                    handleBytesResponse(connection, byteCallback);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                    byteCallback.onFailure(new RestErrorResponse(
                            e.getMessage(), 0, "An exception occurred"));
                }
            }
        });
    }

    public void addHeaders(RestRequestHeader... headers) {
        this.headers.addAll(Arrays.asList(headers));
    }

    private void setupConnection(HttpURLConnection connection) throws Exception {
        connection.setRequestMethod(getRequestTypeFromEnum(type));
        connection.setDoInput(true);
        connection.setDoOutput(requestBody != null);
        connection.setReadTimeout(client.timeout);
        connection.setConnectTimeout(client.timeout);

        for (int i = 0; i < headers.size(); i++) {
            RestRequestHeader header = headers.get(i);
            connection.addRequestProperty(header.key, header.value);
        }
    }

    private void tryToSendRequestBody(HttpURLConnection connection) throws Exception {
        if (requestBody != null) {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

            if (requestBody instanceof String)
                wr.writeBytes((String) requestBody);
            else if (requestBody instanceof byte[]) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write((byte[]) requestBody);
                out.writeTo(connection.getOutputStream());
                out.flush();
                out.close();
            } else
                wr.writeBytes(client.gson.toJson(requestBody));

            wr.close();
        }
    }

    private void handleResponse(HttpURLConnection connection, Callback callback) throws Exception {
        String response = readResponse(connection);

        if (connection.getResponseCode() < 300) {
            Log.i(TAG, getRequestTypeFromEnum(type) + " result: " + connection.getResponseCode() + " " + connection.getResponseMessage());

            callback.onSuccess(new RestResponse(response,
                    connection.getResponseCode(), connection.getResponseMessage(), client.gson));
        } else {
            Log.w(TAG, getRequestTypeFromEnum(type) + " result: " + connection.getResponseCode() + " " + connection.getResponseMessage());

            callback.onFailure(new RestErrorResponse(response,
                    connection.getResponseCode(), connection.getResponseMessage()));
        }
    }

    private void handleBytesResponse(HttpURLConnection connection, ByteCallback byteCallback) throws Exception {
        if (connection.getResponseCode() < 300) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            byte[] b = new byte[1024];
            int s = 0;

            while ((s = in.read(b)) > 0) {
                baos.write(b, 0, s);
            }

            byteCallback.onSuccess(baos.toByteArray(), connection.getResponseCode(), connection.getResponseMessage());
        } else {
            InputStream is = connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();

            byteCallback.onFailure(new RestErrorResponse(response.toString(), connection.getResponseCode(), connection.getResponseMessage()));
        }
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
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

    private String getRequestTypeFromEnum(RequestType requestType) {
        switch (requestType) {
            case GET:
                return "GET";
            case POST:
                return "POST";
            case DELETE:
                return "DELETE";
            case PUT:
                return "PUT";
            case PATCH:
                return "PATCH";
            default:
                return "GET";
        }
    }

    public static class Builder {
        private RestRequest request = new RestRequest();

        public Builder(RestClient client, String fileName) {
            try {
                request.client = client;
                request.url = new URL(client.url, fileName);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        public Builder requestType(RequestType requestType) {
            request.type = requestType;
            return Builder.this;
        }

        public Builder requestBody(Object body) {
            request.requestBody = body;
            return Builder.this;
        }

        public Builder addHeaders(RestRequestHeader... headers) {
            request.headers.addAll(Arrays.asList(headers));
            return Builder.this;
        }

        public RestRequest build() {
            return request;
        }
    }
}
