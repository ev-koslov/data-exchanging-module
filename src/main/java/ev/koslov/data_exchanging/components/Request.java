package ev.koslov.data_exchanging.components;


public class Request {
    private long requestId;
    private Message responseMessage;

    public Request(Message requestMessage) {
        this.requestId = System.nanoTime();
        requestMessage.setRequestId(requestId);
        //TODO: set request ID to message
    }


    public long getRequestId() {
        return requestId;
    }

    public Message getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(Message responseMessage) {
        if (responseMessage.getResponseForRequestId() == requestId) {
            this.responseMessage = responseMessage;
        } else {
            throw new IllegalArgumentException("Response message is not for this request: " + responseMessage.getResponseForRequestId() + "!=" + requestId);
        }
    }
}
