/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.util;

import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Lamp;
import ajouino.model.PowerStrip;

/**
 *
 * @author YoungRok
 */
public class DeviceFactory {

    public static Device createDevice(DeviceInfo deviceInfo) {
        String type = deviceInfo.getType();
        Device device = null;

        if (type != null) {
            if (type.equalsIgnoreCase("powerstrip")) {
                device = new PowerStrip(deviceInfo.getId(), deviceInfo.getAddress());
            } else if (type.equalsIgnoreCase("lamp")) {
                device = new Lamp(deviceInfo.getId(), deviceInfo.getAddress());
            } else if (type.equalsIgnoreCase("intercom")) {
                device = new Device(deviceInfo.getId(), deviceInfo.getAddress());
            } else {
                device = new Device(deviceInfo.getId(), deviceInfo.getAddress());
            }
            device.setType(type);
        } else {
            device = new Device(deviceInfo.getId(), deviceInfo.getAddress());
        }
        
        if (deviceInfo.getValues() != null) {
            device.setValues(deviceInfo.getValues());
        }

        return device;
    }
}
