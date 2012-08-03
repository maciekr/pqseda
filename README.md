pqseda
======

Seda-like camel component that uses permqueue (https://github.com/maciekr/permqueue) for async delivery

How-To
======

The component registers with camel under the name : "pqseda"

<code>
from("direct:input").to("pqseda:aQueueName?parallelExchanges=2"); from("pqseda:aQueueName?parallelExchanges=2").to("bean:process");
</code>
