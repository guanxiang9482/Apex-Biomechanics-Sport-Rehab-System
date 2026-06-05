package main.java.config;

public class DBConnection {
    private String jdbcURL;

    private DBConnection() {
        // Initialize the database connection here
    }

    private static class Holder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance(){
        return Holder.INSTANCE;
    }
}
