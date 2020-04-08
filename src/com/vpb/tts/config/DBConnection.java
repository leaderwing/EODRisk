/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vpb.tts.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author quynx
 */
public class DBConnection {

    private static String JDBC_CONNECTION_URL
            = "jdbc:sqlserver://10.37.17.35:1433";
    private static DBConnection dbConenction;
    private static Connection conn ;

    private DBConnection() {
    }
    public static DBConnection getInstance() {
        if (dbConenction == null) {
            dbConenction = new DBConnection();
        }
        return dbConenction;
    }

    public static Connection getCon(String url, String username, String password) throws FileNotFoundException, IOException, SQLException {
        if(conn == null || conn.isClosed()) {  
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(url,username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
        return conn;
    }
}
