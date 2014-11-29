/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

import java.util.HashMap;
import java.util.Map;

/**
 * DeviceInfo 
 * to communicate between Ajouino server and device
 * and to create a device class in ajouino server.
 * 
 * @author YoungRok
 */
public class DeviceInfo {
    protected String id;
    protected String type;
    protected String address;
    protected String label;
    protected Map<String, Integer> values;

    public DeviceInfo() {
    }

    public DeviceInfo(String id, String type, String address, String label) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.label = label;
        this.values = new HashMap<String, Integer>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
        
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Integer> getValues() {
        return values;
    }

    public void setValues(Map<String, Integer> values) {
        this.values = values;
    }
    
    
}
