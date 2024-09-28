package org.raccoon;

import org.opencv.core.Mat;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private static final Map<String, User> registeredUsers = new HashMap<>();

    public static void registerUser(String username, String password, Mat faceData) {
        registeredUsers.put(username, new User(username, password, faceData));
    }

    public static User getUser(String username) {
        return registeredUsers.get(username);
    }

    public static boolean isUserRegistered(String username) {
        return registeredUsers.containsKey(username);
    }

    public static String findMatchingUser(Mat face) {
        for (Map.Entry<String, User> entry : registeredUsers.entrySet()) {
            double similarity = OpenCVManager.compareImages(face, entry.getValue().getFaceData());
            if (similarity > 0.8) {
                return entry.getKey();
            }
        }
        return null;
    }
}
