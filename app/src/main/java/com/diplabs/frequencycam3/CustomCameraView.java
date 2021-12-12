package com.diplabs.frequencycam3;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.AttributeSet;
import android.util.Size;

import org.opencv.android.JavaCameraView;

import java.text.DecimalFormat;

public class CustomCameraView extends JavaCameraView  {
    public CustomCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }




    private int zoomCounter = 0;
    public void zoomUpCamera(){

        Camera.Parameters params = mCamera.getParameters();
        zoomCounter =params.getMaxZoom()/5;

        if (params.isZoomSupported()){

                params.setZoom((params.getZoom() +zoomCounter)%params.getMaxZoom());

                mCamera.setParameters(params);


        } else{
            //not supported
        }

    }


public double getCameraZoom(){
    Camera.Parameters params = mCamera.getParameters();

    return ((double)params.getZoom() / (double)params.getMaxZoom());
}

//public Size[] getCameraSizes() throws CameraAccessException {
//    CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
//    CameraCharacteristics characteristics = manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
//    StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//    Size[] sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
//
//        return sizes;
//}

//boolean enable = true;
//    public void toggleFlashMode() throws CameraAccessException {
//
//        try {
//
//                if (enable) {
//                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
//                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
//                    enable = false;
//                } else {
//                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
//                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
//                    enable = true;
//                }
//                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
//
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }

    boolean flashlightOn = false;

    public void toggleFlashLight(){

        Camera.Parameters params = mCamera.getParameters();
        if (flashlightOn){

            params.setFlashMode(params.FLASH_MODE_OFF);
            flashlightOn = false;

        } else{
            params.setFlashMode(params.FLASH_MODE_TORCH);
            flashlightOn = true;


        }
        mCamera.setParameters(params);


    }

}
