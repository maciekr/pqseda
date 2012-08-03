package com.heyitworks.camel.component.pqseda;

import com.heyitworks.permqueue.PersistentQueue;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;

import java.io.IOException;

/**
 * 
 *
 */
public class PermqueueEndpoint extends ScheduledPollEndpoint {
    private PersistentQueue queue;
    private int parallelExchanges;

    protected PermqueueEndpoint(String endpointUri, PermqueueComponent component, int parallelExchanges)
            throws IOException {
        super(endpointUri, component);
        this.queue = component.getQueue(endpointUri);
        this.parallelExchanges = parallelExchanges;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new PermqueueConsumer(this, processor, parallelExchanges);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new PermqueueProducer(this);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public PersistentQueue getQueue() {
        return queue;
    }
}
