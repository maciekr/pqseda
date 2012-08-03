package com.heyitworks.camel.component.pqseda;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermqueueProducer extends DefaultProducer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(PermqueueProducer.class);

    protected final PermqueueEndpoint endpoint;

    protected PermqueueProducer(Endpoint endpoint) {
        super(endpoint);
        this.endpoint = (PermqueueEndpoint) endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // Extract for storing pay-load only.
        PermqueuePayload queuePayload = PermqueuePayload
                .generateQueuePayload(exchange);

        LOGGER.debug("Adding exchange: {} to PermQueue: {}",
                exchange.toString(), endpoint.getEndpointUri());

        StopWatch addStopWatch = new Slf4JStopWatch("permqueue.ADD.call");
        addStopWatch.start();
        endpoint.getQueue().add(queuePayload);
        addStopWatch.stop();
        LOGGER.debug("Exchange: {} added to PermQueue: {}",
                exchange.toString(), endpoint.getEndpointUri());

    }

}
