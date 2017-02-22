package ev.koslov.data_exchanging.module;

import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;



public abstract class AbstractClientRequestProcessor<T extends Enum<T>, I extends ClientInterface> {
    private I clientInterface;

    protected abstract boolean accept(Message.Header requestHeader, RequestBody body);
    protected abstract void process(Message.Header requestHeader, RequestBody<T> body);

    final void setAssociatedClient(I clientInterface){
        this.clientInterface = clientInterface;
    }

    protected final I getAssociatedClient(){
        return clientInterface;
    }
}
