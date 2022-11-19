package utils;

import connection.DbConnect;
import model.SmsToSend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static utils.QueryConstraints.SmsToSendQueries.DELETE_BY_ID;
import static utils.QueryConstraints.SmsToSendQueries.getSmsFailedInsertQuery;

public final class SmsToSendUtil {
    private SmsToSendUtil(){

    }

    public static List<SmsToSend> getAllPendingSms() {
        try {
            Connection connection = DbConnect.getConnect();
            PreparedStatement preStatement = connection.prepareStatement(QueryConstraints.SmsToSendQueries.GET_ALL);
            ResultSet resultSet = preStatement.executeQuery();
            List<SmsToSend> list = new ArrayList<>();
            while (resultSet.next()) {
                SmsToSend smsToSend = new SmsToSend();
                smsToSend.setId(resultSet.getLong("id"));
                smsToSend.setMessage(resultSet.getString("message"));
                smsToSend.setPhoneNo(resultSet.getString("PHONE_NUMBER"));
                smsToSend.setCustCode(resultSet.getString("CUST_CODE"));
                list.add(smsToSend);
            }
            preStatement.close();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void dataInsert(List<SmsToSend> list){

        for (SmsToSend smsToSend : list) {

            long id = smsToSend.getId();
            String message = smsToSend.getMessage();
            String phone = smsToSend.getPhoneNo();
            String custCode = smsToSend.getCustCode();

            Boolean isSent = ExternalApiCallUtil.sendSMS(phone , message);

            if(isSent) {
                sendDataInsert(id , phone , message, custCode);
            } else {
                failedDataInsert(id , phone , message);
            }
        }

    }

    public static void sendDataInsert(long id , String phone , String message , String custCode){
        try{
            Connection connection = DbConnect.getConnect();
            PreparedStatement preStatement = connection.prepareStatement(QueryConstraints.SmsToSendQueries.getSmsSentInsertQuery(phone, message, custCode));
            preStatement.executeUpdate();
            deleteSmsToSendData(id);
            preStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteSmsToSendData(long id){
        try{
        Connection connection = DbConnect.getConnect();
        PreparedStatement preStatement = connection.prepareStatement(DELETE_BY_ID);
        preStatement.setLong(1, id);
        preStatement.executeQuery();
        preStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void failedDataInsert(long id , String phone , String message){
        try{
            Connection connection = DbConnect.getConnect();
            PreparedStatement preStatement = connection.prepareStatement(getSmsFailedInsertQuery(phone, message));
            preStatement.executeUpdate();
            deleteSmsToSendData(id);
            preStatement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
