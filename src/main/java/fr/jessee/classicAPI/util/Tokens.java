package fr.jessee.classicAPI.util;

public class Tokens {
    private final String access_token;
    private final String refresh_token;

    public Tokens(String access_token, String refresh_token) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }

    public String getAccessToken() {
        return access_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }
}
