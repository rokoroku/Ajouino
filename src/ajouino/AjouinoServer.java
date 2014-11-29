package ajouino;

import ajouino.controllers.DeviceController;
import ajouino.controllers.SystemController;
import ajouino.controllers.UserController;
import ajouino.model.DeviceInfo;
import ajouino.model.User;
import ajouino.services.SessionManager;
import ajouino.services.SystemServiceFacade;
import com.google.gson.Gson;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Ajouino HTTP server.
 *
 *
 */
public class AjouinoServer extends NanoHTTPD {

    private DeviceController deviceController;
    private UserController userController;
    private SystemController systemController;
    private SessionManager sessionManager;

    public AjouinoServer() {
        super(80);

        sessionManager = SystemServiceFacade.getInstance().getSessionManager();

        deviceController = new DeviceController();
        userController = new UserController();
        systemController = new SystemController();
        SystemServiceFacade.getInstance().getUserManager().putUser(new User("admin", "1234"));

    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> httpParams = session.getParms();

        // put remote host address into params
        String remoteAddress = new String(session.getHeaders().get("remote-addr").getBytes());
        if(httpParams.get("address") == null) {
            httpParams.put("address", remoteAddress);
        }

        // put body contents into params
        if(method.equals(Method.PUT) || method.equals(Method.POST)) try {
            session.parseBody(httpParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get user from created session
        // or create a new session if valid authentication header is given
        User user = sessionManager.getUserFromSession(remoteAddress);
        if (user == null) {
            String authHeader = session.getHeaders().get("authorization");
            user = getUserFromHeader(authHeader);
            if (user != null) {
                sessionManager.createSession(user, remoteAddress);
            } else {
                return new Response(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "Unauthorized");
            }
        }

        System.out.println(method + " " + uri + " " + httpParams.get("postData"));
        String[] splittedUri = uri.split("/");

        Response response = null;
        HTTPInterface controller = null;
        if (splittedUri.length > 1) {

            // select an appropriate controller
            if (splittedUri[1].startsWith("device")) {
                controller = (HTTPInterface) deviceController;
            } else if (splittedUri[1].startsWith("user")) {
                controller = (HTTPInterface) userController;
            } else if (splittedUri[1].equalsIgnoreCase("session")) {
                controller = (HTTPInterface) systemController;
            }

            // route to the controller
            if (controller != null) {
                String uriParams[] = Arrays.copyOfRange(splittedUri, 1, splittedUri.length);
                response = controller.processRequest(method, uriParams, httpParams);
            } else {
                response = new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Bad request");
            }

        } else {
            // index page response
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

//        System.out.println("   > " + response.getData());
        return response;
    }

    private User getUserFromHeader(String authHeader) {
        if(authHeader != null && !authHeader.isEmpty()) {
            authHeader = authHeader.split(" ")[1];
            String userId = Base64.base64Decode(authHeader).split(":")[0];
            User user = SystemServiceFacade.getInstance().getUserManager().getUser(userId);
            if (user != null && user.authenticate(authHeader)) {
                return user;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        ServerRunner.run(AjouinoServer.class);
    }

    public static interface HTTPInterface {
        public Response processRequest(Method method, String[] uriParams, Map<String, String> httpParams);
    }
}
