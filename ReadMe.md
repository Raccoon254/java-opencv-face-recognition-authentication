# Java Face Recognition

This project implements a face recognition system using Java and OpenCV. The application captures video from a webcam and detects faces in real time.

<div>
   <video autoplay loop muted controls width="100%" style="border-radius: 20px">
     <source src="./static/video.mp4" type="video/mkv">
   </video>
</div>

## Requirements

- **Java Development Kit (JDK)**: Make sure you have JDK 11 or higher installed.
- **Apache Maven**: Used for project management and dependencies.
- **OpenCV**: Ensure you have OpenCV installed and the Java bindings are set up.

## Setup

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd JavaFaceRecognition
   ```

2. **Install Dependencies**:
   The project uses Maven to manage dependencies. You can download the required libraries by running:
   ```bash
   mvn clean package
   ```

3. **Configure OpenCV**:
    - Download OpenCV from the official [OpenCV website](https://opencv.org/releases/).
    - Extract the OpenCV package.
    - Set the `java.library.path` to the OpenCV Java libraries directory when running the application.

## Running the Application

1. **Compile and Package**:
   After installing dependencies, ensure the project builds successfully:
   ```bash
   mvn clean package
   ```

2. **Run the Application**:
   Use the following command to run the application:
   ```bash
   java -Djava.library.path=C:\OPENCV\opencv\build\java\x64 -cp target/JavaFaceRecognition-1.0-SNAPSHOT-shaded.jar org.raccoon.Main
   ```

   Replace `C:\OPENCV\opencv\build\java\x64` with the path to your OpenCV Java libraries.

## Important Files

- **`face.xml`**: This is the Haar Cascade classifier file used for face detection. It is loaded dynamically at runtime.
- **`src/main/java/org/raccoon/Main.java`**: The main class containing the application logic.
- **`pom.xml`**: Maven configuration file that defines dependencies and build settings.

## Troubleshooting

- If you encounter issues with loading `face.xml`, ensure that the file is included in the `src/main/resources` directory.
- Make sure your camera is accessible and not used by another application.