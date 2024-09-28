package org.raccoon;

import org.opencv.core.Mat;
import javax.swing.*;

public class FaceRecognitionSystem {
    private GUI gui;

    public FaceRecognitionSystem(GUI gui) {
        this.gui = gui;
    }

    public void register() {
        String username = gui.getUsernameField().getText().trim();
        String password = new String(gui.getPasswordField().getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mat frame = new Mat();
        OpenCVManager.getCapture().read(frame);

        Mat face = OpenCVManager.detectAndExtractFace(frame);
        if (face != null) {
            UserManager.registerUser(username, password, face);
            JOptionPane.showMessageDialog(null, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No face detected. Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void login() {
        String username = gui.getUsernameField().getText().trim();
        String password = new String(gui.getPasswordField().getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!UserManager.isUserRegistered(username)) {
            JOptionPane.showMessageDialog(null, "User not found. Please register first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = UserManager.getUser(username);
        if (!user.getPassword().equals(password)) {
            JOptionPane.showMessageDialog(null, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, "Password correct. Please click 'Scan Face' to complete login.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void scanFace() {
        String username = gui.getUsernameField().getText().trim();
        if (username.isEmpty() || !UserManager.isUserRegistered(username)) {
            JOptionPane.showMessageDialog(null, "Please enter a valid username and click 'Login' first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mat frame = new Mat();
        OpenCVManager.getCapture().read(frame);

        Mat face = OpenCVManager.detectAndExtractFace(frame);
        if (face != null) {
            User user = UserManager.getUser(username);
            double similarity = OpenCVManager.compareImages(face, user.getFaceData());

            if (similarity > 0.8) {
                JOptionPane.showMessageDialog(null, "Face recognized. Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Face doesn't match. Login failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "No face detected. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}