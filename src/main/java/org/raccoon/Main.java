package org.raccoon;

import com.formdev.flatlaf.FlatDarkLaf;
import org.opencv.core.Core;

import javax.swing.*;

public class Main {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.createAndShowGUI();
            OpenCVManager.initializeOpenCV(gui);
            FaceRecognitionSystem system = new FaceRecognitionSystem(gui);
            gui.setFaceRecognitionSystem(system);
        });
    }
}
