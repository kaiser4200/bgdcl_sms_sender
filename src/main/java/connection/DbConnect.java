package connection;

import utils.DBConstraints;

import java.sql.Connection;
import java.sql.DriverManager;

import static utils.DBConstraints.OracleConstraints.DRIVER;


public class DbConnect {
    private static Connection conn = null;

    private DbConnect() {
    }
    public static Connection getConnect() {
        try {
            if (conn == null) {
                Class.forName(DRIVER);
                conn = DriverManager.getConnection( DBConstraints.OracleConstraints.URL, DBConstraints.OracleConstraints.USER, DBConstraints.OracleConstraints.PASSWORD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}