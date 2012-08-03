package com.heyitworks.camel.component.pqseda;

import com.heyitworks.permqueue.BDBJEPersistentQueue;
import com.heyitworks.permqueue.PersistentQueue;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Seda-like component over permqueue: https://github.com/maciekr/pqseda
 *
 */
public class PermqueueComponent extends DefaultComponent {

    private static final int DEFAULT_MAX_PARALLEL_EXCHANGES = 3;

    protected Map<String, PersistentQueue> queues = new HashMap<String, PersistentQueue>();

    @Override
    protected Endpoint createEndpoint(String uri, String remaining,
                                      Map<String, Object> parameters) throws Exception {
        int parallelExchanges = getAndRemoveParameter(parameters,
                "parallelExchanges", Integer.class,
                DEFAULT_MAX_PARALLEL_EXCHANGES);
        return new PermqueueEndpoint(uri, this, parallelExchanges);
    }

    protected PersistentQueue getQueue(String uri) throws IOException {
        synchronized (queues) {
            String queueKey = getQueueKey(uri);
            PersistentQueue queue = queues.get(queueKey);
            if (queue == null) {
                queue = new BDBJEPersistentQueue(queueKey.replaceAll("[:=]",
                        "_"), 100, new NaiveSerializer());
                queues.put(queueKey, queue);
            }
            return queue;
        }
    }

    protected String getQueueKey(String uri) {
        if (uri.contains("?")) {
            // strip parameters
            uri = uri.substring(0, uri.indexOf('?'));
        }
        return uri;
    }
}