package org._brown_tech._object;

import java.io.Serializable;

/**
 * @author Mandela
 */
public class OnlineService implements Serializable {

    public static final long serialVersionUID = 62L;

    public Integer serial;
    public String name;
    public Double defaultCost;

    public OnlineService() {
    }

    public OnlineService(Integer serial, String name, Double defaultCost) {
        this.serial = serial;
        this.name = name;
        this.defaultCost = defaultCost;
    }

    public Integer getSerial() {
        return serial;
    }

    public void setSerial(Integer serial) {
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDefaultCost() {
        return defaultCost;
    }

    public void setDefaultCost(Double defaultCost) {
        this.defaultCost = defaultCost;
    }

    @Override
    public String toString() {
        return "OnlineService{" +
                "serial=" + serial +
                ", name='" + name + '\'' +
                ", defaultCost=" + defaultCost +
                '}';
    }
}
