package utils;

public enum TableConstraints {
    SMS_SENT("SMS_SENT", "ID"),
    SMS_TO_SEND("SMS_TO_SEND", "ID"),
    SMS_FAILED("SMS_FAILED", "ID");
    private final String tableName;
    private final String idColumnName;
    TableConstraints(String tableName, String idColumnName){
        this.tableName = tableName;
        this.idColumnName = idColumnName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdColumnName() {
        return idColumnName;
    }
}
