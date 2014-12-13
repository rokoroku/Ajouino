/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controller;

import ajouino.AjouinoServer;
import ajouino.model.Device;
import ajouino.model.DeviceInfo;
import ajouino.model.Event;
import ajouino.persistent.DeviceCatalog;
import ajouino.service.SystemFacade;
import ajouino.util.ArduinoCaller;
import ajouino.util.DeviceFactory;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

/**
 * DeviceController
 * <p/>
 * Controls HTTP Request related with Device
 */
public class DeviceController implements AjouinoServer.HTTPInterface {

    private DeviceCatalog deviceManager;
    private Gson gson = new Gson();

    private static DeviceController mInstance = null;

    public static DeviceController getInstance() {
        if(mInstance == null) mInstance = new DeviceController();
        return mInstance;
    }

    private DeviceController() {
        deviceManager = SystemFacade.getInstance().getDeviceCatalog();
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
                    //TODO: PUT device is not used yet

                } else {
                    return new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: PUT /device/");
                }

            case DELETE:
                if (operand != null) {
                    //device id is given.
                    Device device = deviceManager.getDevice(operand);

                    if (operand2 == null) {
                        // 1. DELETE /device/id/ deletes the device
                        return deleteDevice(device);

                    } else {
                        // 2. DELETE /device/id/eventid deletes the event
                        Event eventToRemove = null;
                        for (Event event : device.getEvents()) {
                            if (event.getTimestamp().getTime() == Long.parseLong(operand2)) {
                                eventToRemove = event;
                                break;
                            }
                        }
                        return deleteEvent(device, eventToRemove);

                    }
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
            String result = ArduinoCaller.invokeEvent(device, event);
            deviceManager.putEvent(device, event);
            return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, result);

        } catch (ArduinoCaller.ArduinoCallException ex) {
            Response.Status errorStatus = Response.Status.getStatusByCode(ex.getErrorCode());
            if (errorStatus == null) errorStatus = Response.Status.INTERNAL_ERROR;
            return new Response(errorStatus, NanoHTTPD.MIME_PLAINTEXT, ex.getMessage());

        }
        else {
            return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "device not found : " + deviceId);
        }
    }

    private Response postDevice(Device device) {
        try {
            DeviceInfo deviceInfo = null;
            String password = device.getPassword();
            String address = device.getAddress();
            String label = device.getLabel();
            String deviceValidationString = ArduinoCaller.requestInformation(device);
            System.out.println(deviceValidationString);
            if (deviceValidationString != null) deviceInfo = gson.fromJson(deviceValidationString, DeviceInfo.class);
            if (deviceInfo != null) {
                device = DeviceFactory.createDevice(deviceInfo);
                device.setPassword(password);
                device.setAddress(address);
                device.setAvailable(true);
                if(label != null) device.setLabel(label);

                deviceManager.putDevice(device);
                return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
            } else {
                device.setAvailable(false);
                return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_JSON, "Cannot connect to " + device.getAddress());
            }
        } catch (ArduinoCaller.ArduinoCallException e) {
            Response.Status errorStatus = Response.Status.getStatusByCode(e.getErrorCode());
            if (errorStatus == null) errorStatus = Response.Status.INTERNAL_ERROR;
            return new Response(errorStatus, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
        }
    }

    private Response deleteDevice(DeviceInfo deviceInfo) {
        if (deviceInfo != null) {
            Device device = deviceManager.removeDevice(deviceInfo.getId());
            try {
                ArduinoCaller.unregisterDevice(device);
            } catch (ArduinoCaller.ArduinoCallException e) {
                e.printStackTrace();
            }
            return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(device));
        } else {
            return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, "");
        }
    }

    private Response deleteEvent(Device device, Event event) {
        if (device != null) {
            if (event != null) {
                deviceManager.removeEvent(event);

                try {
                    String path = getClass().getResource("/").getPath() + "image/";
                    File imageFile = new File(path + event.getTimestamp().getTime());
                    Files.deleteIfExists(imageFile.toPath());
                } catch (IOException e) {
                    //e.printStackTrace();
                }

                return new Response(Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(event));
            } else {
                return new Response(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
            }
        } else {
            return new Response(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "NOT FOUND: Device not found.");
        }
    }
}
