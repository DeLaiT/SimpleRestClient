# Simple Rest Client
[![Build Status](https://travis-ci.com/DeLaiT/SimpleRestClient-Android.svg?branch=master)](https://travis-ci.com/DeLaiT/SimpleRestClient-Android)
- [x] serializing json response
- [x] raw responses
- [x] headers
- [x] Post objects and Strings 
- [x] responses in byte arrays
- [ ] request body in byte array
### Installation
1. Copy .arr file from builds folder to /libs in your project
2. add dependency to build.graddle
``` graddle
dependencies {
    implementation files('libs/simplerestclient.aar')
}
```
3. Build > Clean Project
4. Sync graddle & Build

### Example
``` java
RestClient client = new RestClient("http://maps.googleapis.com/maps/api/geocode/", 10000);

RestRequest request = new RestRequest.Builder(client, "json?latlng=0,0&sensor=true")
        .requestType(RequestType.GET)
        .build();

request.executeAsync(GeoData.class, new Callback<Object>() {
    @Override
    public void onSuccess(Object responseBody, int code) {
        GeoData = (GeoData)responseBody;
    }

    @Override
    public void onFailure(String errorBody, int code) {}
});
```
