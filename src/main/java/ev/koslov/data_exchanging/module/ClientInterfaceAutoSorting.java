package ev.koslov.data_exchanging.module;

import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;
import ev.koslov.data_exchanging.iface.IRequestProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClientInterfaceAutoSorting extends ClientInterface {
    private List<IRequestProcessor> requestProcessors;

    public ClientInterfaceAutoSorting() {
        this.requestProcessors = new ArrayList<IRequestProcessor>();
    }

    final void sortAndProcess(Message message) throws IOException {
        Message.Header header = message.getHeader();
        RequestBody body = message.getBody();

        for (IRequestProcessor requestProcessor : requestProcessors) {
            if (requestProcessor.accept(header, body)) {
                requestProcessor.process(header, body);
            }
        }
    }

    public void addRequestProcessor(IRequestProcessor requestProcessor){
        requestProcessor.setAssociatedClient(getAssociatedEndpoint());
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
