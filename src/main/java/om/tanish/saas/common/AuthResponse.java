package om.tanish.saas.common;

public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private long expiresIn;

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.tokenType = "Bearer";
        this.expiresIn = 3600;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}