package ajouino.util;

import ajouino.model.Device;
import ajouino.model.User;
import ajouino.service.SystemFacade;
import com.sun.xml.internal.messaging.saaj.util.Base64;

/**
 * Created by YoungRok on 2014-12-07.
 */
public class AuthUtils {
    public static String getUsernameFromHeader(String authHeader) {
        if(authHeader != null && !authHeader.isEmpty()) {
            authHeader = authHeader.split(" ")[1];
            String userId = Base64.base64Decode(authHeader).split(":")[0];
            return userId;
        }
        return null;
    }

    public static String generateBasicAuthHeader(String username, String password) {
        String credential = username + ":" + password;
        String basicAuth = "Basic " + java.util.Base64.getEncoder().encodeToString(credential.getBytes());
        return basicAuth;
    }
}
