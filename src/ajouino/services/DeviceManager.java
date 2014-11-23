/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.services;

import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Event;
import ajouino.util.DeviceFactory;
import ajouino.util.JDBCUtil;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class DeviceManager {

    Map<String, Device> deviceMap;

    public DeviceManager() {
        deviceMap = new ConcurrentHashMap<String, Device>();
        createTable();
        reloadData();        
    }

    public Device getDevice(String deviceId) {
        return deviceMap.get(deviceId);
    }

    public Collection<Device> getDevices() {
        return deviceMap.values();
    }

    public void putDevice(Device device) {
        if (device != null && device.getDeviceID() != null) {
            deviceMap.put(device.getDeviceID(), device);

            try {
                if(!JDBCUtil.isOpened()) JDBCUtil.openConnection();
                JDBCUtil.insertRecord("DEVICE", device);
            } catch (SQLException ex) {
                Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }

    public Device removeDevice(String deviceId) {
        return deviceMap.remove(deviceId);
    }

    public boolean updateData(Device device) {
        return false;
    }
    
    public void reloadData() {
        if(deviceMap == null) deviceMap = new ConcurrentHashMap<String, Device>();
        else deviceMap.clear();

        try {
            String relation = "DEVICE";
            String projection = "*";
            String condition = null;
            ResultSet resultSet = JDBCUtil.selectRecords(projection, relation, condition);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                String deviceId = resultSet.getString("Id");
                String deviceAddr = resultSet.getString("Address");
                String deviceType = resultSet.getString("Type");
                String deviceLabel = resultSet.getString("Label");
                String devicePW = resultSet.getString("Password");
                int devicePins = resultSet.getInt("Pin");
                Date createDate = new Date(resultSet.getLong("CreateDate"));
                
                DeviceInfo deviceInfo = new DeviceInfo(deviceId, deviceType, deviceAddr, deviceLabel);
                Device device = DeviceFactory.createDevice(deviceInfo);
                device.setCreateDate(createDate);
                device.setPassword(devicePW);
                device.setType(devicePW);               
                deviceMap.put(deviceId, device);
                
                String relation2 = "EVENT";
                String condition2 = "'D_id'='" + deviceId +"'";
                ResultSet resultSet2 = JDBCUtil.selectRecords(projection, relation2, condition2);

                while(resultSet2.next()) {
                    Date timestamp = new Date(resultSet2.getLong("Timestamp"));
                    int pin = resultSet2.getInt("pin");
                    int value = resultSet2.getInt("value");
                    Event event = new Event(deviceId, pin, value, timestamp);
                    device.addEvent(event);
                }
    
            }
            System.out.println("Devices have been loaded from DB.");

        } catch (SQLException ex) {
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean createTable() {
        try {
            String relation = "DEVICE";
            String definition = "Id VARCHAR(15) NOT NULL,"
                    + "Address VARCHAR(15) NOT NULL,"
                    + "Type VARCHAR(10),"
                    + "Label VARCHAR(15),"
                    + "Password VARCHAR(15),"
                    + "Pin INT,"
                    + "CreateDate INT8,"
                    + "PRIMARY KEY (Id)";
            JDBCUtil.createTable(relation, definition);

            relation = "EVENT";
            definition = "Timestamp INT8 NOT NULL,"
                    + "D_id VARCHAR(15) NOT NULL,"
                    + "Pin INT NOT NULL,"
                    + "Value INT NOT NULL,"
                    + "PRIMARY KEY (Timestamp) "
                    + "FOREIGN KEY (D_id) REFERENCES DEVICE(Id)";
            JDBCUtil.createTable(relation, definition);

            System.out.println("table DEVICE, EVENT has been created.");
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
