package ajouino;

import ajouino.controllers.DeviceController;
import ajouino.controllers.SystemController;
import ajouino.controllers.UserController;
import ajouino.model.DeviceInfo;
import ajouino.model.User;
import ajouino.services.SessionManager;
import ajouino.services.SystemServiceFacade;
import ajouino.util.HTTPInterface;
import com.google.gson.Gson;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;

import java.util.Map;

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
        super(8080);

        sessionManager = SystemServiceFacade.getInstance().getSessionManager();

        deviceController = new DeviceController();
        userController = new UserController();
        systemController = new SystemController();
        SystemServiceFacade.getInstance().getUserManager().putUser(new User("admin", "1234"));
        
        DeviceInfo info = new DeviceInfo("1", "powerstrip", "192.168.0.1", "powerstrip");
        info.getValues().put(13, 1);
        info.getValues().put(14, 1);
        
        Gson gson = new Gson();
        System.out.println(gson.toJson(info));
        
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Map<String, String> params = session.getParms();
        String remoteAddress = new String(session.getHeaders().get("remote-addr").getBytes());
        if(params.get("address") == null) params.put("address", remoteAddress);

        User user = sessionManager.getUserFromSession(remoteAddress);
        if (user == null) try {
            //TODO: 유저 인증 필요
            String authToken = session.getHeaders().get("authorization");
            authToken = authToken.split(" ")[1];
            if(authToken != null && !authToken.isEmpty()) {
                String userId = Base64.base64Decode(authToken).split(":")[0];
                user = SystemServiceFacade.getInstance().getUserManager().getUser(userId);
                if (user != null && user.authenticate(authToken)) {
                    sessionManager.createSession(user, remoteAddress);
                } else {
                    return new Response(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "Unauthorized");
                }
            } else {
                return new Response(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "Unauthorized");
            }
        } catch (Exception e ){ 
            return new Response(Response.Status.UNAUTHORIZED, NanoHTTPD.MIME_PLAINTEXT, "Unauthorized");            
        }

        System.out.println(method + " '" + uri + "' ");
        String[] splittedUri = uri.split("/", 3);
        //uri : "/device/id/pin/value"
        //spliited : ["", "device", "id/pin/value"]

        Response response;
        if (splittedUri.length > 1) {
            HTTPInterface controller = null;
            if (splittedUri[1].equalsIgnoreCase("device")) {
                controller = (HTTPInterface) deviceController;
            } else if (splittedUri[1].equalsIgnoreCase("user")) {
                controller = (HTTPInterface) userController;
            } else if (splittedUri[1].equalsIgnoreCase("system")) {
                controller = (HTTPInterface) systemController;
            }

            if (controller != null) {
                response = controller.processRequest(method, splittedUri[2].split("/"), params);
            } else {
                response = new Response(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Bad request");
            }
        } else {
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

    public static void main(String[] args) {
        ServerRunner.run(AjouinoServer.class);
    }

}
