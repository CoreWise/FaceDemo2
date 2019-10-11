package com.cw.facesdk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Toast;


import com.huiyuenet.faceCheck.THFI_Param;

import java.util.List;

@SuppressWarnings("deprecation")
public class CameraWrapper {

    private Camera mFaceCamera;
    private SurfaceTexture mPreviewSurface;
    private SurfaceHolder mSurfaceHolder;
    private Camera.PreviewCallback mPreviewCallback;
    public boolean isPreviewing = false;
    public boolean isUserSurfaceHolder = false;
    public Activity mActivity;
    private int imgWidth;
    private int imgHeight;
    private int actW;
    private int actH;
    private float overturnX;
    private float overturnY;
    private boolean angle;
    private int mCameraID;
//    private List<Camera.Size> previewList;
    private SharedPreferences sp;

    public CameraWrapper(Activity context, Camera.PreviewCallback previewCallback, SurfaceHolder surfaceHolder, boolean useSurfaceHolder) {
        this.mActivity = context;
        this.mPreviewSurface = new SurfaceTexture(0);
        this.mPreviewCallback = previewCallback;
        this.mSurfaceHolder = surfaceHolder;
        this.isUserSurfaceHolder = useSurfaceHolder;
        this.sp = context.getSharedPreferences(THFI_Param.SP_FILE_NAME, Activity.MODE_PRIVATE);
    }

    public void Config(int imgWidth, int imgHeight, int actW, int actH, float overturnX,float overturnY, boolean angle) {
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.actW = actW;
        this.actH = actH;
        this.overturnX = overturnX;
        this.overturnY = overturnY;
        this.angle = angle;
    }

    private int rotateAngle = 0;
    public int getRotateAngle(){
        return rotateAngle;
    }

    public Matrix getMatrix()
    {
        boolean previewSizeIsOk = true; // false;
        rotateAngle = 0;

//        if(previewList == null)
//            return null;
//
//        for(int i=0; i<previewList.size(); i++)
//        {
//            if(imgWidth==previewList.get(i).width && imgHeight==previewList.get(i).height)
//            {
//                previewSizeIsOk=true;
//            }
//        }

        if(previewSizeIsOk)
        {
//            double imgSceneScale=(double) imgWidth/imgHeight;
//            double actSceneScale=(double) actW/actH;
//
//            if((imgSceneScale<1.0&&actSceneScale>1.0)||(actSceneScale<1.0&&imgSceneScale>1.0))
//            {
//                rotateAngle = 90;
//            }else
//            {
//                rotateAngle = 0;
//            }


            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(0, info);
            if(mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT){
                rotateAngle = info.orientation;
            }else{
                overturnX = -1;
                overturnY = -1;
            }

            Matrix mMatrix = new Matrix();
//            mMatrix.postScale(-1, -1);

//            String key = THFI_Param.SP_KEY_MATRIX_ROTATE2;
//            if(THFI_Param.CameraID == Camera.CameraInfo.CAMERA_FACING_FRONT){
//                key = THFI_Param.SP_KEY_MATRIX_ROTATE1;
//            }

//            int spRotate = this.sp.getInt(key, -1);
//            if(spRotate != -1){
//                rotateAngle = spRotate;
//            }

            rotateAngle = THFI_Param.RotateAngle;

            mMatrix.setRotate(rotateAngle);
            overturnX=THFI_Param.ReversalHorizontal==true?1:-1;
            mMatrix.postScale(overturnX, overturnY);
            Log.e("wsm","setRotate = " + rotateAngle + " postScale x=" + overturnX+ "y = " + overturnY);

            return mMatrix;
        }
        return null;
    }


    public void openCamera(Activity activity, int cameraID) {

        mCameraID = cameraID;
        this.mActivity = activity;

        if (mFaceCamera == null) {
            try{
                mFaceCamera = Camera.open(cameraID/*THFI_Param.CameraID*/);
            }catch (Exception e){
                Toast.makeText(activity.getApplicationContext(), "Camera ID:" + cameraID + " 打开失败，请重试", Toast.LENGTH_LONG).show();
                mFaceCamera = null;
                return ;
            }
        }

//        previewList = getPreviewList();

        updateCameraParameters(imgWidth,imgHeight);

        //mMatrix.setScale(-1,1);
//        setCameraDisplayOrientation(activity);
    }



    public void closeCamera() {
        if (mFaceCamera != null) {
            stopPreview();
            mFaceCamera.setPreviewCallback(null);
            mFaceCamera.release();
            mFaceCamera = null;
        }
    }

    public boolean startPreview(){

        if(mFaceCamera == null){
//            openCamera(mActivity, 0);
            return false;
        }
//        openFlashLED();
        mFaceCamera.setPreviewCallback(mPreviewCallback);
        //mFaceCamera.setDisplayOrientation(90);
        mFaceCamera.startPreview();
        isPreviewing = true;

        return true;
    }

    public boolean stopPreview(){

//        closeFlashLED();
        if (mFaceCamera != null) {
            mFaceCamera.stopPreview();
            mFaceCamera.setPreviewCallback(null);
        }
        isPreviewing = false;

        return true;
    }

    public   List<Camera.Size> getPreviewList()
    {
        List<Camera.Size> supportedPreviewSizes=null;
        if(mFaceCamera!=null)
        {
            Parameters parameters = mFaceCamera.getParameters();
            supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        }
        return supportedPreviewSizes;
    }

    public void openFlashLED(){
        ShellUtils.execCmd("echo 1 > /sys/class/leds/white/brightness", false);
        ShellUtils.execCmd("echo 1 > /sys/class/leds/white2/brightness", false);
    }

    public void closeFlashLED(){
        ShellUtils.execCmd("echo 0 > /sys/class/leds/white/brightness", false);
        ShellUtils.execCmd("echo 0 > /sys/class/leds/white2/brightness", false);
    }

    private void updateCameraParameters(int imgWidth, int imgHeight) {

        if(mFaceCamera == null){
            return;
        }

        try {
            Parameters params = mFaceCamera.getParameters();
            Log.d("wsm","setPreviewSize  w = "+imgWidth+ "  h = "+imgHeight);
            params.setPreviewSize(imgWidth,imgHeight);
            params.setPreviewFormat(ImageFormat.NV21);
            List<String> supportedFlashModes = params.getSupportedFlashModes();
            if (supportedFlashModes != null && supportedFlashModes.contains(Parameters.FLASH_MODE_OFF)) {
                params.setFlashMode(Parameters.FLASH_MODE_OFF); // 设置闪光模式
            }
            params.setFocusMode(Parameters.FOCUS_MODE_AUTO); // 设置聚焦模式
            params.setPreviewFormat(ImageFormat.NV21); // 设置预览图片格式
            params.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
            mFaceCamera.setParameters(params);

            if(isUserSurfaceHolder){
                mFaceCamera.setPreviewDisplay(mSurfaceHolder);
            }

            mFaceCamera.setPreviewTexture(mPreviewSurface);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getDisplayRotation(Activity activity) {

        if(activity == null){
            return 0;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: return 0;
            case Surface.ROTATION_90: return 90;
            case Surface.ROTATION_180: return 180;
            case Surface.ROTATION_270: return 270;
        }
        return 0;
    }

    private void setCameraDisplayOrientation(Activity activity) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mFaceCamera.setDisplayOrientation(result);   // 修改这里的值并不会影响Camera预览方向
    }


}
