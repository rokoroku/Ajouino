/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.service;

import ajouino.model.User;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author YoungRok
 */
public class SessionManager {

    final private Map<String, User> userSessionTable;
    final private Map<String, Timer> sessionTimerTable;

    public SessionManager() {
        userSessionTable = new ConcurrentHashMap<String, User>();
        sessionTimerTable = new ConcurrentHashMap<String, Timer>();
    }

    public User getUserFromSession(String inetAddress) {
        return userSessionTable.get(inetAddress);
    }
    
    public void createSession(final User user, final String inetAddress) {
        createSession(user, inetAddress, 30000);
    }

    public void createSession(final User user, final String inetAddress, long timeout) {
        if (!userSessionTable.containsKey(inetAddress)) {
            userSessionTable.put(inetAddress, user);
            System.out.println("create new session : " + user.getId() + "/" + inetAddress);
        }
        if (!sessionTimerTable.containsKey(inetAddress)) {
            Timer timer = new Timer(user.getId());
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    userSessionTable.remove(inetAddress);
                    sessionTimerTable.remove(inetAddress);
                    System.out.println("Session timeout : " + user.getId() + "/" + inetAddress);
                }
            };
            sessionTimerTable.put(inetAddress, timer);
            timer.schedule(tt, timeout);
        } else {
            Timer timer = sessionTimerTable.remove(inetAddress);
            timer.cancel();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    userSessionTable.remove(inetAddress);
                    sessionTimerTable.remove(inetAddress);
                    System.out.println("Session timeout : " + user.getId() + "/" + inetAddress);
                }
            };
            timer = new Timer(user.getId());
            sessionTimerTable.put(user.getId(), timer);
            timer.schedule(tt, timeout);
        }
    }
    
    public void removeSession(String inetAddress) {
        userSessionTable.remove(inetAddress);
        Timer timer = sessionTimerTable.get(inetAddress);
        if(timer != null) {
            timer.cancel();
            sessionTimerTable.remove(inetAddress);
        }
        
    }
}
