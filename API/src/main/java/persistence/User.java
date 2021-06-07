package persistence;

public class User {
    private String userName;
    private String userId;
    private String password; //may be hashed later
    private String pubKey;
    private String privKey;

    public User() {
    }

    public User(String userName, String userId, String password, String pubKey, String privKey) {
        this.userName = userName;
        this.userId = userId;
        this.password = password;
        this.pubKey = pubKey;
        this.privKey = privKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrivKey() {
        return privKey;
    }

    public void setPrivKey(String privKey) {
        this.privKey = privKey;
    }
}
