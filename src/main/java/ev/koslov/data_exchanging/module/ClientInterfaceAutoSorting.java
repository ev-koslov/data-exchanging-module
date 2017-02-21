package ev.koslov.data_exchanging.module;

import ev.koslov.data_exchanging.components.Message;
import ev.koslov.data_exchanging.components.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClientInterfaceAutoSorting extends ClientInterface {
    private List<AbstractClientRequestProcessor> requestProcessors;

    public ClientInterfaceAutoSorting() {
        this.requestProcessors = new ArrayList<AbstractClientRequestProcessor>();
    }

    final void sortAndProcess(Message message) throws IOException {
        Message.Header header = message.getHeader();
        RequestBody body = message.getBody();

        for (AbstractClientRequestProcessor requestProcessor : requestProcessors) {
            if (requestProcessor.accept(header, body)) {
                requestProcessor.process(header, body);
            }
        }
    }

    public void addRequestProcessor (AbstractClientRequestProcessor requestProcessor) {
        requestProcessor.setAssociatedClient(this);
        requestProcessors.add(requestProcessor);
    }

    @Override
    protected void processRequestFromServer (Message request){
        try {
            sortAndProcess(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processRequestFromClient (Message request){
        try {
            sortAndProcess(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
