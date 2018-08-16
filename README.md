# Simple Rest Client - WIP
[![Build Status](https://travis-ci.com/DeLaiT/SimpleRestClient-Android.svg?branch=master)](https://travis-ci.com/DeLaiT/SimpleRestClient-Android)
### Installation
``` gradle
dependencies {
    implementation 'delait.android:simple-rest-client:1.0.1'
}
```

### Examples
``` Java
static final RestClient restClient = new RestClient("localhost:8000");

RestRequest request = new RestRequest.Builder(restClient, "user/")
        .requestType(RequestType.GET)
        .addHeaders(new RestRequestHeader("TOKEN", token))
        .build();

new Callback() {
            @Override
            public void onSuccess(RestResponse restResponse) {
                User user = (User) restResponse.getResponseBody(User.class);
                //...
            }

            @Override
            public void onFailure(RestErrorResponse restErrorResponse) {
                Log.e(TAG, restErrorResponse.getErrorBody());
            }
        });

request.executeAsync(callback);
```
#### Rest Client
Client with default settings
``` Java
static final RestClient restClient = new RestClient("localhost:8000");
```
Client with custom settings
``` Java
static final RestClient restClient  = new RestClient.Builder("localhost:8000")
        .setGson(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()) //custom Gson
        .setTimeout(10000) //timeout in milis
        .build();
```

#### Rest Request
Get request
``` Java
RestRequest request = new RestRequest.Builder(restClient, "user/")
        .requestType(RequestType.GET)
        .addHeaders(new RestRequestHeader("TOKEN", token))
        .build();
```
Post request
``` Java
RestRequest request = new RestRequest.Builder(restClient, "user/create/")
        .requestType(RequestType.POST)
        .addHeaders(new RestRequestHeader("Content-Type", "application/json"))
        .requestBody(user)
        .build();
```

#### Executing requests
```Java
request.executeAsync(callback);
```
Response from bytes (might be used for downloading images from server)
```Java
request.getBytesAsync(callback)
```
#### Callbacks
Callback
``` Java
new Callback() {
           @Override
            public void onSuccess(RestResponse restResponse) {
                User user = (User) restResponse.getResponseBody(User.class);
                //...
            }

            @Override
            public void onFailure(RestErrorResponse restErrorResponse) {
                Log.e(TAG, restErrorResponse.getErrorBody());
            }
        });
```
ByteCallback
``` Java
new ByteCallback() {
            @Override
            public void onSuccess(byte[] bytes, int i, String message) {
                activity.runOnUiThread(()->{
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    displayedFriend.profilePicture = bitmap;
                    updateProfilePicture(bitmap);
                });
            }

            @Override
            public void onFailure(RestErrorResponse restErrorResponse) {

            }
```
