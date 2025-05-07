package fr.jessee.classicAPI.util;

public class YamlConfig {
    public Server server;
    public Database database;
    public jwt jwt;

    public static class Server {
        public String port;
        public String keyStoreName;
        public String keyStorePassword;
    }

    public static class Database {
        public String host;
        public String user;
        public String pass;
        public String dbName;
        public String port;
    }

    public static class jwt {
        public String secret;
    }
}
