package com.iict.sms;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ASUS
 */
public class SmsSender {

    public static void main(String[] args) throws SQLException {
        Connection connection = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.230:1521:bdb", "bgdcl786", "bgdcl7861#");
        } catch (Exception e) {
            System.out.println("JDBC Driver not found! " + e.getMessage());
            return;
        }

        if (connection != null) {
//            System.out.println("Connected to Database!");

            String selectQuery = "SELECT count(*) rowNumber FROM SMS_TO_SEND";

            String selectAllValuesQuery = "SELECT * FROM SMS_TO_SEND order by id desc";

            int rowNumber = 0;

            try {
                Statement stmt = connection.createStatement();
                Statement stmt2 = connection.createStatement();
                Statement stmt3 = connection.createStatement();
                ResultSet rs = stmt.executeQuery(selectQuery);

                while (rs.next()) {
                    rowNumber = Integer.parseInt(rs.getString("rowNumber"));

//                    System.out.println("Number of rows- " +rowNumber);

                }

                ResultSet rs2 = stmt.executeQuery(selectAllValuesQuery);

                while (rs2.next() && rowNumber > 0) {

                    // find the record
                    String id = rs2.getString("id");
                    String phone = rs2.getString("phone_number");
                    String message = rs2.getString("message");
                    // String entryTime = rs2.getString("entry_time");
                    String custCode = rs2.getString("cust_code");

                    // send sms
                    sendSMS(phone, message);

                    // insert into sms_sent
                    String insertQuery = "INSERT INTO SMS_SENT (ID,\n" +
                            "                              PHONE_NUMBER,\n" +
                            "                              MESSAGE,\n" +
                            "                              SENT_TIME,\n" +
                            "                              CUST_CODE)\n" +
                            "     VALUES ( ( (SELECT NVL (MAX (id), 0) + 1 FROM SMS_SENT)),\n" +
                            "             '" + phone + "',\n" +
                            "             '" + message + "',\n" +
                            "             SYSTIMESTAMP,\n" +
                            "             '" + custCode + "')";

                    stmt2.executeUpdate(insertQuery);

                    // delete from sms_to_send
                    String deleteQuery = "DELETE FROM SMS_TO_SEND WHERE ID = '" + id + "'";
                    stmt3.executeUpdate(deleteQuery);

                    rowNumber--;

                }

                stmt.close();
                stmt2.close();
                stmt3.close();
                connection.commit();
                // System.out.println("Successfully sent sms to " + rowNumber);

            } catch (SQLException ex) {
                Logger.getLogger(SmsSender.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("Failed to make connection!");
        }
    }

//    public static void main(String[] args) {
//        sendSMS("01918288265", "Hello, This is Test SMS. \nYour Due is 1000 BDT");
//    }

    public static void sendFailed(String phone, String message) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found!");
            return;
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@192.168.0.230:1521:bdb", "bgdcl786", "bgdcl7861#");
        } catch (SQLException e) {
            System.out.println("Failed to Connect to Database!");
            return;
        }

        if (connection != null) {
            System.out.println("Connected to Database!");

            int rowNumber = 0;

            try {
                Statement stmt = connection.createStatement();

                // insert into sms_sent
                String insertQuery = "INSERT INTO SMS_FAILED (ID,\n" +
                        "                              PHONE_NUMBER,\n" +
                        "                              MESSAGE,\n" +
                        "                              FAILED_TIME\n" +
                        "     VALUES ( ( (SELECT NVL (MAX (id), 0) + 1 FROM SMS_FAILED)),\n" +
                        "             '" + phone + "',\n" +
                        "             '" + message + ",\n" +
                        "             SYSTIMESTAMP )";

                stmt.executeUpdate(insertQuery);
                stmt.close();
                connection.commit();

            } catch (SQLException ex) {
                Logger.getLogger(SmsSender.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("Failed to make connection!");
        }
    }

    public static void disableSSL() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static String sendSMS(String msisdn, String sms_string) {
        disableSSL();
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://bulksms.teletalk.com.bd/link_sms_send.php?op=SMS&user=BGDCLCUM&pass=BgDclcu@R87&mobile=" + msisdn + "&charset=ASCII&sms=" + sms_string;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        if(response.getStatusCode().is2xxSuccessful() && response.getBody().contains("<reply>SUCCESS")){

        }else {
            sendFailed(msisdn, sms_string);
        }
        return "Successful";
    }
}
