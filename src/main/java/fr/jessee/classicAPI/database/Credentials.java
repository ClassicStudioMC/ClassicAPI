package fr.jessee.classicAPI.database;

import org.jetbrains.annotations.NotNull;

public class Credentials {
    private final String HOST;
    private final String USER;
    private final String PASS;
    private final String DB_NAME;
    private final String PORT;

    public Credentials(
            @NotNull final String HOST,
            @NotNull final String USER,
            @NotNull final String PASS,
            @NotNull final String DB_NAME,
            @NotNull final String PORT) {

        this.HOST = HOST;
        this.USER = USER;
        this.PASS = PASS;
        this.DB_NAME = DB_NAME;
        this.PORT = PORT;
    }

    public String toURI() {

        return "jdbc:mysql://" +
                this.HOST +
                ":" +
                this.PORT +
                "/" +
                this.DB_NAME;
    }

    public String getUSER() {
        return USER;
    }

    public String getPASS() {
        return PASS;
    }
}
