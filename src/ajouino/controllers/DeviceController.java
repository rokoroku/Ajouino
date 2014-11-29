/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controllers;

import ajouino.AjouinoServer;
import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Event;
import ajouino.services.DeviceManager;
import ajouino.services.SystemServiceFacade;
import ajouino.util.ArduinoUtil;
import ajouino.services.DeviceFactory;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author YoungRok
 */
public class DeviceController implements AjouinoServer.HTTPInterface {

    private DeviceManager deviceManager;
    private Gson gson = new Gson();

    public DeviceController() {
        deviceManager = SystemServiceFacade.getInstance().getDeviceManager();
    }

    /**
     * Controls device REST methods
     *
     * @param method HTTP methods e.g. GET, PUT, POST, DELETE
     * @param uri    given uri "/device/id/pin/value", ["device", "id", "pin", "value"]
     * @param param  given param e.g. http params & post data content
     * @return Response can be json, html, etc.
     */
    @Override
    public Response processRequest(NanoHTTPD.Method method, String[] uri, Map<String, String> param) {

        String command = null;
        String operand = null;
        String operand2 = null;
        String postData = null;
        if (uri != null) {
            try {
                if (uri.length > 0 && !uri[0].isEmpty()) command = uri[0];
                if (uri.length > 1 && !uri[1].isEmpty()) operand = uri[1];
                if (uri.length > 2 && !uri[2].isEmpty()) operand2 = uri[2];
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /device/id/");
            }
        }
        if (method.equals(NanoHTTPD.Method.POST)) {
            postData = param.get("postData");
        } else if (method.equals(NanoHTTPD.Method.PUT)) {
            postData = param.get("content");
        }

        switch (method) {
            case GET:
                if (command.equalsIgnoreCase("devices")) {
                    //return all devices
                    if (operand == null) {
                        return getDevices();
                    } else if (operand.equalsIgnoreCase("lookup")) {
                        return getDevicesOnNetwork();
                    }

                } else if (command.equalsIgnoreCase("device") && operand != null) {
                    //deviceId is presented.
                    return getDevice(operand);

                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /device/id/, /devices/ or /devices/lookup");
                }

            case POST:
                if (operand == null) {
                    Device device = gson.fromJson(postData, Device.class);
                    return postDevice(device);

                } else if (operand2 != null && operand2.equalsIgnoreCase("event")) {
                    //deviceId is presented.
                    Event event = gson.fromJson(postData, Event.class);
                    return postEvent(operand, event);

                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: POST /device/ or POST /device/id/event");
                }

            case PUT:
                if (operand != null) {
                    Device device = deviceManager.getDevice(operand);
                    DeviceInfo deviceInfo = gson.fromJson(postData, DeviceInfo.class);
                    //device.update(deviceInfo);

                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: PUT /device/");
                }

            case DELETE:
                if (operand != null) {
                    Device device = deviceManager.getDevice(operand);
                    return deleteDevice(device);

                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: DELETE /device/id");
                }

            default:
                return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. use GET, POST, PUT, DELETE methods only.");
        }
    }

    private Response getDevices() {
        Gson gson = new Gson();
        Collection<Device> devices = deviceManager.getDevices();
        return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(devices));
    }

    private Response getDevice(String deviceId) {
        Gson gson = new Gson();
        Device device = deviceManager.getDevice(deviceId);
        if (device != null) {
            return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
        } else {
            return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "device not found : " + deviceId);
        }
    }

    private Response getDevicesOnNetwork() {
        Gson gson = new Gson();
        Collection<DeviceInfo> devices = deviceManager.lookupDeviceOnNetwork();
        return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(devices));
    }

    private Response postEvent(String deviceId, Event event) {
        Device device = deviceManager.getDevice(deviceId);
        if (device != null && event.getDeviceID().equals(deviceId)) try {
            String result = ArduinoUtil.invokeEvent(device, event);
            deviceManager.putEvent(device, event);
            return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, result);

        } catch (ArduinoUtil.ArduinoException ex) {
            Response.Status errorStatus = Response.Status.getStatusByCode(ex.getErrorCode());
            if (errorStatus == null) errorStatus = Response.Status.INTERNAL_ERROR;
            return new Response(errorStatus, NanoHTTPD.MIME_PLAINTEXT, ex.getMessage());

        } else {
            return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "device not found : " + deviceId);
        }
    }

    private Response postDevice(Device device) {
        try {
            DeviceInfo deviceInfo = null;
            String password = device.getPassword();
            String deviceValidationString = ArduinoUtil.requestInformation(device);
            System.out.println(deviceValidationString);
            if (deviceValidationString != null) deviceInfo = gson.fromJson(deviceValidationString, DeviceInfo.class);
            if (deviceInfo != null) {
                device = DeviceFactory.createDevice(deviceInfo);
                device.setPassword(password);
                device.setAvailable(true);
                deviceManager.putDevice(device);
                return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
            } else {
                device.setAvailable(false);
                return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_JSON, "Cannot connect to " + device.getAddress());
            }
        } catch (ArduinoUtil.ArduinoException e) {
            Response.Status errorStatus = Response.Status.getStatusByCode(e.getErrorCode());
            if (errorStatus == null) errorStatus = Response.Status.INTERNAL_ERROR;
            return new Response(errorStatus, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
        }
    }

    private Response deleteDevice(DeviceInfo deviceInfo) {
        Device device = deviceManager.removeDevice(deviceInfo.getId());
        return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
    }
}
