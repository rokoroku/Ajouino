/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.services;

import ajouino.controllers.*;

/**
 *
 * @author YoungRok
 */
public class SystemServiceFacade {

    private static SystemServiceFacade instance;

    private SessionManager sessionManager;
    private DeviceManager deviceManager;
    private UserManager userManager;

    private SystemServiceFacade() {
    }

    public static SystemServiceFacade getInstance() {
        if (instance == null) {
            instance = new SystemServiceFacade();
        }
        return instance;
    }

    public SessionManager getSessionManager() {
        if (sessionManager == null) {
            sessionManager = new SessionManager();
        }
        return sessionManager;
    }

    public DeviceManager getDeviceManager() {
        if (deviceManager == null) {
            deviceManager = new DeviceManager();
        }
        return deviceManager;
    }

    public UserManager getUserManager() {
        if (userManager == null) {
            userManager = new UserManager();
        }
        return userManager;
    }

    
}
