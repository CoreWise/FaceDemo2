package com.huiyuenet.faceCheck;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceCheck {

    static {
        System.loadLibrary("FaceCheck");
    }

    private static Context mContext;

    public static int init(Context context){
        mContext = context.getApplicationContext();
        createDir(context);
        return initFaceEngine();
    }

    private static String rootDir = Environment.getExternalStorageDirectory().toString() + "/FaceResource/";
    public static String readDir = "";
    public static String write = rootDir + "write/";

    private static int initFaceEngine() {

        int initResult1 = -1;
        int initResult2 = -1;
        int initResult3 = -1;

        initResult1 = FaceCheck.Init(readDir, write, mContext);
        initResult2 = FaceCheck.InitFaceEngine(readDir, write);
        initResult3 = FaceCheck.allocateFeatureMemory(1000);

        Log.i("face_test", "FaceCheck  initFaceEngine initResult1:" + initResult1 + ", initResult2:" + initResult2
                + ", initResult3:" + initResult3 + ",sdkversioninfo:" + GetSDKVersionInfo());

        if(initResult1>=0 && initResult2>=0 && initResult3>=0){
            return THFI_Param.SUCCESS;
        }else{
            int code = initResult1;
            if(initResult1 < 0){
                code = initResult1;
            }else if(initResult2 < 0){
                code = initResult2;
            }else if(initResult3 < 0){
                code = initResult3;
            }
            return code;
        }
    }

    public static void createDir(Context context) {
        readDir = context.getApplicationInfo().nativeLibraryDir;

        File file = new File(write);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static native int ver1N(int matchNum, byte[] feature, int[] matchIndex, float[] matchScore);
    public static native int allocateFeatureMemory(int capacity);
    public static native int addFeature(int index, byte[] feature);
    public static native void clearFeature();

    public static native int ver1NN(int featureCount, byte[] featureList, byte[] featureSingle, int[] matchIndex, float[] matchScore);

    public static native int DetectLive2(byte[] bgrImageData, byte[] irImageData, THFI_FacePos[] facePosRgb, THFI_FacePos[] facePosIr, int width, int height, int threshold, float[] score);
    public static native int getPixelsBGR(byte[] dest, byte[] src, int w, int h);
    public static native String GetSDKVersionInfo();


    private static native int Test(String read, String write, String pic1, String pic2);
    public static int test(Context context, String pic1, String pic2){
        createDir(context);
        return Test(readDir, write, pic1, pic2);
    }
    /********************************人脸检测**************************************/

    /**
     * 初始化引擎
     * @param read 存放 libTHDetect_dpbin.so文件的目录
     * @param write 临时读写目录，供算法初始化使用
     * @return
     */
    private static native int Init(String read, String write, Context context);

    /**
     * 人脸检测
     * @param nChannelID 检测通道id
     * @param pImage 图片数组
     * @param bpp 图片位数 24,8
     * @param nWidth 图片宽度
     * @param nHeight 图片高度
     * @param pfps 检测后的人脸坐标点数组
     * @param nMaxFaceNums 最大检测数
     * @param nSampleSize 缩放比例
     * @return
     */
    public static native int CheckFace(short nChannelID, byte[] pImage, int bpp, int nWidth, int nHeight, THFI_FacePos[] pfps, int nMaxFaceNums, int nSampleSize);

    /**
     * 释放引擎
     * @return
     */
    public static native void Release();

    /********************************人脸比对**************************************/
    /**
     * 初始化人脸引擎
     * @param read 核心库文件读取目录
     * @param write 初始化引擎时使用的临时写目录
     * @return
     */
    private static native int InitFaceEngine (String read, String write);

    /**
     * 获取特征点长度
     * @return
     */
    public static native int GetFeaturesSize();

    /**
     * 特征点抽取
     * @param pImage 待抽取图片数据
     * @param nWidth 图片宽
     * @param nHeight 图片高
     * @param nChannel 色彩通道，必须填3
     * @param pfps 人脸坐标点，由人脸检测模块得到
     * @param pFeature 返回的人脸特征
     * @return
     */
    public static native int GetFeatures (byte[] pImage, int nWidth, int nHeight, int nChannel, THFI_FacePos pfps, byte[] pFeature);

    /**
     * 人脸比对
     * @param pFeature1 待比对人脸1
     * @param pFeature2 待比对人脸2
     * @return
     */
    public static native float FaceCompare (byte[] pFeature1,  byte[] pFeature2);
    public static native float FaceCompareShort (byte[] pFeature1,  byte[] pFeature2);

    /**
     * 释放引擎
     */
    public static native void FaceEngineRelease();

    public static native int THFQSetParam(THFQ_Param param);
    public static native int THFQCheck(short _channelID, byte[] _imageArray, int _bpp, int _width, int _height, THFI_FacePos[] _facePosObjArray, THFQ_Result[] _resultObjArray);
    public static native int THFQCheckBrightness(short _channelID, byte[] _imageArray, int _bpp, int _width, int _height, THFI_FacePos[] _facePosObjArray, int[] _occlusion);
    public static native int THFQCheckOcclusion(short _channelID, byte[] _imageArray, int _bpp, int _width, int _height, THFI_FacePos[] _facePosObjArray, int[] _brightness);
    public static native int THFQCheckHat(short _channelID, byte[] _imageArray, int _bpp, int _width, int _height, THFI_FacePos[] _facePosObjArray, int[] _hat);
    public static native int THFQCheckBlurGlasses(short _channelID, byte[] _imageArray, int _bpp, int _width, int _height, THFI_FacePos[] _facePosObjArray, int[] _blur, int[] _glasses);

    public enum CheckOperation{
        CHECK_BRIGHTNESS,
        CHECK_OCCLUSION,
        CHECK_HAT,
        CHECK_BLUR,
        CHECK_GLASSES,
    }
}
