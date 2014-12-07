package ajouino.util;

import ajouino.model.User;
import ajouino.service.SystemFacade;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by YoungRok on 2014-12-04.
 */
public class GcmSender {

    private final static String SENDER_KEY = "AIzaSyDAZuE43lgDWZX9JZzAjRKvL4g37eVF5Z4";
    private final static int RETRY_COUNT = 5;

    private static Sender sender = null;

    public static void sendMessage(String data) throws IOException {
        if (sender == null) sender = new Sender(SENDER_KEY);
        final Message message = new Message.Builder().addData("msg", data).build();

        Collection<User> Users = SystemFacade.getInstance().getUserCatalog().getUsers();
        for(User user : Users) {
            String regId = user.getGcmId();
            if(regId != null) {
                sender.send(message, regId, RETRY_COUNT);
                System.out.println("Message{" + message.toString() + "} sent to " + regId);
            } else {
                System.out.println("User " + user.getId() + " does not contain any gcmID");
            }
        }
    }

}
