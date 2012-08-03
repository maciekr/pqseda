package com.heyitworks.camel.component.pqseda.component;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.perf4j.StopWatch;

import java.util.LinkedList;

public class PermqueueComponentTest extends CamelTestSupport {

	private static final int PAUSE_TIMEOUT = 5000;
	private static final String PERM_QUEUE_TEST_QUEUE_URI = "pqseda:testQueue?parallelExchanges=5";
	private static final String MOCK_DESTINATION_URI = "mock:destination";
	private static final String PAUSING_MESSAGE_BODY = "pause";

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(PERM_QUEUE_TEST_QUEUE_URI)
						.routeId("testRoute")
						.process(new Processor() {
							public void process(Exchange exchange)
									throws Exception {
								String body = exchange.getIn().getBody(
										String.class);
								if (PAUSING_MESSAGE_BODY.equals(body)) {
									Thread.sleep(PAUSE_TIMEOUT);
								}
							}
						}).to(MOCK_DESTINATION_URI);
			}
		};
	}

	@Test
	public void testParallelConsume() throws InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int totalMessageCount = 10;
		MockEndpoint destination = getMockEndpoint(MOCK_DESTINATION_URI);
		destination.setExpectedMessageCount(totalMessageCount);
		destination.expectedBodiesReceivedInAnyOrder(new LinkedList() {
			{
                for (int i=0;i<totalMessageCount-1;i++) {
                    add("test_"+i);
                }
				add(PAUSING_MESSAGE_BODY);
			}
		});

		template.sendBody(PERM_QUEUE_TEST_QUEUE_URI, PAUSING_MESSAGE_BODY);

        for (int i=0;i<totalMessageCount - 1;i++) {
		    template.sendBody(PERM_QUEUE_TEST_QUEUE_URI, "test_"+i);
        }
        destination.assertIsSatisfied(PAUSE_TIMEOUT * 3);
        stopWatch.stop();
        System.out.println("---> TIME = "+stopWatch.getElapsedTime());
	}
}
