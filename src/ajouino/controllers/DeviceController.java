/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controllers;

import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Event;
import ajouino.services.DeviceManager;
import ajouino.services.SystemServiceFacade;
import ajouino.util.ArduinoUtil;
import ajouino.util.DeviceFactory;
import ajouino.util.HTTPInterface;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class DeviceController implements HTTPInterface {

    private DeviceManager deviceManager;

    public DeviceController() {
        deviceManager = SystemServiceFacade.getInstance().getDeviceManager();
    }
    
    /**
     * Controls device REST methods
     * 
     * @param method HTTP methods e.g. GET, PUT, POST, DELETE
     * @param uri given uri "/device/id/pin/value", ["device", "id", "pin", "value"]
     * @param param given param "?addr=127.0.0.1&password=ajouino", [<"addr","127.0.0.1">, <"password", "ajouino">]
     * @return Response can be json, html, etc.
     */
    @Override
    public Response processRequest(NanoHTTPD.Method method, String[] uri, Map<String, String> param) {

        String deviceId = null;
        Integer pin = null;
        Integer value = null;

        try {
            if (uri[0] != null && !uri[0].isEmpty()) {
                deviceId = uri[0];
            }
            if (uri.length > 2) {
                pin = Integer.parseInt(uri[1]);
                value = Integer.parseInt(uri[2]);
            }
        } catch (Exception e) {
            return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /device/id/");
        }

        Gson gson = new Gson();
        switch (method) {
            case GET:
                if (deviceId == null) {
                    //return all devices
                    Collection<Device> devices = deviceManager.getDevices();
                    return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(devices));
                } else {
                    //deviceId is presented.
                    Device device = deviceManager.getDevice(deviceId);
                    if (device != null) {
                        return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
                    } else {
                        return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "device not found : " + deviceId);
                    }
                }

            case POST:
                if (deviceId != null) {
                    //deviceId is presented.
                    Device device = deviceManager.getDevice(deviceId);
                    if (device != null) {
                        if (pin != null && value != null) {
                            try {
                                //TODO: set device pin value
                                Event event = new Event(deviceId, pin, value, new Date());
                                String result = ArduinoUtil.invokeEvent(device, event);
                                return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, result);

                            } catch (ArduinoUtil.ArduinoException ex) {
                                Logger.getLogger(DeviceController.class.getName()).log(Level.SEVERE, null, ex);

                                Response.Status errorStatus = Response.Status.getStatusByCode(ex.getErrorCode());
                                if(errorStatus == null) errorStatus = Response.Status.INTERNAL_ERROR;
                                return new Response(errorStatus, NanoHTTPD.MIME_JSON, ex.getMessage());
                            }
                        }
                    } else {
                        return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "device not found : " + deviceId);
                    }
                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: POST /device/id/pin/value");
                }
                break;
            case PUT:
                if(deviceId != null) try {
                    //TODO: add device with params
                    String deviceInfoString = ArduinoUtil.requestInformation(deviceId, "arduino");
                    DeviceInfo deviceInfo = gson.fromJson(deviceInfoString, DeviceInfo.class);
                    Device device = DeviceFactory.createDevice(deviceInfo);
//                    Device device = new Device(deviceId, param.get("address"));
//                    device.setLabel("test");
                    deviceManager.putDevice(device);
                    return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
                } catch (Exception e) {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: " + e.getMessage());        
                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: PUT /device/inetaddress");
                }
    
            case DELETE:
                if(deviceId != null) {
                    //TODO: add device (deviceId will be address)
                    Device device = deviceManager.removeDevice(deviceId);
                    return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: DELETE /device/id");
                }

            
            default:
                break;
        }
        return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error.");
    }
}
