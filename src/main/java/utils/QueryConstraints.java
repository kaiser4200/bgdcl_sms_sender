package utils;

public final class QueryConstraints {
    private QueryConstraints(){}

    public static class SmsToSendQueries{
        public static final String GET_ALL = "select * from sms_to_send";
        public static final String DELETE_BY_ID = "delete from SMS_TO_SEND where ID = ?";
        public static final String SMS_SENT_INSERT = "insert into SMS_SENT (ID, PHONE_NUMBER, MESSAGE, SENT_TIME, CUST_CODE)VALUES (?, ?, ?, ?, ?)";
        public static final String SMS_FAILED_INSERT = "insert into SMS_FAILED (ID, PHONE_NUMBER, MESSAGE, Failed_TIME, SMS_PROVIDER_REQUEST, SMS_PROVIDER_RESPONSE, BGDCL_LOG)VALUES (?, ?, ?, ?, ?, ?, ?)";

        public static String getNextIdQuery(TableConstraints tableConstraints){
            return "SELECT NVL (MAX ("+tableConstraints.getIdColumnName()+"), 0) + 1 as NEXT_ID FROM " + tableConstraints.getTableName();
        }

    }
    public static class SmsEmailNotificationQueries{
        public static final String GET_ALL_PENDING = "select * from sms_email_notification where type=? and status=?";
        public static final String UPDATE_PENDING = "update SMS_EMAIL_NOTIFICATION set status =?  where id =?";
    }
}
