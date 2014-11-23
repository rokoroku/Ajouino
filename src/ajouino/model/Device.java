/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

import ajouino.util.ArduinoUtil;
import ajouino.util.JDBCUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class Device implements JDBCUtil.SQLObjectInteface {

    String deviceID;
    String label;
    String address;
    String password;
    String type;
    Map<Integer, Integer> values;
    List<Event> events;
    Date createDate;
    Date lastSyncDate;
    boolean available;

    public Device(String deviceID, String address) {
        this.deviceID = deviceID;
        this.address = address;
        this.createDate = new Date();
        this.lastSyncDate = new Date();
        this.events = new ArrayList<Event>();
        this.values = new HashMap<Integer, Integer>();
    }

    public Device(String deviceID, String address, String password) {
        this(deviceID, address);
        this.password = password;
    }

    public boolean connect() {
        try {
            ArduinoUtil.requestInformation(address, password);
            this.available = true;
        } catch (ArduinoUtil.ArduinoException ex) {
            Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            this.available = false;
        }
        return isAvailable();
    }

    public boolean sendEvent(Event event) throws ArduinoUtil.ArduinoException {
        if (event != null) {
            ArduinoUtil.invokeEvent(this, event);
            this.events.add(event);
            lastSyncDate = new Date();
            return true;
        }
        return false;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastSyncDate() {
        return lastSyncDate;
    }

    public void setLastSyncDate(Date lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }

    public Map<Integer, Integer> getValues() {
        return values;
    }

    public void setValues(Map<Integer, Integer> values) {
        this.values = values;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();

        sb.append("VALUES (")
                .append((deviceID != null) ? "'" + deviceID + "'" : "null").append(", ")
                .append((address != null) ? "'" + address + "'" : "null").append(", ")
                .append((type != null) ? "'" + type + "'" : "null").append(", ")
                .append((label != null) ? "'" + label + "'" : "null").append(", ")
                .append((password != null) ? "'" + password + "'" : "null").append(", ")
                .append((values.size() > 0) ? "'" + values + "'" : "null").append(", ")
                .append((createDate != null) ? "'" + createDate.getTime() + "'" : "null")
                .append(")");
        return sb.toString();
    }
}
