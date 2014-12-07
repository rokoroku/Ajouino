/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ajouino.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author YoungRok
 */
public class JdbcAdapter {

    //database connection information
    private static final String DB_FILE = "ajouino.db";

    private static Connection connection;
    private static boolean isOpened = false;
    static {
        try {
            connection = openConnection();
        } catch (SQLException ex) {
            Logger.getLogger(JdbcAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public interface SQLObjectInteface {
        public String toSQL();
        public String getPrimaryKey();
    }
    
    //get a connection
    public static Connection openConnection() throws SQLException {
        Connection conn = null;

        SQLiteConfig config = new SQLiteConfig();
        String filepath = JdbcAdapter.class.getClass().getResource("/").getPath();
        conn = DriverManager.getConnection("jdbc:sqlite:/" + filepath + DB_FILE, config.toProperties());

        System.out.println("Connected to database");
        isOpened = true;

        return conn;
    }

    public static int updateRecord(String relation, String projection, String condition) throws SQLException {
        Statement state = connection.createStatement();
        String query = new StringBuilder()
                .append("UPDATE ")
                .append(relation)
                .append(" SET ").append(projection)
                .append(" WHERE ").append(condition)
                .append(";").toString();

        return state.executeUpdate(query);
    }

    //create single record with record object    
    public static int insertRecord(String relation, SQLObjectInteface record) throws SQLException {
        Statement state = connection.createStatement();
        String query = new StringBuilder()
                .append("INSERT into ")
                .append(relation)
                .append(" ").append(record.toSQL())
                .append(";").toString();

        return state.executeUpdate(query);
    }

    //create batch records with record object    
    public static int insertRecords(String relation, Collection recordCollection) throws SQLException {
        Statement state = connection.createStatement();

        for (Object record : recordCollection) {
            if (record instanceof SQLObjectInteface) {
                String query = new StringBuilder()
                        .append("INSERT IGNORE into ")
                        .append(relation)
                        .append(" ").append(((SQLObjectInteface) record).toSQL())
                        .append(";").toString();
                state.addBatch(query);
            }
        }

        int[] result = state.executeBatch();
        int count = 0;
        for (int i : result) {
            count += i;
        }

        return count;
    }

    //select record
    public static ResultSet selectRecords(String projection, String relation, String condition) throws SQLException {
        Statement state = connection.createStatement();

        StringBuilder sb = new StringBuilder()
                .append("SELECT ").append(projection)
                .append(" FROM ").append(relation);
        if (condition != null) {
            sb.append(" WHERE ").append(condition);
        }

        String query = sb.append(';').toString();
        return state.executeQuery(query);
    }

    //delete record
    public static int deleteRecord(String relation, String condition) throws SQLException {
        Statement state = connection.createStatement();

        StringBuilder sb = new StringBuilder()
                .append("DELETE FROM ").append(relation);
        if (condition != null) {
            sb.append(" WHERE ").append(condition);
        }

        String query = sb.append(';').toString();
        return state.executeUpdate(query);
    }

    //execute raw query
    public static ResultSet executeQuery(String query) throws SQLException {
        Statement state = connection.createStatement();
        return state.executeQuery(query);
    }

    //create table
    public static int createTable(String relation, String definition) throws SQLException {
        Statement state = connection.createStatement();
        String query = new StringBuilder()
                .append("CREATE TABLE IF NOT EXISTS ")
                .append(relation)
                .append(" (").append(definition).append(")")
                .append(";").toString();
        return state.executeUpdate(query);
    }

    public static boolean isOpened() {
        return isOpened;
    }

}
