package delait.simplerestclient;

import android.net.SSLCertificateSocketFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class RestRequest {
    private static final String TAG = "RestRequest";
    public RestClient client;
    public RequestType type = RequestType.GET;
    private Object requestBody = null;
    private List<RestRequestHeader> headers = new ArrayList<>();
    private URL url;
    private Date startTime;
    private HttpURLConnection connection;
    private RestRequest() {

    }

    public void executeAsync(final Callback callback) {
        AsyncTask.execute(() -> {
           execute(callback);
        });
    }

    public void getBytesAsync(final ByteCallback byteCallback) {
        AsyncTask.execute(() -> {
           getBytes(byteCallback);
        });
    }

    public void getBytes(final ByteCallback byteCallback) {
        try {
            startTime = new Date();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            setupConnection(connection);
            tryToSendRequestBody(connection);
            handleBytesResponse(connection, byteCallback);
        } catch (IOException e) {
            long executionTime = new Date().getTime() - startTime.getTime();

            if (client.showLogs)
                Log.e(TAG, e.getMessage() + " " + executionTime + " ms", e);

            byteCallback.onFailure(new RestErrorResponse(
                    e.getMessage(), 0, "An exception occurred", executionTime));
        }
    }

    public void execute(final Callback callback){
        try {
            startTime = new Date();
            connection = (HttpURLConnection) url.openConnection();
            setupConnection(connection);
            tryToSendRequestBody(connection);
            handleResponse(connection, callback);
        } catch (Exception e) {
            long executionTime = new Date().getTime() - startTime.getTime();

            if (client.showLogs)
                Log.e(TAG, e.getMessage() + " " + executionTime + " ms", e);

            callback.onFailure(new RestErrorResponse(
                    e.getMessage(), 0, "An exception occurred", executionTime));
        }
    }

    public void addHeaders(RestRequestHeader... headers) {
        this.headers.addAll(Arrays.asList(headers));
    }

    private void setupConnection(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(getRequestTypeFromEnum(type));
        connection.setDoInput(true);
        connection.setDoOutput(requestBody != null);
        connection.setReadTimeout(client.timeout);
        connection.setConnectTimeout(client.timeout);

        if (client.trustEveryone) {
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConn = (HttpsURLConnection) connection;
                httpsConn.setSSLSocketFactory(SSLCertificateSocketFactory.getInsecure(0, null));
                httpsConn.setHostnameVerifier(new AllowAllHostnameVerifier());
            }
        }

        for (int i = 0; i < headers.size(); i++) {
            RestRequestHeader header = headers.get(i);
            connection.addRequestProperty(header.key, header.value);
        }
    }

    private void tryToSendRequestBody(HttpURLConnection connection) throws IOException {
        if (requestBody != null) {
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());

            if (requestBody instanceof String)
                wr.write(((String) requestBody).getBytes("UTF-8"));
            else if (requestBody instanceof byte[]) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write((byte[]) requestBody);
                out.writeTo(connection.getOutputStream());
                out.flush();
                out.close();
            } else
                wr.write(client.gson.toJson(requestBody).getBytes("UTF-8"));

            wr.close();
        }
    }

    private void handleResponse(HttpURLConnection connection, Callback callback) throws IOException {
        String response = readResponse(connection);
        long executionTime = new Date().getTime() - startTime.getTime();

        if (connection.getResponseCode() < 300) {
            if (client.showLogs)
                Log.i(TAG, getRequestTypeFromEnum(type) +
                        " " + url.getPath() +
                        " result: " + connection.getResponseCode() +
                        " " + connection.getResponseMessage() +
                        " " + executionTime + " ms");

            callback.onSuccess(new RestResponse(response, connection.getResponseCode(),
                    connection.getResponseMessage(), client.gson, executionTime));
        } else {
            if (client.showLogs)
                Log.w(TAG, getRequestTypeFromEnum(type) +
                        " " + url.getPath() +
                        " result: " + connection.getResponseCode() +
                        " " + connection.getResponseMessage() +
                        " " + executionTime + " ms");

            callback.onFailure(new RestErrorResponse(response, connection.getResponseCode(),
                    connection.getResponseMessage(), executionTime));
        }
    }

    private void handleBytesResponse(HttpURLConnection connection, ByteCallback byteCallback) throws IOException {
        long executionTime = new Date().getTime() - startTime.getTime();

        if (connection.getResponseCode() < 300) {
            if (client.showLogs)
                Log.i(TAG, getRequestTypeFromEnum(type) +
                        " " + url.getPath() +
                        " result: " + connection.getResponseCode() +
                        " " + connection.getResponseMessage() +
                        " " + executionTime + " ms");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            byte[] b = new byte[1024];
            int s = 0;

            while ((s = in.read(b)) > 0) {
                baos.write(b, 0, s);
            }

            byteCallback.onSuccess(baos.toByteArray(), connection.getResponseCode(),
                    connection.getResponseMessage(), executionTime);
        } else {
            if (client.showLogs)
                Log.w(TAG, getRequestTypeFromEnum(type) +
                        " " + url.getPath() +
                        " result: " + connection.getResponseCode() +
                        " " + connection.getResponseMessage() +
                        " " + executionTime + " ms");

            InputStream is = connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();

            byteCallback.onFailure(new RestErrorResponse(response.toString(), connection.getResponseCode(),
                    connection.getResponseMessage(), executionTime));
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
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

        public Builder(RestClient client, String path) {
            try {
                request.client = client;
                request.url = new URL(client.url, path);
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
