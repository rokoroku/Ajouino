/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

/**
 *
 * @author YoungRok
 */
public class PowerStrip extends Device {

    public PowerStrip(DeviceInfo deviceInfo) {
        super(deviceInfo);
    }

    public PowerStrip(String deviceID, String type, String address, String label) {
        super(deviceID, type, address, label);
    }
}
