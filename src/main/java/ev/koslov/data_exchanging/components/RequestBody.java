package ev.koslov.data_exchanging.components;


public class RequestBody<T extends Enum<T>> extends MessageBody {
    private final T command;

    public RequestBody(T requestCommand) {
        //TODO: add checking for T implements Taglib interface. Throw exception otherwise
        this.command = requestCommand;
    }

    public T getCommand() {
        return command;
    }
}
