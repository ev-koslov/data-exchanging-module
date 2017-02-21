package ev.koslov.data_exchanging.iface;

import com.sun.istack.internal.Nullable;
import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;
import ev.koslov.data_exchanging.module.Client;


public interface IRequestProcessor<T extends Enum<T>, B extends RequestBody<T>> {
    boolean accept(Message.Header requestHeader, @Nullable RequestBody body);
    boolean process(Message.Header requestHeader, @Nullable B body);
    void setAssociatedClient(Client client);
}
