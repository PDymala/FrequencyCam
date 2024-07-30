package com.diplabs.frequencycam3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;

import static org.opencv.imgproc.Imgproc.INTER_AREA;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.resize;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "FreqencyCam3";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    private CustomCameraView javaCameraView;// JavaCameraView javaCameraView;

    private DFT dft;
    private Mat resizeimage;
    private Mat frame;
    private int spectrumStatus = 0;
    private int matrixStatus = 0;
    private int rotateStatus = 0;
    ImageButton imageButtonSpectrum;
    ImageButton imageButtonMatrix;
    ImageButton imageButtonSize;
    ImageButton imageButtonRotate;
    ImageButton imageButtonFlash;
    ImageButton imageButtonSave;
    ImageButton imageButtonClose;


    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaCameraView = findViewById(R.id.cameraView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        permisions();


        initializeButtons();



    }


    private void permisions() {
        // checking if the permission has already been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera(javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera(javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeCamera(CustomCameraView javaCameraView, int activeCamera) {


        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableFpsMeter();
        javaCameraView.enableView();

    }

    private void initializeButtons() {

        imageButtonSpectrum = findViewById(R.id.imageButtonSpectrum);
        imageButtonMatrix = findViewById(R.id.imageButtonMatrix);
        imageButtonSize = findViewById(R.id.imageButtonSize);
        imageButtonRotate = findViewById(R.id.imageButtonRotate);
        imageButtonFlash = findViewById(R.id.imageButtonFlash);
        imageButtonClose = findViewById(R.id.imageButtonClose);

    }


    public void changeRotateStatus(View view) {
        if (rotateStatus == 0) {
            rotateStatus = 1;
            //Phase domain

        } else {
            rotateStatus = 0;
            //Fourier domain

        }

    }

    public void changeSpectrumStatus(View view) {
        if (spectrumStatus == 0) {
            spectrumStatus = 1;
            //Phase domain

        } else if (spectrumStatus == 1) {
            spectrumStatus = 2;
            //Fourier domain

        } else {
            spectrumStatus = 0;
            //Fourier domain

        }

    }

    public void changeMatrixStatus(View view) {
        if (matrixStatus == 0) {
            matrixStatus = 1;

        } else {
            matrixStatus = 0;

        }
    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        dft = new DFT();
        Mat frame = inputFrame.rgba();

        // Cutting square in the middle (optimum for DFT)
        int cropWidthIn = frame.width();
        int cropHeightIn = frame.height();
        int cropCenterXIn = (frame.width() - cropWidthIn) / 2;
        int cropCenterYIn = (frame.height() - cropHeightIn) / 2;

        // Ensure ROI is within bounds
        if (cropCenterXIn < 0) cropCenterXIn = 0;
        if (cropCenterYIn < 0) cropCenterYIn = 0;
        if (cropWidthIn + cropCenterXIn > frame.width()) cropWidthIn = frame.width() - cropCenterXIn;
        if (cropHeightIn + cropCenterYIn > frame.height()) cropHeightIn = frame.height() - cropCenterYIn;

        Rect rectCropIn = new Rect(cropCenterXIn, cropCenterYIn, cropWidthIn, cropHeightIn);
        Mat croppedImageInput = new Mat(frame, rectCropIn); // RELEASE?

        // Copy to different Mat
        Mat resizeImage = new Mat();
        croppedImageInput.copyTo(resizeImage);

        // Change to grayscale (DFT is only on 1 channel)
        Imgproc.cvtColor(resizeImage, resizeImage, Imgproc.COLOR_RGB2GRAY);

        // Changing image quality before DFT. Smaller = faster but less precise
        org.opencv.core.Size qualitySize = new org.opencv.core.Size(
                resizeImage.width() * (1 - qualityFactor),
                resizeImage.height() * (1 - qualityFactor)
        );
        Imgproc.resize(resizeImage, resizeImage, qualitySize, 0, 0, Imgproc.INTER_AREA);

        // DFT: Amp or Phase or non
        boolean rotate = (rotateStatus == 0);
        boolean matrix = (matrixStatus == 0);

        if (spectrumStatus == 0) {
            dft.getDFT(resizeImage, rotate, matrix).copyTo(resizeImage);
            dft.release();
        } else if (spectrumStatus == 1) {
            dft.getPhase2(resizeImage, rotate, matrix).copyTo(resizeImage);
            dft.release();
        }

        // Resize back to square image
        Imgproc.resize(resizeImage, resizeImage, croppedImageInput.size(), 0, 0, Imgproc.INTER_AREA);

        // Preview (not camera) zoom in the middle
        int cropWidth = (int) (resizeImage.width() * (1 - zoomPreviewFactor));
        int cropHeight = (int) (resizeImage.height() * (1 - zoomPreviewFactor));
        int cropCenterX = (resizeImage.width() - cropWidth) / 2;
        int cropCenterY = (resizeImage.height() - cropHeight) / 2;

        // Ensure ROI is within bounds
        if (cropCenterX < 0) cropCenterX = 0;
        if (cropCenterY < 0) cropCenterY = 0;
        if (cropWidth + cropCenterX > resizeImage.width()) cropWidth = resizeImage.width() - cropCenterX;
        if (cropHeight + cropCenterY > resizeImage.height()) cropHeight = resizeImage.height() - cropCenterY;

        Rect rectCrop = new Rect(cropCenterX, cropCenterY, cropWidth, cropHeight);
        Mat croppedImage = new Mat(resizeImage, rectCrop); // RELEASE?

        // Resize to maximum square
        Imgproc.resize(croppedImage, croppedImage, croppedImageInput.size(), 0, 0, Imgproc.INTER_AREA);

        Mat output;

            // Putting our square in the middle of rectangle (input is rectangle)
            // as return image has to be the same size
            output = new Mat(frame.size(), resizeImage.type(), new Scalar(0));

            int submatX = (frame.cols() - croppedImage.cols()) / 2;
            if (submatX < 0) submatX = 0;
            if (croppedImage.rows() > frame.rows()) croppedImage = croppedImage.rowRange(0, frame.rows());
            if (croppedImage.cols() > frame.cols()) croppedImage = croppedImage.colRange(0, frame.cols());

            Rect submatRect = new Rect(submatX, 0, croppedImage.cols(), croppedImage.rows());
            croppedImage.copyTo(output.submat(submatRect));


        // Release Mats to avoid memory leaks
        croppedImageInput.release();
        croppedImage.release();

        return output;
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

        resizeimage = new Mat();
        frame = new Mat();
    }


    @Override
    public void onCameraViewStopped() {

        resizeimage.release();
        frame.release();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    protected void onResume() {
        super.onResume();


        if (javaCameraView != null) {
            javaCameraView.enableView();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }


    }


    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }

    }


    private double qualityFactor = 0.0;
    public void qualityButton(View view){
        qualityFactor = (qualityFactor + 0.2)%1.0;
        Toast.makeText(this, "Quality factor: " + dec.format(1.0-qualityFactor), Toast.LENGTH_SHORT).show();

    }

    DecimalFormat dec = new DecimalFormat("#0.00");
    public void zoomUpCamera(View view) throws CameraAccessException {
        javaCameraView.zoomUpCamera2();
     Toast.makeText(this, "Camera zoom: " + dec.format(javaCameraView.getZoomFloat()) , Toast.LENGTH_SHORT).show();


    }


    private double zoomPreviewFactor = 0.0;
    public void zoomUpPreview(View view){
            zoomPreviewFactor = (zoomPreviewFactor + 0.2)%1.0;
        Toast.makeText(this, "Preview zoom: " + dec.format(zoomPreviewFactor), Toast.LENGTH_SHORT).show();

        }



    public void turnOnOff(View view) throws CameraAccessException {

//        javaCameraView.toggleFlashLight();
    javaCameraView.toggleFlashMode();
    }

    public void closeApp(View view) {
        this.finishAndRemoveTask();
    }
}
