package delait.simplerestclient;

public class RestErrorResponse {
    public final int status;
    public final String statusMessage;
    private String errorBody;

    public RestErrorResponse (String errorBody, int status, String statusMessage){
        this.errorBody = errorBody;
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public String getErrorBody(){
        return errorBody;
    }
}
