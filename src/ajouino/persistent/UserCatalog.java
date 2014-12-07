/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.persistent;

import ajouino.model.User;
import ajouino.util.JdbcAdapter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author YoungRok
 */
public class UserCatalog {

    Map<String, User> userMap;

    public UserCatalog() {
        userMap = new ConcurrentHashMap<String, User>();
        createTable();
        reloadData();
    }

    public User getUser(String userId) {
        return userMap.get(userId);
    }
    
    public Collection<User> getUsers() {
        return userMap.values();
    }

    public boolean putUser(User user) {
        if (user != null && user.getId() != null) {
            try {
                if(!JdbcAdapter.isOpened()) JdbcAdapter.openConnection();

                if(userMap.containsKey(user.getId())) {
                    updateUser(user);
                } else {
                    JdbcAdapter.insertRecord("USER", user);
                    userMap.put(user.getId(), user);
                }
                return true;
            } catch (SQLException ex) {
                Logger.getLogger(UserCatalog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public User updateUser(User user) {

        String relation = "USER";
        String condition = USER_COLUMN_ID + "='" + user.getId() + "'";

        StringBuilder stringBuilder = new StringBuilder();
        if (user.getGcmId() != null)
            stringBuilder.append(USER_COLUMN_GCM_TOKEN + "='").append(user.getGcmId()).append("', ");
        if (user.getPassword() != null)
            stringBuilder.append(USER_COLUMN_PASSWORD + "='").append(user.getPassword()).append("', ");
        if (user.getPermission() > 0)
            stringBuilder.append(USER_COLUMN_PERMISSION + "='").append(user.getPermission()).append("', ");

        String values = stringBuilder.toString();
        if (values.endsWith(", ")) values = values.substring(0, values.lastIndexOf(","));

        try {
            JdbcAdapter.updateRecord(relation, values, condition);

            User originalUser = getUser(user.getId());
            if (user.getGcmId() != null)
                originalUser.setGcmId(user.getGcmId());
            if (user.getPassword() != null)
                originalUser.setPasswordDigest(user.getPassword());
            if (user.getPermission() > 0)
                originalUser.setPermission(user.getPermission());

            return originalUser;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User removeUser(String userId) {
        User user = userMap.remove(userId);
        try {
            String relation = "USER";
            String condition = USER_COLUMN_ID + "='" + userId +"'";
            JdbcAdapter.deleteRecord(relation, condition);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public final static String USER_COLUMN_ID = "Id";
    public final static String USER_COLUMN_PASSWORD = "Password";
    public final static String USER_COLUMN_GCM_TOKEN = "Gcm_id";
    public final static String USER_COLUMN_PERMISSION = "Permission";

    private boolean createTable() {
        try {
            String relation = "USER";
            String definition = USER_COLUMN_ID + " VARCHAR(15) NOT NULL,"
                    + USER_COLUMN_PASSWORD + " VARCHAR(50) NOT NULL,"
                    + USER_COLUMN_GCM_TOKEN + " VARCHAR(200),"
                    + USER_COLUMN_PERMISSION + " INT,"
                    + "PRIMARY KEY (" + USER_COLUMN_ID + ")";
            JdbcAdapter.createTable(relation, definition);

            System.out.println("table USER has been created.");
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(UserCatalog.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }


    public void reloadData() {
        if(userMap == null) userMap = new ConcurrentHashMap<String, User>();
        else userMap.clear();

        try {
            String relation = "USER";
            String projection = "*";
            String condition = null;
            ResultSet resultSet = JdbcAdapter.selectRecords(projection, relation, condition);
            ResultSetMetaData metaData = resultSet.getMetaData();
            while (resultSet.next()) {
                String userId = resultSet.getString(1);
                String userPW = resultSet.getString(2);
                String gcmKey = resultSet.getString(3);
                Integer permission = resultSet.getInt(4);

                User user = new User(userId);
                if(userPW != null) user.setPasswordDigest(userPW);
                if(gcmKey != null) user.setGcmId(gcmKey);
                if(permission != null) user.setPermission(permission.shortValue());

                userMap.put(userId, user);
            }
            System.out.println("Users have been loaded from DB.");

        } catch (SQLException ex) {
            Logger.getLogger(UserCatalog.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
