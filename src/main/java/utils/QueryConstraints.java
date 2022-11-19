package utils;

public final class QueryConstraints {
    private QueryConstraints(){}

    public static class SmsToSendQueries{
        public static final String GET_ALL = "select * from sms_to_send";
        public static final String DELETE_BY_ID = "delete from SMS_TO_SEND where ID = ?";
        public static String getSmsSentInsertQuery(String phone, String message, String custCode){
            return "insert into SMS_SENT (ID, PHONE_NUMBER, MESSAGE, SENT_TIME, CUST_CODE)VALUES ((SELECT NVL (MAX (id), 0) + 1 FROM SMS_SENT), '"+ phone +"' , '"+ message +"',SYSTIMESTAMP , '"+ custCode +"')";
        }

        public static String getSmsFailedInsertQuery(String phone, String message){
            return "insert into SMS_FAILED (ID, PHONE_NUMBER, MESSAGE, Failed_TIME)VALUES ((SELECT NVL (MAX (id), 0) + 1 FROM SMS_FAILED), '"+ phone +"' , '"+ message +"', SYSTIMESTAMP)";
        }
    }
    public static class SmsEmailNotificationQueries{
        public static final String GET_ALL_PENDING = "select * from sms_email_notification where type=? and status=?";
        public static final String UPDATE_PENDING = "update SMS_EMAIL_NOTIFICATION set status =?  where id =?";
    }
}
