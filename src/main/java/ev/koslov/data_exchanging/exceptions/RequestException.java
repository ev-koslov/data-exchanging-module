package ev.koslov.data_exchanging.exceptions;

import java.io.IOException;

public class RequestException extends IOException {
    public RequestException(String message) {
        super(message);
    }

    public RequestException() {
        super();
    }
}
