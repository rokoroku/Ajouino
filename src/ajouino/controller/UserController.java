/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.controller;

import ajouino.AjouinoServer;
import ajouino.model.User;
import ajouino.service.SessionManager;
import ajouino.service.SystemFacade;
import ajouino.persistent.UserCatalog;
import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import java.util.Map;

/**
 * UserController
 * <p/>
 * Controls HTTP Request related with User
 * Not fully implemented yet.
 */
public class UserController implements AjouinoServer.HTTPInterface {

    private UserCatalog userCatalog;
    private SessionManager sessionManager;
    private Gson gson = new Gson();

    public UserController() {
        userCatalog = SystemFacade.getInstance().getUserCatalog();
        sessionManager = SystemFacade.getInstance().getSessionManager();
    }

    /**
     * Controls user REST methods
     *
     * @param method HTTP methods e.g. GET, PUT, POST, DELETE
     * @param uri    given uri "/user/id/", ["user", "id"]
     * @param param  given param "?addr=127.0.0.1&password=ajouino", [<"addr","127.0.0.1">, <"password", "ajouino">]
     * @return Response can be json, html, etc.
     */
    @Override
    public NanoHTTPD.Response processRequest(NanoHTTPD.Method method, String[] uri, Map<String, String> param) {
        //request : "/user/id/attr/"
        //uri : ["id", "attr"]

        User invokeUser = sessionManager.getUserFromSession(param.get("address"));

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
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /user/id/");
            }
        }
        if (method.equals(NanoHTTPD.Method.POST)) {
            postData = param.get("postData");
        } else if (method.equals(NanoHTTPD.Method.PUT)) {
            postData = param.get("content");
        }

        switch (method) {
            case GET:
                if(invokeUser == null) {

                } else {
                    //TODO: implement permission
                    if (command.equalsIgnoreCase("users")) {
                        if(invokeUser.getId().equals("admin")) {
                            String respond = gson.toJson(userCatalog.getUsers());
                            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, respond);
                        } else {
                            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Access Denied");
                        }

                    } else if (command.equalsIgnoreCase("user") && operand != null) {
                        if(operand.equalsIgnoreCase("authenticate")) {
                            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OK");
                        } else {
                            //userId is presented.
                            //String respond = gson.toJson(userCatalog.getUser(operand));
                            //return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, respond);
                            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Access Denied");
                        }
                    } else {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: GET /users/ or /user/id");
                    }
                }
            case POST:
                if (operand == null) {
                    User user  = gson.fromJson(postData, User.class);
                    return postUser(user);

                } else {
                    User user = userCatalog.getUser(operand);
                    if (user != null) {
                        user = gson.fromJson(postData, User.class);
                        return postUser(user);
                    } else {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "User not found.");
                    }
                }
                //return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: POST /user/");

            case PUT:
                if (operand != null) {
                    User user = userCatalog.getUser(operand);
                    if(user.getId().equals(invokeUser.getId())) {
                        if (user != null) {
                            user = gson.fromJson(postData, User.class);
                            return putUser(user);
                        } else {
                            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "User not found.");
                        }
                    } else {
                        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "Access Denied");
                    }
                } else {
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: PUT /user/");
                }

            case DELETE:
                if (operand != null) {
                    User user = userCatalog.getUser(operand);
                    return deleteUser(user);

                } else {
                    return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. Usage: DELETE /user/id");
                }

            default:
                return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error. use GET, POST, PUT, DELETE methods only.");
        }
        //return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQUEST: Syntax error.");
    }


    private NanoHTTPD.Response postUser(User user) {

        if(user != null && user.getId() != null) {
            userCatalog.putUser(user);
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
        } else {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQEUST: posted data is not allowed." );
        }
    }
    
    private NanoHTTPD.Response putUser(User user) {

        if(user != null && user.getId() != null) {
            userCatalog.putUser(user);
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
        } else {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "BAD REQEUST: posted data is not allowed." );
        }
    }
    
    private NanoHTTPD.Response deleteUser(User user) {
        if(user != null) {
            user = userCatalog.removeUser(user.getId());
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, gson.toJson(user));
        } else {
            return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON, "");
        }
    }
}
