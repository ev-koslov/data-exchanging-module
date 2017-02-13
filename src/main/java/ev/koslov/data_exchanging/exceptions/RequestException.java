package ev.koslov.data_exchanging.exceptions;

public class RequestException extends Exception {
    public RequestException(String message) {
        super(message);
    }

    public RequestException() {
        super();
    }
}
