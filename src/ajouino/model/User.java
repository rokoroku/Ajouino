/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.model;

import ajouino.util.JdbcAdapter;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class User implements JdbcAdapter.SQLObjectInteface {

    String id;
    String password;
    String gcmId;
    short permission;

    public User(String Id) {
        this.id = Id;
        this.permission = 777;

    }

    public User(String Id, String password) {
        this.id = Id;
        this.setPassword(password);
        this.permission = 777;
    }

    public boolean authenticate(String passwordDigest) {
        return this.password.equals(passwordDigest);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        try {
            this.password = new String(Base64.encode(new String(id + ":" + password).getBytes("UTF-8")));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setPasswordDigest(String passwordDigest) {
        password = passwordDigest;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

    public short getPermission() {
        return permission;
    }

    public void setPermission(short permission) {
        this.permission = permission;
    }

    @Override
    public String toSQL() {
        StringBuilder sb = new StringBuilder();

        sb.append("VALUES (")
                .append((id != null) ? "'" + id + "'" : "null").append(", ")
                .append((password != null) ? "'" + password + "'" : "null").append(", ")
                .append((gcmId != null) ? "'" + gcmId + "'" : "null").append(", ")
                .append("'" + permission + "'")
                .append(")");
        return sb.toString();
    }

    @Override
    public String getPrimaryKey() {
        return id;
    }
}
