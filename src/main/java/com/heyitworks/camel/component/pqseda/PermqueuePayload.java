package com.heyitworks.camel.component.pqseda;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * camel exchange payload
 */
public class PermqueuePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Will throw RuntimeException if body cannot be converted to String with registered type converters
     * <p/>
     * Headers and properties that cannot be converted to string are dropped.
     *
     * @param exchange
     * @return
     */
    public static PermqueuePayload generateQueuePayload(Exchange exchange) {
        PermqueuePayload result = new PermqueuePayload();

        Message message = (exchange.getIn() != null) ? exchange.getIn()
                : exchange.getOut();

        result.setMessageId(message.getMessageId());

        Object rawBody = message.getBody();
        String stringBody = message.getBody(String.class);
        if (rawBody != null && stringBody == null) {
            throw new RuntimeException("Can't convert body to String. BreadcrumbId: " + message.getHeader(Exchange.BREADCRUMB_ID));
        }
        result.setBody(stringBody);

        result.setExchangeId(exchange.getExchangeId());
        result.setFromRouteId(exchange.getFromRouteId());

        Map<String, Object> headers = message.getHeaders();

        List<String> headersKeys = new LinkedList<String>();
        List<String> headersValues = new LinkedList<String>();
        for (String key : headers.keySet()) {
            String value = message.getHeader(key, String.class);
            if (value != null) {
                headersKeys.add(key);
                headersValues.add(value);
            }
        }
        result.setHeaderKeys(headersKeys.toArray(new String[headersKeys.size()]));
        result.setHeaderValues(headersValues.toArray(new String[headersValues
                .size()]));

        Map<String, Object> properties = exchange.getProperties();
        List<String> propertiesKeys = new LinkedList<String>();
        List<String> propertiesValues = new LinkedList<String>();
        for (String key : properties.keySet()) {
            String value = exchange.getProperty(key, String.class);
            if (value != null) {
                propertiesKeys.add(key);
                propertiesValues.add(value);
            }
        }
        result.setPropertiesKeys(propertiesKeys
                .toArray(new String[propertiesKeys.size()]));
        result.setPropertiesValues(propertiesValues
                .toArray(new String[propertiesKeys.size()]));

        return result;
    }

    private String body;

    private String exchangeId;

    private String fromRouteId;

    private String[] headerKeys;

    private String[] headerValues;

    private String messageId;

    private String[] propertiesKeys;

    private String[] propertiesValues;

    public String getBody() {
        return body;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public String getFromRouteId() {
        return fromRouteId;
    }

    public String[] getHeaderKeys() {
        return headerKeys;
    }

    public String[] getHeaderValues() {
        return headerValues;
    }

    public String getMessageId() {
        return messageId;
    }

    public String[] getPropertiesKeys() {
        return propertiesKeys;
    }

    public String[] getPropertiesValues() {
        return propertiesValues;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setFromRouteId(String fromRouteId) {
        this.fromRouteId = fromRouteId;
    }

    public void setHeaderKeys(String[] headerKeys) {
        this.headerKeys = headerKeys;
    }

    public void setHeaderValues(String[] headerValues) {
        this.headerValues = headerValues;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setPropertiesKeys(String[] propertiesKeys) {
        this.propertiesKeys = propertiesKeys;
    }

    public void setPropertiesValues(String[] propertiesValues) {
        this.propertiesValues = propertiesValues;
    }
}
