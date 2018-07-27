# Simple Rest Client
- [x] serializing json response
- [x] raw responses
- [x] headers
- [x] Post objects and Strings 
- [ ] downloading files
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
