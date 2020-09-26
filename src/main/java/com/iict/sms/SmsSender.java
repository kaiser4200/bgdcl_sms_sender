package com.iict.sms;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ASUS
 */
public class SmsSender {

    public static void main(String[] args) throws SQLException {
        Connection connection = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@192.168.0.230:1521:bdb", "bgdcl786", "bgdcl7861#");
        } catch (Exception e ) {
            System.out.println("JDBC Driver not found! "+ e.getMessage());
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

                while ( rs.next() ) {
                    rowNumber = Integer.parseInt(rs.getString("rowNumber"));

//                    System.out.println("Number of rows- " +rowNumber);

                }

                ResultSet rs2 = stmt.executeQuery(selectAllValuesQuery);

                while(rs2.next() && rowNumber > 0){

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
                            "             '"+ phone +"',\n" +
                            "             '"+ message +"',\n" +
                            "             SYSTIMESTAMP,\n" +
                            "             '"+ custCode +"')";

                    stmt2.executeUpdate(insertQuery);

                    // delete from sms_to_send
                    String deleteQuery = "DELETE FROM SMS_TO_SEND WHERE ID = '"+ id +"'";
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

    public static void sendFailed(String phone, String message) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e ) {
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
                        "             '"+ phone +"',\n" +
                        "             '"+ message +",\n"+
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

    public static String sendSMS(String msisdn, String sms_string) {
        try
        {
            //Creating a Client
            Client client = Client.create();
            String url = "http://sms.sslwireless.com/pushapi/dynamic/server.php?msisdn=" + URLEncoder.encode(msisdn, "UTF-8") + "&sms=" + URLEncoder.encode(sms_string, "UTF-8") + "&user=" + URLEncoder.encode("bgdclapi", "UTF-8") + "&pass=" + URLEncoder.encode("93P494j=", "UTF-8") + "&csmsid=" + URLEncoder.encode("1234567890", "UTF-8") + "&sid=" + URLEncoder.encode("NiceITBGDCL", "UTF-8");
            WebResource webResource = client
                    .resource(url); //?msisdn=8801911763079&sms=SMS&user=sgcl&pass=A1r3i5f7&sid=KGDCL&csmsid=123456789"
            //Format of the response accepted
            ClientResponse response = webResource.accept("application/xml")
                    .get(ClientResponse.class);
            //Check for the response is success or not
            if (response.getStatus() != 200)
            {
                // throw new Exception("Exception Occured - HTTP Error Code : "
                // response.getStatus());
                sendFailed(msisdn, sms_string);
            }

            // String output = response.getEntity(String.class);

            // System.out.println("Fetching Output....");
            // System.out.println(output);

        } catch (UniformInterfaceException e)
        {
            System.out.println(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
         }
        return "Successful";
    }

}
