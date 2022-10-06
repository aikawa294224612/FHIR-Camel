package com.polly.example;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTest {

	public static void main(String[] args) {
		
		Connection conn = null;
		 
        try {
        	
        	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
            String dbURL = "jdbc:sqlserver://POLLYPENG-440\\SQLEXPRESS:49680;"
            		+ "encrypt=true;databaseName=Patient;"
            		+ "trustServerCertificate=true";
            String user = "pollypeng";
            String pass = "P@ssword";
            conn = DriverManager.getConnection(dbURL, user, pass);
            if (conn != null) {
                DatabaseMetaData dm = (DatabaseMetaData) conn.getMetaData();
                System.out.println("Driver name: " + dm.getDriverName());
                System.out.println("Driver version: " + dm.getDriverVersion());
                System.out.println("Product name: " + dm.getDatabaseProductName());
                System.out.println("Product version: " + dm.getDatabaseProductVersion());
                
                Statement selectStmt = conn.createStatement();
                ResultSet rs = selectStmt
                  .executeQuery("SELECT * FROM Inform");
                
                while(rs.next())
                {
                  System.out.println(rs.getString(1));  //First Column
                  System.out.println(rs.getString(2));  //Second Column
                  System.out.println(rs.getString(3));  //Third Column
                  System.out.println(rs.getString(4));  //Fourth Column
                }
            }
 
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

	}

}
