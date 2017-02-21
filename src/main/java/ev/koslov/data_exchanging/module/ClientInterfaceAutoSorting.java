package ev.koslov.data_exchanging.module;

import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;
<<<<<<< HEAD
=======
import ev.koslov.data_exchanging.iface.IRequestProcessor;
>>>>>>> origin/master

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClientInterfaceAutoSorting extends ClientInterface {
<<<<<<< HEAD
    private List<AbstractClientRequestProcessor> requestProcessors;

    public ClientInterfaceAutoSorting() {
        this.requestProcessors = new ArrayList<AbstractClientRequestProcessor>();
=======
    private List<IRequestProcessor> requestProcessors;

    public ClientInterfaceAutoSorting() {
        this.requestProcessors = new ArrayList<IRequestProcessor>();
>>>>>>> origin/master
    }

    final void sortAndProcess(Message message) throws IOException {
        Message.Header header = message.getHeader();
        RequestBody body = message.getBody();

<<<<<<< HEAD
        for (AbstractClientRequestProcessor requestProcessor : requestProcessors) {
=======
        for (IRequestProcessor requestProcessor : requestProcessors) {
>>>>>>> origin/master
            if (requestProcessor.accept(header, body)) {
                requestProcessor.process(header, body);
            }
        }
    }

<<<<<<< HEAD
    public void addRequestProcessor(AbstractClientRequestProcessor requestProcessor) {
        requestProcessor.setAssociatedClient(this);
=======
    public void addRequestProcessor(IRequestProcessor requestProcessor){
        requestProcessor.setAssociatedClient(getAssociatedEndpoint());
>>>>>>> origin/master
        requestProcessors.add(requestProcessor);
    }

    @Override
    protected void processRequestFromServer(Message request) {
        try {
            sortAndProcess(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processRequestFromClient(Message request) {
        try {
            sortAndProcess(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
