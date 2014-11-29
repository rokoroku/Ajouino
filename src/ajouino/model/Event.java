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

    public static final String TYPE_DIGITAL = "digital";
    public static final String TYPE_ANALOG = "analog";
    public static final String TYPE_COLOR = "color";
    public static final String TYPE_INFO = "hello";

    String deviceID;
    String type;
    Integer value;
    Date timestamp;

    public Event() {
    }

    public Event(String deviceID, String type, int value) {
        this(deviceID, type, value, new Date());
    }

    public Event(String deviceID, String type, int value, Date timestamp) {
        this.deviceID = deviceID;
        this.type = type;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getValue() {
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
                .append((timestamp != null) ? "'" + timestamp.getTime() + "'" : "null").append(", ")
                .append((deviceID != null) ? "'" + deviceID + "'" : "null").append(", ")
                .append((type != null) ? "'" + type + "'" : "null").append(", ")
                .append(value).append(" ")
                .append(")");
        return sb.toString();
    }
    
}
