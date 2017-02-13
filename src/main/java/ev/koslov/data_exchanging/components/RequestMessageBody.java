package ev.koslov.data_exchanging.components;


public class RequestMessageBody<T extends Enum<T>> extends AbstractMessageBody {
    private T command;

    public RequestMessageBody(T requestCommand) {
        //TODO: add checking for T implements Taglib interface. Throw exception otherwise
        this.command = requestCommand;
    }

    public T getCommand() {
        return command;
    }
}
