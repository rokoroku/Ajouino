package ajouino;

import ajouino.controller.DeviceController;
import ajouino.controller.EventController;
import ajouino.controller.UserController;
import ajouino.model.Device;
import ajouino.model.User;
import ajouino.service.SessionManager;
import ajouino.service.SystemFacade;
import ajouino.util.AuthUtils;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;

import java.util.Arrays;
import java.util.Map;

/**
 * Ajouino HTTP server.
 */
public class AjouinoServer extends NanoHTTPD {

    public AjouinoServer() {
        super(80);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> httpParams = session.getHeaders();

        // put remote host address into params
        String remoteAddress = new String(session.getHeaders().get("remote-addr").getBytes());
        if (httpParams.get("address") == null) {
            httpParams.put("address", remoteAddress);
        }

        // put body contents into params
        if (method.equals(Method.PUT) || method.equals(Method.POST)) try {
            session.parseBody(httpParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get user from created session
        // or create a new session if valid authentication header is given
        SessionManager sessionManager = SystemFacade.getInstance().getSessionManager();
        User user = sessionManager.getUserFromSession(remoteAddress);
        if (user == null) {
            String authHeader = session.getHeaders().get("authorization");
            if(authHeader != null) {
                String username = AuthUtils.getUsernameFromHeader(authHeader);
                user = SystemFacade.getInstance().getUserCatalog().getUser(username);
            }
            if (user != null) {
                sessionManager.createSession(user, remoteAddress);
            } else {
                // Get device from remote address
                Device device = SystemFacade.getInstance().getDeviceCatalog().getDeviceByAddress(remoteAddress);
                if (device == null) {
                    return new Response(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "Unauthorized");
                }
            }
        }

        System.out.println(method + " " + uri + " " + ((httpParams.get("postData") != null) ? httpParams.get("postData") : ""));
        String[] splittedUri = uri.split("/");

        Response response = null;
        HTTPInterface controller = null;
        if (splittedUri.length > 1) {

            // select an appropriate controller
            controller = getController(splittedUri[1]);

            // route to the controller
            if (controller != null) {
                String uriParams[] = Arrays.copyOfRange(splittedUri, 1, splittedUri.length);
                response = controller.processRequest(method, uriParams, httpParams);
            } else {
                response = new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Bad request");
            }

        } else {
            // default index page response
            System.out.println(method + " '" + uri + "' ");
            String msg = "<html><body><h1>Hello server</h1>\n";
            Map<String, String> parms = session.getParms();
            if (parms.get("username") == null) {
                msg
                        += "<form action='?' method='get'>\n"
                        + "  <p>Your name: <input type='text' name='username'></p>\n"
                        + "</form>\n";
            } else {
                msg += "<p>Hello, " + parms.get("username") + "!</p>";
            }

            msg += "</body></html>\n";
            response = new Response(msg);
        }

        return response;
    }


    public HTTPInterface getController(String route) {
        HTTPInterface controller = null;

        if (route.startsWith("device")) {
            controller = DeviceController.getInstance();
        } else if (route.startsWith("user")) {
            controller = UserController.getInstance();
        } else if (route.equalsIgnoreCase("event")) {
            controller = EventController.getInstance();
        }

        return controller;
    }

    public static interface HTTPInterface {
        public Response processRequest(Method method, String[] uriParams, Map<String, String> httpParams);
    }
}
