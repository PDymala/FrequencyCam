package com.diplabs.frequencycam3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.resize;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "FreqencyCam3";
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    private CustomCameraView javaCameraView;// JavaCameraView javaCameraView;

    private BaseLoaderCallback baseLoaderCallback;
    private DFT dft;
    private Mat resizeimage;
    private Mat frame;
    private int spectrumStatus = 0;
    private int matrixStatus = 0;
    private int rotateStatus = 0;
    private int size = 512;

    ImageButton imageButtonSpectrum;
    ImageButton imageButtonMatrix;
    ImageButton imageButtonSize;
    ImageButton imageButtonRotate;
//    ImageButton imageButtonFullScreen;
//    ImageButton imageButtonZoomUp;
//    ImageButton imageButtonZoomDown;
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
        javaCameraView = (CustomCameraView) findViewById(R.id.cameraView1);//javaCameraView = (JavaCameraView) findViewById(R.id.cameraView1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        initializeButtons();



        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch (status) {

                    case BaseLoaderCallback.SUCCESS:
                        javaCameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }

        };


        try {
           for (Size s : javaCameraView.getCameraSizes()){
               Log.i(TAG, "onCreate: "+s.toString());;
           }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    public void showPopupSize(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu_size);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.size128:
                size = 128;
                javaCameraView.setMaxFrameSize(size, size);
//                javaCameraView.setMinimumHeight(size);
                javaCameraView.disableView();
                javaCameraView.enableView();
                return true;

            case R.id.size256:
                size = 256;
                javaCameraView.setMaxFrameSize(size, size);
//                javaCameraView.setMinimumHeight(size);
                javaCameraView.disableView();
                javaCameraView.enableView();
                return true;

            case R.id.size512:
                size = 512;
                javaCameraView.setMaxFrameSize(size, size);
//                javaCameraView.setMinimumHeight(size);
                javaCameraView.disableView();
                javaCameraView.enableView();
                return true;

            case R.id.size1028:
                size = 1028;
                javaCameraView.setMaxFrameSize(size, size);
//                javaCameraView.setMinimumHeight(size);
                javaCameraView.disableView();
                javaCameraView.enableView();
                return true;
            default:
                return false;
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
     //   javaCameraView.enableFpsMeter();
        javaCameraView.setMaxFrameSize(size, size);
//        javaCameraView.setMinimumHeight(size);

    }

    private void initializeButtons() {

        imageButtonSpectrum = findViewById(R.id.imageButtonSpectrum);
        imageButtonMatrix = findViewById(R.id.imageButtonMatrix);
        imageButtonSize = findViewById(R.id.imageButtonSize);
        imageButtonRotate = findViewById(R.id.imageButtonRotate);
//        imageButtonFullScreen = findViewById(R.id.imageButtonFullScreen);
//        imageButtonZoomUp = findViewById(R.id.imageButtonZoomUp);
//        imageButtonZoomDown = findViewById(R.id.imageButtonZoomDown);
        imageButtonFlash = findViewById(R.id.imageButtonFlash);
//        imageButtonSave = findViewById(R.id.imageButtonSave);
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
//        Log.i(TAG, "onCameraFrame: " + inputFrame.rgba().size());
        dft = new DFT();
        frame = inputFrame.rgba();

        frame.copyTo(resizeimage);

        //resize do najblizej zadanego wymiaru? jak jest 600 to 512
//        resize(frame,             // input image
//                resizeimage,            // result image
//                new Size(size, size),     // new dimensions
//                0,
//                0,
//                INTER_CUBIC       // interpolation method
//        );
//        Log.i(TAG, "onCameraFrame: " + resizeimage.size());
        Imgproc.cvtColor(frame, resizeimage, Imgproc.COLOR_RGB2GRAY);


        //potem crop?
        //Rect roi = new Rect(x, y, width, height);
        //Mat cropped = new Mat(uncropped, roi);


        boolean rotate = true;
        boolean matrix = true;

        if (matrixStatus == 0) {
            matrix = true;
        } else {
            matrix = false;
        }

        if (rotateStatus == 0) {
            rotate = true;

        } else {
            rotate = false;
        }


        if (spectrumStatus == 0) {
            dft.getDFT(resizeimage, rotate, matrix).copyTo(resizeimage);
            dft.release();
        } else if (spectrumStatus == 1) {
            dft.getPhase2(resizeimage, rotate, matrix).copyTo(resizeimage);
            dft.release();
        } else {


        }


        //i tutaj albo wypluc ten crop albo rozciagniety?

        //output image musi miec ten sam wymiar co input. inaczej sie psuje. dlatego DFT na małym nalezy
        resize(resizeimage,             // input image
                resizeimage,            // result image
                frame.size(),     // new dimensions
                0,
                0,
                INTER_CUBIC       // interpolation method
        );
        Log.i(TAG, "onCameraFrame: " + resizeimage.size());


//   return dft.getDFT(resizeimage);

        return resizeimage;
    }

//    public void zoomUp(View view) {
//        javaCameraView.zoomUp();
//
//
//    }
//
//    public void zoomDown(View view) {
//        javaCameraView.zoomDown();
//
//
//    }

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

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is Configured or Connected successfully.");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            Log.d(TAG, "OpenCV not Working or Loaded.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
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


    public void turnOnOff(View view) throws CameraAccessException {

        javaCameraView.toggleFlashMode();
    }

    public void closeApp(View view) {
        this.finishAndRemoveTask();
    }
}
