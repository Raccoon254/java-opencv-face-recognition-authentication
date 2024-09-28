package org.raccoon;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.HashMap;
import java.util.Map;

public class FaceRecognitionSystem {
    private Map<String, User> registeredUsers = new HashMap<>();
    private CascadeClassifier faceDetector;

    public FaceRecognitionSystem(CascadeClassifier faceDetector) {
        this.faceDetector = faceDetector;
    }

    public void registerUser(String username, String password, Mat faceData) {
        registeredUsers.put(username, new User(username, password, faceData));
    }

    public boolean loginUser(String username, String password) {
        User user = registeredUsers.get(username);
        return user != null && user.getPassword().equals(password);
    }

    public String recognizeFace(Mat face) {
        for (User user : registeredUsers.values()) {
            double similarity = compareImages(face, user.getFaceData());
            if (similarity > 0.8) {
                return user.getUsername();
            }
        }
        return null;
    }

    private double compareImages(Mat img1, Mat img2) {
        Mat diff = new Mat();
        Core.absdiff(img1, img2, diff);
        Mat grayDiff = new Mat();
        Imgproc.cvtColor(diff, grayDiff, Imgproc.COLOR_BGR2GRAY);
        Scalar sum = Core.sumElems(grayDiff);
        double totalPixels = grayDiff.rows() * grayDiff.cols();
        return 1 - (sum.val[0] / (totalPixels * 255));
    }
}
