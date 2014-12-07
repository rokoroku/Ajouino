/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controller;

import ajouino.AjouinoServer;
import ajouino.model.Device;
import ajouino.model.Event;
import ajouino.persistent.DeviceCatalog;
import ajouino.service.SystemFacade;
import ajouino.util.GcmSender;
import com.google.gson.Gson;
import com.sun.deploy.net.HttpRequest;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * EventController
 * <p/>
 * Controls HTTP Request related with Event invoked from Devices
 * Not fully implemented yet.
 */
public class EventController implements AjouinoServer.HTTPInterface {

    private DeviceCatalog deviceCatalog;
    private Gson gson = new Gson();

    public EventController() {
        deviceCatalog = SystemFacade.getInstance().getDeviceCatalog();
    }

    @Override
    public NanoHTTPD.Response processRequest(NanoHTTPD.Method method, String[] uriParams, Map<String, String> httpParams) {

        String command = null;
        String operand = null;
        String operand2 = null;
        String postData = null;
        String eventData = null;
        if (uriParams != null) {
            try {
                if (uriParams.length > 0 && !uriParams[0].isEmpty()) command = uriParams[0];
                if (uriParams.length > 1 && !uriParams[1].isEmpty()) operand = uriParams[1];
                if (uriParams.length > 2 && !uriParams[2].isEmpty()) operand2 = uriParams[2];
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /device/id/");
            }
        }

        postData = httpParams.get("postData");
        eventData = httpParams.get("event");

        switch (method) {
            case GET:
                if (command.equalsIgnoreCase("event")) {
                    if (operand != null && operand.equalsIgnoreCase("image")) {
                        if (operand2 != null) {
                            // GET "/event/image/eventid"
                            // command = "event"
                            // operand = "image"
                            // operand2 = "eventid" which is timestamp of the event.

                            try {
                                // get path of stored image
                                String path = getClass().getResource("/").getPath() + "image/" + operand2;
                                InputStream is = new FileInputStream(new File(path));

                                // return stream of image
                                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, HttpRequest.JPEG_MIME_TYPE, is);

                            } catch (Exception e) {
                                e.printStackTrace();
                                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "NOT FOUND: Image " + operand2);
                            }
                        }
                    }
                }

            case POST:
                if (command.equalsIgnoreCase("event")) try {
                    // POST "/event/"
                    // post event without image

                    // parse event from postData
                    if (eventData != null) {
                        Event event = gson.fromJson(eventData, Event.class);

                        // send GCM push notification
                        GcmSender.sendMessage(event.toString());

                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to send push notification.");
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: Failed to send push notification.");

                }
                else {
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: POST /event/");
                }

            case PUT:
                if (command.equalsIgnoreCase("event")) try {
                    // PUT "/event/"
                    // put event with image

                    // parse event from postData
                    Event event = null;
                    if (eventData != null) event = gson.fromJson(eventData, Event.class);

                    // check whether the received event is from registered device or not
                    Device device = null;
                    if (event != null)
                        device = SystemFacade.getInstance().getDeviceCatalog().getDevice(event.getDeviceID());
                    if (device != null) {
                        // send GCM Push Notification
                        GcmSender.sendMessage(gson.toJson(event));

                        // get temp file path
                        Path tempFilePath = Paths.get(httpParams.get("content"));

                        // create "/image/" directory if not exist
                        String path = getClass().getResource("/").getPath() + "image/";
                        File outputDir = new File(path);
                        if (!outputDir.exists()) outputDir.mkdir();

                        // create output file
                        File outputFile = new File(path + event.getTimestamp().getTime());
                        if (!outputFile.exists()) outputFile.createNewFile();

                        // write to output file
                        Files.copy(tempFilePath, new FileOutputStream(outputFile));
                        deviceCatalog.addEvent(event);

                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OK");

                    } else {
                        // return NOT FOUND response
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "NOT FOUND: Device not found related with event.");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Content not resolvable.");
                }

            default:
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. use GET, POST, PUT method only.");
        }

    }

}
