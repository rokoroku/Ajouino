/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.service;

import ajouino.persistent.DeviceCatalog;
import ajouino.persistent.UserCatalog;

/**
 *
 * @author YoungRok
 */
public class SystemFacade {

    private static SystemFacade instance;

    private SessionManager sessionManager;
    private DeviceCatalog deviceCatalog;
    private UserCatalog userCatalog;

    private SystemFacade() {
    }

    public static SystemFacade getInstance() {
        if (instance == null) {
            instance = new SystemFacade();
        }
        return instance;
    }

    public SessionManager getSessionManager() {
        if (sessionManager == null) {
            sessionManager = new SessionManager();
        }
        return sessionManager;
    }

    public DeviceCatalog getDeviceCatalog() {
        if (deviceCatalog == null) {
            deviceCatalog = new DeviceCatalog();
        }
        return deviceCatalog;
    }

    public UserCatalog getUserCatalog() {
        if (userCatalog == null) {
            userCatalog = new UserCatalog();
        }
        return userCatalog;
    }

    
}
