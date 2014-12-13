package ajouino;

import ajouino.model.User;
import ajouino.service.SystemFacade;
import fi.iki.elonen.ServerRunner;

/**
 * Created by YoungRok on 2014-12-07.
 */
public class main {

    public static void main(String[] args) {

        // create default user if not exist (admin)
        if(SystemFacade.getInstance().getUserCatalog().getUser("admin") == null) {
            SystemFacade.getInstance().getUserCatalog().putUser(new User("admin", "1234"));
        }

        // run Ajouino Server
        ServerRunner.run(AjouinoServer.class);
    }

}
