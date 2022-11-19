package utils;

import connection.DbConnect;
import model.SmsEmailNotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public final class SmsEmailNotificationUtil {

    private SmsEmailNotificationUtil(){}

    public static List<SmsEmailNotification> getAllSmsPending() {
        try {
            Connection connection = DbConnect.getConnect();
            PreparedStatement preStatement = connection.prepareStatement(QueryConstraints.SmsEmailNotificationQueries.GET_ALL_PENDING);
            preStatement.setString(1, "SMS");
            preStatement.setString(2, "PENDING");
            ResultSet resultSet = preStatement.executeQuery();
            List<SmsEmailNotification> list = new ArrayList<>();
            while (resultSet.next()) {
                SmsEmailNotification smsEmailNotification = new SmsEmailNotification();
                smsEmailNotification.setId(resultSet.getLong("id"));
                smsEmailNotification.setMessage(resultSet.getString("message"));
                smsEmailNotification.setType(resultSet.getString("type"));
                smsEmailNotification.setEmail(resultSet.getString("email"));
                smsEmailNotification.setMobile(resultSet.getString("mobile"));
                smsEmailNotification.setSubject(resultSet.getString("subject"));
                smsEmailNotification.setStatus(resultSet.getString("status"));

                list.add(smsEmailNotification);
            }
            preStatement.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void updatePendingList(List<SmsEmailNotification> list) {
        try {
            for (SmsEmailNotification smsEmailNotificationModel : list) {
              Boolean isSmsSent = ExternalApiCallUtil.sendSMS(smsEmailNotificationModel.getMobile(), smsEmailNotificationModel.getMessage());
              String status = "SENT";
              if(!isSmsSent){
                  status = "FAILED";
              }
              updateSmsEmailNotification(smsEmailNotificationModel.getId(), status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void updateSmsEmailNotification(long id, String status){
        try {
            Connection connection = DbConnect.getConnect();
            PreparedStatement preStatement = connection.prepareStatement(QueryConstraints.SmsEmailNotificationQueries.UPDATE_PENDING);
            preStatement.setString(1, status);
            preStatement.setLong(2, id);
            preStatement.executeUpdate();
            preStatement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
