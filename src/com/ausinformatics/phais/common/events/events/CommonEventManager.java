package com.ausinformatics.phais.common.events.events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.ausinformatics.phais.common.events.VisualGameEvent;
import com.ausinformatics.phais.server.interfaces.EventManager;
import com.ausinformatics.phais.utils.Base64;

public class CommonEventManager implements EventManager {

    @Override
    public String toData(VisualGameEvent event) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(event);
            oos.close();
            String res = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
            return res;
        } catch (IOException e) {
            System.out.println("Error to data: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public VisualGameEvent fromData(String data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(data, Base64.NO_WRAP));
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (VisualGameEvent) ois.readObject();
        } catch (IOException e) {
            System.out.println("Error from data: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("Error from data: " + e.getMessage());
            return null;
        }
    }

}
