package utils;

import com.iict.sms.enums.ExternalApiResponseEnum;
import connection.DbConnect;
import model.SmsToSend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public static void failedDataInsert(long id , String phone , String message , String url , String response, String bgdclLog){
        try{
            Connection connection = DbConnect.getConnect();
            PreparedStatement preStatement = connection.prepareStatement(getSmsFailedInsertQuery(phone, message , url , response, bgdclLog));
            preStatement.executeUpdate();
            deleteSmsToSendData(id);
            preStatement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
