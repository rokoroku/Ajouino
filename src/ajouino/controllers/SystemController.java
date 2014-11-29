/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controllers;

import ajouino.AjouinoServer;
import fi.iki.elonen.NanoHTTPD;

import java.util.Map;

/**
 *
 * @author YoungRok
 */
public class SystemController implements AjouinoServer.HTTPInterface {

    public SystemController() {

    }

    @Override
    public NanoHTTPD.Response processRequest(NanoHTTPD.Method method, String[] uriParams, Map<String, String> httpParams) {
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
    }
}
