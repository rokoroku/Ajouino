/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

import ajouino.util.JDBCUtil;
import java.util.Date;

/**
 *
 * @author YoungRok
 */
public class Event implements JDBCUtil.SQLObjectInteface {
    String deviceID;
    String pinType = "digital";

    int pin;
    int value;
    Date timestamp;

    public Event() {
    }    

    public Event(String deviceID, int pin, int value, Date timestamp) {
        this.deviceID = deviceID;
        this.pin = pin;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    
    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getPinType() {
        return pinType;
    }

    public void setPinType(String pinType) {
        this.pinType = pinType;
    }
    
    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();
 
        sb.append("VALUES (")
                .append((timestamp != null) ? "'" + timestamp.getTime() + "'" : "null")
                .append((deviceID != null) ? "'" + deviceID + "'" : "null").append(", ")
                .append(pin).append(",")
                .append(value).append(", ")
                .append(")");
        return sb.toString();
    }
    
}
