package org.raccoon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel imageLabel;
    private JProgressBar progressBar;
    private JPanel loadingPanel;
    private JPanel mainPanel;
    private FaceRecognitionSystem faceRecognitionSystem;

    public void createAndShowGUI() {
        frame = new JFrame("kenTom AI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        createLoadingPanel();
        createMainPanel();

        frame.add(loadingPanel);
        frame.setVisible(true);

        simulateLoading();
    }

    private void createLoadingPanel() {
        loadingPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        loadingPanel.add(progressBar, BorderLayout.SOUTH);
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel logoPanel = new JPanel(new BorderLayout());
        ImageIcon logo = new ImageIcon("logo.png");
        Image scaledLogo = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoPanel.add(logoLabel, BorderLayout.CENTER);
        logoPanel.add(Box.createRigidArea(new Dimension(0, 100)), BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");
        JButton scanFaceButton = new JButton("Scan Face");

        Dimension buttonSize = new Dimension(120, 30);
        registerButton.setPreferredSize(buttonSize);
        loginButton.setPreferredSize(buttonSize);
        scanFaceButton.setPreferredSize(buttonSize);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        controlPanel.add(new JLabel("Username:"), gbc);
        gbc.gridy = 1;
        controlPanel.add(usernameField, gbc);
        gbc.gridy = 2;
        controlPanel.add(new JLabel("Password:"), gbc);
        gbc.gridy = 3;
        controlPanel.add(passwordField, gbc);
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        controlPanel.add(registerButton, gbc);
        gbc.gridx = 1;
        controlPanel.add(loginButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        controlPanel.add(scanFaceButton, gbc);

        registerButton.addActionListener(e -> faceRecognitionSystem.register());
        loginButton.addActionListener(e -> faceRecognitionSystem.login());
        scanFaceButton.addActionListener(e -> faceRecognitionSystem.scanFace());

        leftPanel.add(logoPanel, BorderLayout.NORTH);
        leftPanel.add(controlPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(640, 480));
        rightPanel.add(imageLabel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.add(new JLabel("kenTom AI - Face Recognition System"));

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void simulateLoading() {
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

    public void updateImage(BufferedImage image) {
        SwingUtilities.invokeLater(() -> imageLabel.setIcon(new ImageIcon(image)));
    }

    public JFrame getFrame() { return frame; }
    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public void setFaceRecognitionSystem(FaceRecognitionSystem system) { this.faceRecognitionSystem = system; }
}