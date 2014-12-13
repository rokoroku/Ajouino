/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.persistent;

import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Event;
import ajouino.util.DeviceFactory;
import ajouino.util.JdbcAdapter;
import ajouino.service.ServiceDiscoverer;

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
 * @author YoungRok
 */
public class DeviceCatalog {

    private final static String DEVICE_COLUMN_ID = "Id";
    private final static String DEVICE_COLUMN_ADDRESS = "Address";
    private final static String DEVICE_COLUMN_TYPE = "Type";
    private final static String DEVICE_COLUMN_LABEL = "Label";
    private final static String DEVICE_COLUMN_PASSWORD = "Password";
    private final static String DEVICE_COLUMN_CREATEDATE = "CreateDate";

    private final static String EVENT_COLUMN_TIMESTAMP = "Timestamp";
    private final static String EVENT_COLUMN_DEVICE_ID = "D_id";
    private final static String EVENT_COLUMN_TYPE = "Type";
    private final static String EVENT_COLUMN_VALUE = "Value";

    Map<String, Device> deviceMap;

    public DeviceCatalog() {
        deviceMap = new ConcurrentHashMap<String, Device>();
        createTable();
        reloadData();
    }

    public Device getDevice(String deviceId) {
        return deviceMap.get(deviceId);
    }

    public Device getDeviceByAddress(String remoteAddress) {
        for (Device device : deviceMap.values()) {
            String address = device.getAddress();
            address = address.split(":")[0];
            if (address.equals(remoteAddress)) return device;
        }
        return null;
    }

    public Collection<Device> getDevices() {
        return deviceMap.values();
    }

