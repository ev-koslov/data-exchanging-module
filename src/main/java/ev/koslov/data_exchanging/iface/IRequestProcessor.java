package ev.koslov.data_exchanging.iface;

import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;
import ev.koslov.data_exchanging.module.Client;


public interface IRequestProcessor<T extends Enum<T>, B extends RequestBody<T>> {
    boolean accept(Message.Header requestHeader, RequestBody body);
    boolean process(Message.Header requestHeader, B body);
    void setAssociatedClient(Client client);
}
