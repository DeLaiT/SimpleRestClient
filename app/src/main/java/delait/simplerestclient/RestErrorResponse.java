package delait.simplerestclient;

import org.greenrobot.eventbus.EventBus;

public class RestErrorResponse {
    public final int status;
    public final String statusMessage;
    private String errorBody;
    public long executionTime;

    public RestErrorResponse (String errorBody, int status, String statusMessage, long executionTime){
        this.errorBody = errorBody;
        this.status = status;
        this.statusMessage = statusMessage;
        this.executionTime = executionTime;

        EventBus.getDefault().post(this);
    }

    public String getErrorBody(){
        return errorBody;
    }

    public String toString(){
        return status + " " + statusMessage + " " + executionTime + " ms";
    }
}
