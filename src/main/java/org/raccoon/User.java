package org.raccoon;
import org.opencv.core.Mat;

public class User {
    private String username;
    private String password;
    private Mat faceData;

    public User(String username, String password, Mat faceData) {
        this.username = username;
        this.password = password;
        this.faceData = faceData;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Mat getFaceData() {
        return faceData;
    }
}
