/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

import ajouino.util.ArduinoCaller;
import ajouino.util.JdbcAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author YoungRok
 */
public class Device extends DeviceInfo implements JdbcAdapter.SQLObjectInteface {

    private String password;
    private List<Event> events = new ArrayList<Event>();
    private Date createDate;
    private Date lastSyncDate;
    private boolean available;

    public Device(DeviceInfo deviceInfo) {
        super(deviceInfo.getId(), deviceInfo.getType(), deviceInfo.getAddress(), deviceInfo.getLabel());
    }

    public Device(String deviceID, String type, String address, String label) {
        super(deviceID, type, address, label);
    }

    public boolean connect() {
        try {
            ArduinoCaller.requestInformation(this);
            this.available = true;
        } catch (ArduinoCaller.ArduinoCallException ex) {
            Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            this.available = false;
        }
        return isAvailable();
    }

    public boolean sendEvent(Event event) throws ArduinoCaller.ArduinoCallException {
        if (event != null) {
            ArduinoCaller.invokeEvent(this, event);
            this.events.add(event);
            lastSyncDate = new Date();
            return true;
        }
        return false;
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

    public boolean addEvent(Event event) {
        if (!events.contains(event)) {
            return events.add(event);
        }
        return false;
    }

    public boolean removeEvent(Event event) {
        return events.remove(event);
    }

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();

        sb.append("VALUES (")
                .append((id != null) ? "'" + id + "'" : "null").append(", ")
                .append((address != null) ? "'" + address + "'" : "null").append(", ")
                .append((type != null) ? "'" + type + "'" : "null").append(", ")
                .append((label != null) ? "'" + label + "'" : "null").append(", ")
                .append((password != null) ? "'" + password + "'" : "null").append(", ")
                .append((createDate != null) ? "'" + createDate.getTime() + "'" : "null")
                .append(")");
        return sb.toString();
    }

    @Override
    public String getPrimaryKey() {
        return id;
    }
}
