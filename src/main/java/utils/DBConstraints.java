package utils;

public final class DBConstraints {
    private DBConstraints(){}

    public static class OracleConstraints{

        public static final String URL = "jdbc:oracle:thin:@192.168.0.230:1521:bdb";
        public static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
        public static final String USER = "bgdcl786";
        public static final String PASSWORD = "bgdcl7861#";
    }
}
