package com.heyitworks.camel.component.pqseda;

import com.heyitworks.permqueue.PersistentQueue;
import org.apache.camel.*;
import org.apache.camel.impl.*;
import org.apache.camel.spi.ExceptionHandler;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 */
public class PermqueueConsumer extends ServiceSupport implements Consumer {
	private static final int DEFAULT_FIXED_DELAY_SECONDS = 1;

	public static final int DEFAULT_INITIAL_DELAY_SECONDS = 1;

	private final static Logger LOGGER = LoggerFactory
			.getLogger(PermqueueConsumer.class);

	private PermqueueEndpoint endpoint;
	private Processor processor;
	private ExceptionHandler exceptionHandler;
	private ScheduledExecutorService executorService;
	private int maxParallelExchangeProcessing;

	public PermqueueConsumer(PermqueueEndpoint endpoint, Processor processor,
			int maxParallelExchangeProcessing) {
		this.endpoint = endpoint;
		this.processor = processor;
		this.maxParallelExchangeProcessing = maxParallelExchangeProcessing;
	}

	@Override
	protected void doStart() throws Exception {
		if (executorService == null || executorService.isTerminated()) {
			executorService = Executors
					.newScheduledThreadPool(maxParallelExchangeProcessing);
		}
		for (int i = 0; i < maxParallelExchangeProcessing; i++) {
			executorService.scheduleWithFixedDelay(
					new Poller(endpoint.getQueue()),
					DEFAULT_INITIAL_DELAY_SECONDS, DEFAULT_FIXED_DELAY_SECONDS,
					TimeUnit.SECONDS);
		}
	}

	@Override
	protected void doStop() throws Exception {
		executorService.shutdown();
        try {
            executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
	}

	public ExceptionHandler getExceptionHandler() {
		if (exceptionHandler == null) {
			exceptionHandler = new LoggingExceptionHandler(getClass());
		}
		return exceptionHandler;
	}

	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

    @Override
	public Endpoint getEndpoint() {
		return endpoint;
	}

	public static DefaultExchange generateDefaultExchange(
			final PermqueuePayload payload, CamelContext camelContext,
			Endpoint fromEndpoint) {
		DefaultExchange newExchange = new DefaultExchange(camelContext);
		newExchange.setExchangeId(payload.getExchangeId());
		newExchange.setFromEndpoint(fromEndpoint);
		newExchange.setFromRouteId(payload.getFromRouteId());
		newExchange.setPattern(ExchangePattern.InOnly);

		Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
		for (int i = 0; i < payload.getPropertiesKeys().length; i++) {
			properties.put(payload.getPropertiesKeys()[i],
					payload.getPropertiesValues()[i]);
		}
		newExchange.setProperties(properties);

		if (camelContext.isUseMDCLogging()) {
			newExchange.setUnitOfWork(new MDCUnitOfWork(newExchange));
		} else {
			newExchange.setUnitOfWork(new DefaultUnitOfWork(newExchange));
		}

		DefaultMessage message = new DefaultMessage();
		message.setMessageId(payload.getMessageId());
		message.setBody(payload.getBody());

		for (int i = 0; i < payload.getHeaderKeys().length; i++) {
			message.setHeader(payload.getHeaderKeys()[i],
					payload.getHeaderValues()[i]);
		}

		newExchange.setIn(message);
		return newExchange;
	}

	private class Poller implements Runnable {

		private PersistentQueue queue;

		private Poller(PersistentQueue queue) {
			this.queue = queue;
		}

        @Override
		public void run() {

			if (isSuspending() || isSuspended()) {
				return;
			}

			PermqueuePayload polledPayload = null;

			try {
				StopWatch pollStopWatch = new Slf4JStopWatch("permqueue.POLL.call");
				pollStopWatch.start();
				polledPayload = queue.<PermqueuePayload> poll();
				pollStopWatch.stop();

				final PermqueuePayload payload = polledPayload;

				if (payload != null && !isStopping()) {

					try {
						LOGGER.debug("Consumed exchange with payload: " + payload.getBody());

						Exchange newExchange = generateDefaultExchange(payload,
								endpoint.getCamelContext(), endpoint);
						newExchange.setFromEndpoint(endpoint);

						LOGGER.debug("About to process an exchange from endpoint: {}. \nExchange (cloned): {}",
										endpoint.getEndpointUri(),
										newExchange.toString());

						processor.process(newExchange);
					} catch (Throwable e) {
						LOGGER.error(
										"Exception while consuming from PermQueue endpoint: {}",
										endpoint.getEndpointUri(), e);
						getExceptionHandler().handleException(
								"Error processing payload", e);

					}
				}
			} catch (Throwable e) {
				LOGGER.error("Exception while consuming from PermQueue endpoint: {}",
						endpoint.getEndpointUri(), e);
				if (polledPayload != null) {
					getExceptionHandler().handleException(
							"Error processing payload", e);
				} else {
					getExceptionHandler().handleException(e);
				}
			}

		}
	}
}