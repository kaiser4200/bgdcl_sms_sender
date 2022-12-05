package utils;

import com.iict.sms.enums.ExternalApiResponseEnum;
import connection.DbConnect;
import model.SmsToSend;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static utils.QueryConstraints.SmsToSendQueries.*;

public final class SmsToSendUtil {
    private SmsToSendUtil(){

    }

    public static List<SmsToSend> getAllPendingSms() {
        List<SmsToSend> list = new ArrayList<>();
        try {
            Connection connection = DbConnect.getConnect();
            if(connection != null){
                PreparedStatement preStatement = connection.prepareStatement(QueryConstraints.SmsToSendQueries.GET_ALL);
                ResultSet resultSet = preStatement.executeQuery();
                while (resultSet.next()) {
                    SmsToSend smsToSend = new SmsToSend();
                    smsToSend.setId(resultSet.getLong("id"));
                    smsToSend.setMessage(resultSet.getString("message"));
                    smsToSend.setPhoneNo(resultSet.getString("PHONE_NUMBER"));
                    smsToSend.setCustCode(resultSet.getString("CUST_CODE"));
                    list.add(smsToSend);
                }
                preStatement.close();
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return list;
        }
    }

    public static void dataInsert(List<SmsToSend> list){

        for (SmsToSend smsToSend : list) {
            long id = smsToSend.getId();
            String message = smsToSend.getMessage();
            String phone = smsToSend.getPhoneNo();
            String custCode = smsToSend.getCustCode();
            Map<String ,Object> map = ExternalApiCallUtil.sendSMS(phone , message);
            Boolean isSend = (Boolean) map.get(ExternalApiResponseEnum.IS_SENT.name());
            if(isSend) {
                sendDataInsert(id , phone , message, custCode);
            } else {
                String url = (String) map.get(ExternalApiResponseEnum.URL.name());
                String response = (String) map.get(ExternalApiResponseEnum.RESPONSE.name());
                String bgdclLog = (String) map.get(ExternalApiResponseEnum.BGDCL_LOG.name());
                failedDataInsert(id , phone , message , url , response, bgdclLog);
            }
        }
    }

    public static void sendDataInsert(long id , String phone , String message , String custCode){
        try{
            Connection connection = DbConnect.getConnect();
            if(connection != null){
                Long nextId = getNextId(TableConstraints.SMS_SENT);
                if(nextId != null){
                    PreparedStatement preStatement = getPreparedStatementOnResult(connection, SMS_SENT_INSERT, nextId, phone, message);
                    preStatement.setString(4, custCode);
                    preStatement.executeUpdate();
                    deleteSmsToSendData(id);
                    preStatement.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteSmsToSendData(long id){
        try{
        Connection connection = DbConnect.getConnect();
        if(connection != null){
            PreparedStatement preStatement = connection.prepareStatement(DELETE_BY_ID);
            preStatement.setLong(1, id);
            preStatement.executeQuery();
            preStatement.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void failedDataInsert(long id , String phone , String message , String url , String response, String bgdclLog){
        try{
            Connection connection = DbConnect.getConnect();
            if(connection != null){
                Long nextId = getNextId(TableConstraints.SMS_FAILED);
                if(nextId != null){
                    PreparedStatement preStatement = getPreparedStatementOnResult(connection, SMS_FAILED_INSERT, nextId, phone, message);
                    preStatement.setString(4, url);
                    preStatement.setString(5, response);
                    preStatement.setString(6, bgdclLog);
                    preStatement.executeUpdate();
                    deleteSmsToSendData(id);
                    preStatement.close();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PreparedStatement getPreparedStatementOnResult(Connection connection, String query, Long nextId, String phone, String message) throws SQLException {
        PreparedStatement preStatement = connection.prepareStatement(query);
        preStatement.setLong(1, nextId);
        preStatement.setString(2, phone);
        preStatement.setString(3, message);
        return preStatement;
    }

    private static long getCurrentTimestamp(){
        java.util.Date currentDate = new java.util.Date();
        return getTimestamp(currentDate);
    }

    private static long getTimestamp(java.util.Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        return date.getTime();
    }

    public static Long getNextId(TableConstraints tableConstraints){
        try {
            Long maxId = null;
            Connection connection = DbConnect.getConnect();
            if(connection != null){
                String query = QueryConstraints.SmsToSendQueries.getNextIdQuery(tableConstraints);
                PreparedStatement preStatement = connection.prepareStatement(query);
                ResultSet resultSet = preStatement.executeQuery();
                while (resultSet.next()) {
                    maxId = resultSet.getLong("NEXT_ID");
                }
                preStatement.close();
            }
            return maxId;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
