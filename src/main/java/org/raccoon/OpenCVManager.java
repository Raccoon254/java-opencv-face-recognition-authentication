package org.raccoon;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class OpenCVManager {
    private static final String CASCADE_FILE = "/face.xml";
    private static CascadeClassifier faceDetector;
    private static VideoCapture capture;

    public static void initializeOpenCV(GUI gui) {
        try {
            loadCascadeClassifier(gui);
            startVideoCapture(gui);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(gui.getFrame(), "Error initializing OpenCV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loadCascadeClassifier(GUI gui) throws IOException {
        URL xmlUrl = OpenCVManager.class.getResource(CASCADE_FILE);
        if (xmlUrl == null) {
            throw new IOException("Could not find face detection file!");
        }

        File tempFile = File.createTempFile("face", ".xml");
        tempFile.deleteOnExit();

        try (InputStream in = xmlUrl.openStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());
    }

    private static void startVideoCapture(GUI gui) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(gui.getFrame(), "Error opening camera", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        new Thread(() -> {
            Mat frame = new Mat();
            while (true) {
                capture.read(frame);
                if (!frame.empty()) {
                    Mat processedFrame = processFrame(frame);
                    BufferedImage image = matToBufferedImage(processedFrame);
                    gui.updateImage(image);
                }
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static Mat processFrame(Mat frame) {
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(frame, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0), 2);
            Mat face = new Mat(frame, rect);
            Imgproc.resize(face, face, new Size(100, 100));
            String matchedUser = UserManager.findMatchingUser(face);
            if (matchedUser != null) {
                Imgproc.putText(frame, matchedUser, new Point(rect.x, rect.y - 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, new Scalar(0, 255, 0), 2);
            }
        }

        return frame;
    }

    public static Mat detectAndExtractFace(Mat frame) {
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

    public static double compareImages(Mat img1, Mat img2) {
        Mat diff = new Mat();
        Core.absdiff(img1, img2, diff);
        Mat grayDiff = new Mat();
        Imgproc.cvtColor(diff, grayDiff, Imgproc.COLOR_BGR2GRAY);
        Scalar sum = Core.sumElems(grayDiff);
        double totalPixels = grayDiff.rows() * grayDiff.cols();
        return 1 - (sum.val[0] / (totalPixels * 255));
    }

    public static BufferedImage matToBufferedImage(Mat mat) {
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

    public static VideoCapture getCapture() {
        return capture;
    }
}
