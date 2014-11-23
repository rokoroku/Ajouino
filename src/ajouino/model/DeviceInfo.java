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
    String id;  
    String type;  
    String address;
    String label;
    Map<Integer, Integer> values;    

    public DeviceInfo() {
    }

    public DeviceInfo(String id, String type, String address, String label) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.label = label;
        values = new HashMap<Integer, Integer>();
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

    public Map<Integer, Integer> getValues() {
        return values;
    }

    public void setValues(Map<Integer, Integer> values) {
        this.values = values;
    }
    
    
}
