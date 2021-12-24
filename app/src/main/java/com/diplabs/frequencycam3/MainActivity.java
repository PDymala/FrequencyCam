package com.diplabs.frequencycam3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
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

    private BaseLoaderCallback baseLoaderCallback;
    private DFT dft;
    private Mat resizeimage;
    private Mat frame;
    private int spectrumStatus = 0;
    private int matrixStatus = 0;
    private int rotateStatus = 0;
    private boolean stretch = false;
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
//        Log.i(TAG, "onCameraFrame: " + inputFrame.rgba().size());
        dft = new DFT();
        frame = inputFrame.rgba();




        //Cutting square in the middle (optimum for DFT)
        int cropWidthIn = (int)((double)frame.height());
        int cropHeightIn = (int)((double)frame.height());
        int cropCenterXIn = (int)((frame.width()-cropWidthIn)/2.0);
        int cropCenterYIn= (int)((frame.height()-cropHeightIn)/2.0);
        Rect rectCropIn = new Rect(cropCenterXIn, cropCenterYIn, cropWidthIn, cropHeightIn);
        Mat croppedImageInput = new Mat(frame, rectCropIn); // RELEASE?


        //copy to different Mat
        croppedImageInput.copyTo(resizeimage);

        //change to grayscale (DFT is only on 1 channeL)
        Imgproc.cvtColor(resizeimage, resizeimage, Imgproc.COLOR_RGB2GRAY);


        //changing image quality before DFT. Smaller = faster but less precise
        org.opencv.core.Size qualitySize = new org.opencv.core.Size((int)(resizeimage.height()*(1-qualityFactor)), (int)(resizeimage.width()*(1-qualityFactor)));
        resize(resizeimage,             // input image
                resizeimage,            // result image
                qualitySize,     // new dimensions
                0,
                0,
                INTER_AREA       // interpolation method
        );


        //DFT: Amp or Phase or non
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
//
            dft.getDFT(resizeimage, rotate, matrix).copyTo(resizeimage);
            dft.release();


        } else if (spectrumStatus == 1) {
            dft.getPhase2(resizeimage, rotate, matrix).copyTo(resizeimage);
            dft.release();
        } else {


        }


        //resize back to square image
        resize(resizeimage,             // input image
                resizeimage,            // result image
                croppedImageInput.size(),     // new dimensions
                0,
                0,
                INTER_AREA       // interpolation method
        );




        //preview (not camera) zoom in the middle
        int cropWidth = (int) (resizeimage.width()*(1-zoomPreviewFactor));
        int cropHeight = (int) (resizeimage.height()*(1-zoomPreviewFactor));
        int cropCenterX = (int)((resizeimage.width()-cropWidth)/2.0);
        int cropCenterY = (int)((resizeimage.height()-cropHeight)/2.0);
        Rect rectCrop = new Rect(cropCenterX, cropCenterY, cropWidth, cropHeight);
        Mat croppedImage = new Mat(resizeimage, rectCrop); // RELEASE?
        // end of zoom



        // resize to maximum square
        resize(croppedImage,             // input image
                croppedImage,            // result image
                croppedImageInput.size(),     // new dimensions
                0,
                0,
                INTER_AREA       // interpolation method
        );





        if (stretch){
            //stretching the square to frame size
            resize(croppedImage,             // input image
                    croppedImage,            // result image
                    frame.size(),     // new dimensions
                    0,
                    0,
                    INTER_CUBIC       // interpolation method
            );
            return croppedImage;


        } else{

                   //putting our square in the middle of rectangle (input is rectangle) as return image has to be the same size
        Mat output =  new Mat(frame.size(),resizeimage.type(), new Scalar(0));
        croppedImage.copyTo(output.submat(new Rect((frame.cols()-croppedImageInput.cols())/2,0,croppedImage.rows(),croppedImage.cols())));


        return output;

        }



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


    public void stretch(View view){

        if (stretch) stretch = false;
        else stretch = true;
    }

    public void turnOnOff(View view) throws CameraAccessException {

//        javaCameraView.toggleFlashLight();
    javaCameraView.toggleFlashMode();
    }

    public void closeApp(View view) {
        this.finishAndRemoveTask();
    }
}
