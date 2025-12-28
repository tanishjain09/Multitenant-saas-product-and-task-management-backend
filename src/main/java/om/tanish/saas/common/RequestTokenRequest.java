package om.tanish.saas.common;

public class RequestTokenRequest {
    private String refreshToken;
    public String getRefreshToken(){return refreshToken;}

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
