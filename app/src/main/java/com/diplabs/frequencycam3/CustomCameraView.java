package com.diplabs.frequencycam3;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.AttributeSet;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;

import java.util.List;

public class CustomCameraView extends JavaCamera2View  {
    public CustomCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



//
//    public void zoomUp(){
//
////        Camera.Parameters params = mCamera.getParameters();
////        if (params.getZoom() + 10 <= params.getMaxZoom()){
////            params.setZoom(params.getZoom() +10);
////            mCamera.setParameters(params);
////
////        }
//
//    }
//
//    public void zoomDown(){
////        Camera.Parameters params = mCamera.getParameters();
////        if(params.getZoom() - 10 >= 0) {
////            params.setZoom(params.getZoom() - 10);
////            mCamera.setParameters(params);
////        }
//    }
public Size[] getCameraSizes() throws CameraAccessException {
    CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
    CameraCharacteristics characteristics = manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
    StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    Size[] sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);

        return sizes;
}

boolean enable = true;
    public void toggleFlashMode() throws CameraAccessException {

        try {

                if (enable) {
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    enable = false;
                } else {
                    mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    enable = true;
                }
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }



}