/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.services;

import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Event;
import ajouino.util.JDBCUtil;
import ajouino.util.JmDNSUtil;

import javax.jmdns.ServiceInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public boolean putDevice(Device device) {
        if (device != null && device.getId() != null) {

            try {
                if(!JDBCUtil.isOpened()) JDBCUtil.openConnection();
                JDBCUtil.insertRecord("DEVICE", device);
                deviceMap.put(device.getId(), device);
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(DeviceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public Device removeDevice(String deviceId) {
        Device device = deviceMap.remove(deviceId);
        try {
            String relation = "DEVICE";
            String projection = "'Id'='" + deviceId +"'";
            String condition = null;
            JDBCUtil.deleteRecord(projection, relation, condition);

            relation = "EVENT";
            projection = "'D_Id'='" + deviceId +"'";
            JDBCUtil.deleteRecord(projection, relation, condition);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return device;
    }

    public Collection<DeviceInfo> lookupDeviceOnNetwork() {

        Collection<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        ServiceInfo[] serviceInfos = JmDNSUtil.lookupServices(JmDNSUtil.ARDUINO_SERVICE);

        for(ServiceInfo info : serviceInfos) {
            System.out.println(info.toString());
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setAddress(info.getHostAddresses()[0]);
            deviceInfo.setLabel(info.getName());
            deviceInfo.setId(info.getName());
            deviceInfos.add(deviceInfo);
        }
        return deviceInfos;

    }

    public Boolean putEvent(Device device, Event event) {
        if (device.getId().equals(event.getDeviceID())) try {
            if(!JDBCUtil.isOpened()) JDBCUtil.openConnection();
            JDBCUtil.insertRecord("EVENT", event);
            device.addEvent(event);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                    String type = resultSet2.getString("Type");
                    int value = resultSet2.getInt("Value");

                    Event event = new Event(deviceId, type, value, timestamp);
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
                    + "CreateDate INT8,"
                    + "PRIMARY KEY (Id)";
            JDBCUtil.createTable(relation, definition);

            relation = "EVENT";
            definition = "Timestamp INT8 NOT NULL,"
                    + "D_id VARCHAR(15) NOT NULL,"
                    + "Type CARCHAR(8) NOT NULL,"
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
