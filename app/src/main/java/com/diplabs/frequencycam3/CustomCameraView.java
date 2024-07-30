package com.diplabs.frequencycam3;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import org.opencv.android.JavaCamera2View;

public class CustomCameraView extends JavaCamera2View {

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    // !! https://stackoverflow.com/questions/52568987/camera-zoom-setting-using-camera2-api
    float zoomFloat;
    float currentZoom = 1.0f;
    float minimumZoom = 1.0f;

    public void zoomUpCamera2() throws CameraAccessException{

        CameraManager mCameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);

        Zoom zoom = new Zoom( mCameraManager.getCameraCharacteristics(mCameraID));
        zoomFloat = (zoom.maxZoom - Zoom.DEFAULT_ZOOM_FACTOR )/ 6;


        if (currentZoom + zoomFloat < zoom.maxZoom){
            currentZoom += zoomFloat;
        } else{
            currentZoom = minimumZoom;
        }


        zoom.setZoom(mPreviewRequestBuilder,currentZoom);  //0.0f - 1.0 no zoom





        mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);

    }



    public float getZoomFloat() {
        return currentZoom;
    }

    //
//    private int zoomCounter = 0;
//    public void zoomUpCamera(){
//
//        Camera.Parameters params = mCamera.getParameters();
//        zoomCounter =params.getMaxZoom()/5;
//
//        if (params.isZoomSupported()){
//
//                params.setZoom((params.getZoom() +zoomCounter)%params.getMaxZoom());
//
//                mCamera.setParameters(params);
//
//
//        } else{
//            //not supported
//        }
//
//    }
//
//
//public double getCameraZoom(){
//    Camera.Parameters params = mCamera.getParameters();
//
//    return ((double)params.getZoom() / (double)params.getMaxZoom());
//}

//public Size[] getCameraSizes() throws CameraAccessException {
//    CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
//    CameraCharacteristics characteristics = manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
//    StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//    Size[] sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
//
//        return sizes;
//}



    boolean enable = true;
    public void toggleFlashMode() throws CameraAccessException { //mPrevie changed from protected to public

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





    public final class Zoom
    {
        private static final float DEFAULT_ZOOM_FACTOR = 1.0f;

        @NonNull
        private final Rect mCropRegion = new Rect();

        public final float maxZoom;

        @Nullable
        private final Rect mSensorSize;

        public final boolean hasSupport;

        public Zoom(@NonNull final CameraCharacteristics characteristics)
        {
            this.mSensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            if (this.mSensorSize == null)
            {
                this.maxZoom = Zoom.DEFAULT_ZOOM_FACTOR;
                this.hasSupport = false;
                return;
            }

            final Float value = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);

            this.maxZoom = ((value == null) || (value < Zoom.DEFAULT_ZOOM_FACTOR))
                    ? Zoom.DEFAULT_ZOOM_FACTOR
                    : value;

            this.hasSupport = (Float.compare(this.maxZoom, Zoom.DEFAULT_ZOOM_FACTOR) > 0);
        }

        public void setZoom(@NonNull final CaptureRequest.Builder builder, final float zoom)
        {
            if (this.hasSupport == false)
            {
                return;
            }

            final float newZoom = MathUtils.clamp(zoom, Zoom.DEFAULT_ZOOM_FACTOR, this.maxZoom);

            final int centerX = this.mSensorSize.width() / 2;
            final int centerY = this.mSensorSize.height() / 2;
            final int deltaX  = (int)((0.5f * this.mSensorSize.width()) / newZoom);
            final int deltaY  = (int)((0.5f * this.mSensorSize.height()) / newZoom);

            this.mCropRegion.set(centerX - deltaX,
                    centerY - deltaY,
                    centerX + deltaX,
                    centerY + deltaY);

            builder.set(CaptureRequest.SCALER_CROP_REGION, this.mCropRegion);
        }
    }




//    boolean flashlightOn = false;
//
//    public void toggleFlashLight(){
//
//        Camera.Parameters params = mCamera.getParameters();
//        if (flashlightOn){
//
//            params.setFlashMode(params.FLASH_MODE_OFF);
//            flashlightOn = false;
//
//        } else{
//            params.setFlashMode(params.FLASH_MODE_TORCH);
//            flashlightOn = true;
//
//
//        }
//        mCamera.setParameters(params);
//
//
//    }

}
