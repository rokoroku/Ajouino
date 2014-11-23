/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.util;

import ajouino.model.Device;
import ajouino.model.Event;
import fi.iki.elonen.NanoHTTPD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class ArduinoUtil {

    public static class ArduinoException extends Exception {

        int errorCode;

        public ArduinoException(int errorCode, String string) {
            super(string);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    /**
     * build url like :
     * http://root:arduino@192.168.0.104/arduino/digital/13/0  
     * 
     * @param device : device to invoke an event
     * @param event : event to invoke
     * @return : Returned string
     */
    public static String createRequestUri(Device device, Event event) {
        StringBuilder sb = new StringBuilder();
        if (!device.getAddress().startsWith("http://")) {
            sb.append("http://");
        }
        if (device.getPassword() != null) {
            sb.append("root:").append(device.getPassword()).append("@");
        }
        sb.append(device.getAddress()).append("/arduino/");

        if (event != null) {
            sb.append(event.getPinType()).append("/")
                    .append(event.getPin()).append("/")
                    .append(event.getValue());
        }
        return sb.toString();
    }

    public static String requestInformation(String address, String password) throws ArduinoException {
        StringBuilder sb = new StringBuilder();
        if (!address.startsWith("http://")) {
            sb.append("http://");
        }
        if (password != null) {
            sb.append("root:").append(password).append("@");
        }
        
        sb.append(address)
                .append("/arduino/hello/0");
        
        return invoke(sb.toString());
    }

    public static String invokeEvent(Device device, Event event) throws ArduinoException {
        return invoke(createRequestUri(device, event));
    }

    public static String invoke(String uri) throws ArduinoException {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setRequestProperty("User-Agent", "Ajouino");

            // get response
            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                    return response.toString();

                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new ArduinoException(responseCode, "Unauthorized");
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new ArduinoException(responseCode, "Not found");
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    throw new ArduinoException(responseCode, "Bad request");
                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                    throw new ArduinoException(responseCode, "Client timeout");
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArduinoUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArduinoUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new ArduinoException(HttpURLConnection.HTTP_UNAVAILABLE, "Unavailable");
    }
}
