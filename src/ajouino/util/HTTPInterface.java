package ajouino.util;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author YoungRok
 */
public interface HTTPInterface {
    public Response processRequest(NanoHTTPD.Method method, String[] uri, Map<String, String> params);
}
