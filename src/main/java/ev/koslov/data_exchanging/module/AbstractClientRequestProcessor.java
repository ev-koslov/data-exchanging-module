package ev.koslov.data_exchanging.module;

import com.sun.istack.internal.Nullable;
import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;



public abstract class AbstractClientRequestProcessor<T extends Enum<T>, B extends RequestBody<T>, I extends ClientInterface> {
    private I clientInterface;

    protected abstract boolean accept(Message.Header requestHeader, @Nullable RequestBody body);
    protected abstract void process(Message.Header requestHeader, @Nullable B body);

    final void setAssociatedClient(I clientInterface){
        this.clientInterface = clientInterface;
    }
}