    public boolean putDevice(Device device) {
        if (device != null && device.getId() != null) {

            try {
                if (!JdbcAdapter.isOpened()) JdbcAdapter.openConnection();

                if (deviceMap.containsKey(device.getId())) {
                    updateDevice(device);
                } else {
                    JdbcAdapter.insertRecord("DEVICE", device);
                    deviceMap.put(device.getId(), device);
                }

                return true;
            } catch (SQLException ex) {
                Logger.getLogger(DeviceCatalog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public Device updateDevice(Device device) {
        String relation = "DEVICE";
        String condition = DEVICE_COLUMN_ID + "='" + device.getId() + "'";
        StringBuilder stringBuilder = new StringBuilder();
        if (device.getAddress() != null)
            stringBuilder.append(DEVICE_COLUMN_ADDRESS + "='").append(device.getAddress()).append("', ");
        if (device.getPassword() != null)
            stringBuilder.append(DEVICE_COLUMN_PASSWORD + "='").append(device.getPassword()).append("', ");
        if (device.getLabel() != null)
            stringBuilder.append(DEVICE_COLUMN_LABEL + "='").append(device.getLabel()).append("', ");
        if (device.getType() != null)
            stringBuilder.append(DEVICE_COLUMN_TYPE + "='").append(device.getType()).append("', ");

        String values = stringBuilder.toString();
        if (values.endsWith(", ")) values = values.substring(0, values.lastIndexOf(","));

        try {
            JdbcAdapter.updateRecord(relation, values, condition);
            Device originalDevice = getDevice(device.getId());
            if (device.getAddress() != null)
                originalDevice.setAddress(device.getAddress());
            if (device.getPassword() != null)
                originalDevice.setPassword(device.getPassword());
            if (device.getLabel() != null)
                originalDevice.setLabel(device.getLabel());
            if (device.getType() != null)
                originalDevice.setType(device.getType());

            return originalDevice;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Device removeDevice(String deviceId) {
        Device device = deviceMap.remove(deviceId);
        try {
            String relation = "DEVICE";
            String condition = DEVICE_COLUMN_ID + "='" + deviceId + "'";
            JdbcAdapter.deleteRecord(relation, condition);

            relation = "EVENT";
            condition = EVENT_COLUMN_DEVICE_ID + "='" + deviceId + "'";
            JdbcAdapter.deleteRecord(relation, condition);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return device;
    }

    public Event addEvent(Event event) {
        Device device = getDevice(event.getDeviceID());
        if(device != null) try {
            JdbcAdapter.insertRecord("EVENT", event);
            device.addEvent(event);
            return event;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Event removeEvent(Event event) {
        try {
            String relation = "EVENT";
            String condition = EVENT_COLUMN_TIMESTAMP + "='" + event.getTimestamp().getTime() + "'";
            JdbcAdapter.deleteRecord(relation, condition);

            Device device = getDevice(event.getDeviceID());
            if(device != null) {
                device.removeEvent(event);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return event;
    }

    public Collection<DeviceInfo> lookupDeviceOnNetwork() {

        Collection<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
        Collection<ServiceInfo> serviceInfos = ServiceDiscoverer.getServicesOnNetwork();

        for (ServiceInfo info : serviceInfos) {
            System.out.println(info.toString());
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setLabel(info.getName());
            deviceInfo.setId(info.getName());

            String address = info.getHostAddresses()[0];
            if (info.getPort() != 80) address += ":" + info.getPort();

            deviceInfo.setAddress(address);

            deviceInfos.add(deviceInfo);
        }
        return deviceInfos;

    }

    public Boolean putEvent(Device device, Event event) {
        if (device.getId().equals(event.getDeviceID())) try {
            if (!JdbcAdapter.isOpened()) JdbcAdapter.openConnection();
            JdbcAdapter.insertRecord("EVENT", event);
            device.addEvent(event);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void reloadData() {
        if (deviceMap == null) deviceMap = new ConcurrentHashMap<String, Device>();
        else deviceMap.clear();

        try {
            String relation = "DEVICE";
            String projection = "*";
            String condition = null;
            ResultSet resultSet = JdbcAdapter.selectRecords(projection, relation, condition);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                String deviceId = resultSet.getString(1);
                String deviceAddr = resultSet.getString(2);
                String deviceType = resultSet.getString(3);
                String deviceLabel = resultSet.getString(4);
                String devicePW = resultSet.getString(5);
                Date createDate = new Date(resultSet.getLong(6));

                DeviceInfo deviceInfo = new DeviceInfo(deviceId, deviceType, deviceAddr, deviceLabel);
                Device device = DeviceFactory.createDevice(deviceInfo);
                device.setCreateDate(createDate);
                device.setPassword(devicePW);
                deviceMap.put(deviceId, device);


                String relation2 = "EVENT";
                String condition2 = EVENT_COLUMN_DEVICE_ID + "='" + deviceId + "'";
                ResultSet resultSet2 = JdbcAdapter.selectRecords(projection, relation2, condition2);

                while (resultSet2.next()) {
                    Date timestamp = new Date(resultSet2.getLong(1));
                    //deviceId = resultSet2.getString(2);
                    String type = resultSet2.getString(3);
                    int value = resultSet2.getInt(4);

                    device.addEvent(new Event(deviceId, type, value, timestamp));
                }

            }
            System.out.println("Devices have been loaded from DB.");

        } catch (SQLException ex) {
            Logger.getLogger(DeviceCatalog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean createTable() {
        try {
            String relation = "DEVICE";
            String definition = DEVICE_COLUMN_ID + " VARCHAR(20) NOT NULL,"
                    + DEVICE_COLUMN_ADDRESS + " VARCHAR(20) NOT NULL,"
                    + DEVICE_COLUMN_TYPE + " VARCHAR(10),"
                    + DEVICE_COLUMN_LABEL + " VARCHAR(20),"
                    + DEVICE_COLUMN_PASSWORD + " VARCHAR(20),"
                    + DEVICE_COLUMN_CREATEDATE + " INT8,"
                    + "PRIMARY KEY (" + DEVICE_COLUMN_ID + ")";
            JdbcAdapter.createTable(relation, definition);

            relation = "EVENT";
            definition = EVENT_COLUMN_TIMESTAMP + " INT8 NOT NULL,"
                    + EVENT_COLUMN_DEVICE_ID + " VARCHAR(20) NOT NULL,"
                    + EVENT_COLUMN_TYPE + " CARCHAR(8) NOT NULL,"
                    + EVENT_COLUMN_VALUE + " INT NOT NULL,"
                    + "PRIMARY KEY (" + EVENT_COLUMN_TIMESTAMP + ") "
                    + "FOREIGN KEY (" + EVENT_COLUMN_DEVICE_ID + ") REFERENCES DEVICE(" + DEVICE_COLUMN_ID + ")";
            JdbcAdapter.createTable(relation, definition);

            System.out.println("table DEVICE, EVENT has been created.");
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(DeviceCatalog.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
