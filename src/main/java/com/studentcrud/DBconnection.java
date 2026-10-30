package com.studentcrud;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBconnection {

    public static Connection getConnection() {
        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/studentdb",
                "root",
                "Priyarana123"   // <-- change to your MySQL password
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return con;
    }
}