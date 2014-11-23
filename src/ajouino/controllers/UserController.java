/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controllers;

import ajouino.model.User;
import ajouino.services.UserManager;
import ajouino.services.SystemServiceFacade;
import ajouino.util.HTTPInterface;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author YoungRok
 */
public class UserController implements HTTPInterface {

    private UserManager userManager;

    public UserController() {
        userManager = SystemServiceFacade.getInstance().getUserManager();
    }

    @Override
    public NanoHTTPD.Response processRequest(NanoHTTPD.Method method, String[] uri, Map<String, String> param) {
        //request : "/user/id/attr/"
        //uri : ["id", "attr"]

        String userId = null;
        String attr = null;
        String value = null;

        try {
            if (uri[0] != null && !uri[0].isEmpty()) {
                userId = uri[0];
            }
            if (uri.length > 0) {
                attr = uri[1];
            }
            if (uri.length > 1) {
                value = uri[2];
            }
        } catch (Exception e) {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /user/id/");
        }

        Gson gson = new Gson();
        switch (method) {
            case GET:
                if (userId == null) {
                    //return all users
                    Collection<User> users = userManager.getUsers();
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(users));
                } else {
                    //userId is presented.
                    User user = userManager.getUser(userId);
                    if (user != null) {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
                    } else {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "user not found : " + userId);
                    }
                }

            case POST:
                if (userId != null) {
                    //userId is presented.
                    //TODO:: update user with attrs
                    User user = userManager.getUser(userId);
                    if (user != null) {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
                    } else {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "user not found : " + userId);
                    }
                } else {
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: POST /user/id/pin/value");
                }

            case PUT:
                if(userId != null) {
                    //TODO: add user (userId will be address)
                    User user = new User(userId, new Date().toString());
                    user.setGcmAddress(user.toString());
                    userManager.putUser(user);
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
                } else {
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: PUT /user/inetaddress");
                }
    
            case DELETE:
                if(userId != null) {
                    //TODO: add user (userId will be address)
                    User user = userManager.removeUser(userId);
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
                } else {
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: DELETE /user/id");
                }

            
            default:
                break;
        }
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error.");
    }
}
