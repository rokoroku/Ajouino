/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

import com.sun.xml.internal.messaging.saaj.util.Base64;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class User {

    String userID;
    String passwordDigest;
    String gcmAddress;
    short permission;

    public User(String userID, String password) {
        this.userID = userID;
        this.setPassword(password);
        this.permission = 1;
    }

    public boolean authenticate(String passwordDigest) {
        return this.passwordDigest.equals(passwordDigest);
    }
    
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPasswordDigest() {
        return passwordDigest;
    }

    public void setPassword(String password) {
        try {
            this.passwordDigest = new String(Base64.encode(new String(userID + ":" + password).getBytes("UTF-8")));            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getGcmAddress() {
        return gcmAddress;
    }

    public void setGcmAddress(String gcmAddress) {
        this.gcmAddress = gcmAddress;
    }

    public short getPermission() {
        return permission;
    }

    public void setPermission(short permission) {
        this.permission = permission;
    }

}
