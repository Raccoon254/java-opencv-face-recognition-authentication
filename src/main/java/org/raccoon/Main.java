package org.raccoon;

import com.formdev.flatlaf.FlatDarkLaf;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static final String CASCADE_FILE = "face.xml";
    private static final Map<String, UserData> registeredUsers = new HashMap<>();

    private static JFrame frame;
    private static JTextField usernameField;
    private static JPasswordField passwordField;
    private static JLabel imageLabel;
    private static VideoCapture capture;
    private static CascadeClassifier faceDetector;

    private static JProgressBar progressBar;
    private static JPanel loadingPanel;
    private static JPanel mainPanel;

    private static class UserData {
        String password;
        Mat faceData;

        UserData(String password, Mat faceData) {
            this.password = password;
            this.faceData = faceData;
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel( new FlatDarkLaf() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }

        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
            initializeOpenCV();
        });
    }

    private static void createAndShowGUI() {
        frame = new JFrame("kenTom AI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        // Create loading panel
        loadingPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setSize(800, 600);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);

        // Create main panel
        mainPanel = new JPanel(new BorderLayout());

        // Left panel for controls
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Logo panel
        JPanel logoPanel = new JPanel(new BorderLayout());
        ImageIcon logo = new ImageIcon("logo.png");
        Image scaledLogo = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);

        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));

        logoPanel.add(logoLabel, BorderLayout.CENTER);
        //Add top margin
        logoPanel.add(Box.createRigidArea(new Dimension(0, 100)), BorderLayout.NORTH);

        // Control panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(15);
        usernameField.setSize(200, 30);
        passwordField = new JPasswordField(15);
        passwordField.setSize(200, 30);
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");
        JButton scanFaceButton = new JButton("Scan Face");

        Dimension buttonSize = new Dimension(120, 30);
        registerButton.setPreferredSize(buttonSize);
        loginButton.setPreferredSize(buttonSize);
        scanFaceButton.setPreferredSize(buttonSize);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        controlPanel.add(new JLabel("Username:"), gbc);
        gbc.gridy = 1;
        controlPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        controlPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 3;
        controlPanel.add(passwordField, gbc);

        gbc.gridy = 4; gbc.gridwidth = 1;
        controlPanel.add(registerButton, gbc);
        gbc.gridx = 1;
        controlPanel.add(loginButton, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        controlPanel.add(scanFaceButton, gbc);

        registerButton.addActionListener(e -> register());
        loginButton.addActionListener(e -> login());
        scanFaceButton.addActionListener(e -> scanFace());

        leftPanel.add(logoPanel, BorderLayout.NORTH);
        leftPanel.add(controlPanel, BorderLayout.CENTER);

        // Right panel for camera feed
        JPanel rightPanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(640, 480));
        rightPanel.add(imageLabel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.add(new JLabel("kenTom AI - Face Recognition System"));

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        frame.add(loadingPanel);
        frame.setVisible(true);

        // Simulate loading
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(20);
                    final int progress = i;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                }
                SwingUtilities.invokeLater(() -> {
                    frame.remove(loadingPanel);
                    frame.add(mainPanel);
                    frame.revalidate();
                    frame.repaint();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void initializeOpenCV() {
        faceDetector = new CascadeClassifier(CASCADE_FILE);
        capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(frame, "Error opening camera", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        new Thread(() -> {
            Mat frame = new Mat();
            while (true) {
                capture.read(frame);
                if (!frame.empty()) {
                    Mat processedFrame = processFrame(frame);
                    BufferedImage image = matToBufferedImage(processedFrame);
                    //Scale down the image to fit the screen


                    SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
                }
                try {
                    // 60fps
                    Thread.sleep(3);  // ~30 fps
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static Mat processFrame(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            // Draw rectangle around the face
            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);

            // Extract the face
            Mat face = new Mat(frame, rect);
            Imgproc.resize(face, face, new Size(100, 100));

            // Check if the face matches any registered user
            String matchedUser = findMatchingUser(face);
            if (matchedUser != null) {
                // Draw the user's name above the rectangle
                Imgproc.putText(frame, matchedUser, new Point(rect.x, rect.y - 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, new Scalar(0, 255, 0), 2);
            }
        }

        return frame;
    }

    private static String findMatchingUser(Mat face) {
        for (Map.Entry<String, UserData> entry : registeredUsers.entrySet()) {
            double similarity = compareImages(face, entry.getValue().faceData);
            if (similarity > 0.8) { // Adjust threshold as needed
                return entry.getKey();
            }
        }
        return null;
    }

    private static void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mat frame = new Mat();
        capture.read(frame);

        Mat face = detectAndExtractFace(frame, faceDetector);
        if (face != null) {
            registeredUsers.put(username, new UserData(password, face));
            JOptionPane.showMessageDialog(null, "User registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "No face detected. Registration failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter both username and password", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!registeredUsers.containsKey(username)) {
            JOptionPane.showMessageDialog(null, "User not found. Please register first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserData userData = registeredUsers.get(username);
        if (!userData.password.equals(password)) {
            JOptionPane.showMessageDialog(null, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(null, "Password correct. Please click 'Scan Face' to complete login.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void scanFace() {
        String username = usernameField.getText().trim();
        if (username.isEmpty() || !registeredUsers.containsKey(username)) {
            JOptionPane.showMessageDialog(null, "Please enter a valid username and click 'Login' first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Mat frame = new Mat();
        capture.read(frame);

        Mat face = detectAndExtractFace(frame, faceDetector);
        if (face != null) {
            Mat registeredFace = registeredUsers.get(username).faceData;
            double similarity = compareImages(face, registeredFace);

            if (similarity > 0.8) { // Adjust threshold as needed
                JOptionPane.showMessageDialog(null, "Face recognized. Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Face doesn't match. Login failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "No face detected. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Mat detectAndExtractFace(Mat frame, CascadeClassifier faceDetector) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections);

        Rect[] faces = faceDetections.toArray();
        if (faces.length > 0) {
            Rect faceRect = faces[0];
            Mat face = new Mat(frame, faceRect);
            Imgproc.resize(face, face, new Size(100, 100));
            return face;
        }
        return null;
    }

    private static double compareImages(Mat img1, Mat img2) {
        Mat diff = new Mat();
        Core.absdiff(img1, img2, diff);
        Mat grayDiff = new Mat();
        Imgproc.cvtColor(diff, grayDiff, Imgproc.COLOR_BGR2GRAY);
        Scalar sum = Core.sumElems(grayDiff);
        double totalPixels = grayDiff.rows() * grayDiff.cols();
        return 1 - (sum.val[0] / (totalPixels * 255));
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}