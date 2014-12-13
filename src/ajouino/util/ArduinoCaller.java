/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.util;

import ajouino.model.Device;
import ajouino.model.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author YoungRok
 */
public class ArduinoCaller {

    public static class ArduinoCallException extends Exception {

        int errorCode;

        public ArduinoCallException(int errorCode, String string) {
            super(string);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    public static String requestInformation(Device device) throws ArduinoCallException {
        StringBuilder sb = new StringBuilder();
        String address = device.getAddress();
        if (!address.startsWith("http://")) sb.append("http://");
        sb.append(address).append("/arduino/hello/0");

        String authHeader = AuthUtils.generateBasicAuthHeader("root", device.getPassword());
        return invoke(sb.toString(), authHeader);
    }

    public static void unregisterDevice(Device device) throws ArduinoCallException {
        StringBuilder sb = new StringBuilder();
        String address = device.getAddress();
        if (!address.startsWith("http://")) sb.append("http://");
        sb.append(address).append("/arduino/bye/0");

        String authHeader = AuthUtils.generateBasicAuthHeader("root", device.getPassword());
        invoke(sb.toString(), authHeader);
    }

    public static String invokeEvent(Device device, Event event) throws ArduinoCallException {
        String uri = createRequestUri(device, event);
        String authHeader = AuthUtils.generateBasicAuthHeader("root", device.getPassword());
        return invoke(uri, authHeader);
    }

    public static String invoke(String uri, String authHeader) throws ArduinoCallException {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            System.out.println("invoke arduino event : " + uri);

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("User-Agent", "Ajouino");
            if (authHeader != null) connection.setRequestProperty("Authorization", authHeader);

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
                    throw new ArduinoCallException(responseCode, "Unauthorized");
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new ArduinoCallException(responseCode, "Not found");
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    throw new ArduinoCallException(responseCode, "Bad request");
                case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
                    throw new ArduinoCallException(responseCode, "Client timeout");
            }
        } catch (SocketTimeoutException ex ) {
            Logger.getLogger(ArduinoCaller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ArduinoCaller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ArduinoCaller.class.getName()).log(Level.SEVERE, null, ex);
        }
        throw new ArduinoCallException(HttpURLConnection.HTTP_UNAVAILABLE, "Unavailable");
    }

    /**
     * build url like :
     * http://root:arduino@192.168.0.104/arduino/digital/13/0
     *
     * @param device device to invoke an event
     * @param event  event to invoke
     * @return Returned string
     */
    private static String createRequestUri(Device device, Event event) {
        StringBuilder sb = new StringBuilder();
        if (!device.getAddress().startsWith("http://")) {
            sb.append("http://");
        }
        sb.append(device.getAddress()).append("/arduino/");

        if (event != null && event.getType() != null) {
            sb.append(event.getType()).append("/");
            if (event.getValue() != null) {
                if (event.getType().equals("color")) sb.append(String.format("%08X", event.getValue()));
                else sb.append(event.getValue());
            }
        }
        return sb.toString();
    }

}
