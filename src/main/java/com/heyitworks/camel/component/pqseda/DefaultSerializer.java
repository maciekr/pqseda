package com.heyitworks.camel.component.pqseda;


import com.heyitworks.permqueue.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DefaultSerializer implements Serializer {

    public byte[] write(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream output = new ObjectOutputStream(baos);
            output.writeObject(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public Object read(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream input = new ObjectInputStream(bais);
            return input.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
