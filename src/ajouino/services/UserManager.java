/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.services;

import ajouino.model.User;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author YoungRok
 */
public class UserManager {

    Map<String, User> userMap;

    public UserManager() {
        userMap = new ConcurrentHashMap<String, User>();
    }

    public User getUser(String userId) {
        return userMap.get(userId);
    }
    
    public Collection<User> getUsers() {
        return userMap.values();
    }

    public void putUser(User user) {
        if (user != null && user.getUserID() != null) {
            userMap.put(user.getUserID(), user);
        }
    }

    public User removeUser(String userId) {
        return userMap.remove(userId);
    }
}
