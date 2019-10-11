package com.huiyuenet.faceCheck;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by liuzhiwei on 2018-06-27.
 */
public class FaceFunction {
    /**是否打印日志*/
    public static boolean PRINTLOG = true;
    private static String targer = "face";
    /**人脸特征点建模存放路径*/
    public static final String PATH = Environment.getExternalStorageDirectory().toString() + "/FeaturePath/";
    public static String savePath = PATH;
    /**人脸比对通过分数*/
    public static float fraction = 0.65f;

    public synchronized static float faceLiveCheck(byte[] pixelsBGR, THFI_FacePos[] faceRect, int width, int height){
        float[] liveScore = new float[1];

        if(pixelsBGR == null){
            return liveScore[0];
        }

        int ret = FaceCheck.DetectLive2(pixelsBGR, null, faceRect, null, width, height, 50, liveScore);

        return liveScore[0];
    }

    public static int faceQualityCheck(byte[] pixels, int width, int height, THFI_FacePos[] facePos, THFQ_Result[] faceResult){

        int ret = -1;

        if(pixels == null){
            return ret;
        }

        ret = FaceCheck.THFQCheck((short)0, pixels, 24, width, height, facePos, faceResult);

        return ret;
    }

    public static int faceQualityCheck(FaceCheck.CheckOperation operation, byte[] pixels, int width, int height, THFI_FacePos[] facePos, int[] faceResult){

        int ret = -1;

        if(pixels == null){
            return ret;
        }

        int[] tmp = new int[10];
        switch (operation){
            case CHECK_HAT:
                ret = FaceCheck.THFQCheckHat((short)0, pixels, 24, width, height, facePos, faceResult);
                break;
            case CHECK_OCCLUSION:
                ret = FaceCheck.THFQCheckOcclusion((short)0, pixels, 24, width, height, facePos, faceResult);
                break;
            case CHECK_BRIGHTNESS:
                ret = FaceCheck.THFQCheckBrightness((short)0, pixels, 24, width, height, facePos, faceResult);
                break;
            case CHECK_BLUR:
                ret = FaceCheck.THFQCheckBlurGlasses((short)0, pixels, 24, width, height, facePos, faceResult, tmp);
                break;
            case CHECK_GLASSES:
                ret = FaceCheck.THFQCheckBlurGlasses((short)0, pixels, 24, width, height, facePos, tmp, faceResult);
                break;
            default:
                break;
        }

        return ret;
    }

    /**
     * 抽取特征点
     * @param pixels 待抽取特征点的图片（请保证图片是正的）
     * @param angle 图片允许偏移的最大角度（建议建模时20度以内，检测时30度以内）
     * @param facePos 图片对应的人脸位置
     * @return
     */
    public static int faceDetect(byte[] pixels, int width, int height, int angle, THFI_FacePos[] facePos){

        int ret = -1;

        if(pixels == null){
            return ret;
        }

//        synchronized (PATH)
        {
            ret = FaceCheck.CheckFace(
                    (short)0, pixels, 24,
                    width, height, facePos,
                    THFI_Param.MAX_FACE_NUMS, THFI_Param.SAMPLE_SIZE);
        }

        if (ret > 0) {
            //检测到人脸，判断角度和人脸坐标准确度
            THFI_FacePos pos = facePos[0];
            if (pos.fAngle.confidence < 0.7
                    || Math.abs(pos.fAngle.pitch) > angle
                    || Math.abs(pos.fAngle.roll) > angle) {
                Log.i("face_info", "人脸定位准确度不足或人脸角度大于" + angle + ", confidence:" + pos.fAngle.confidence);
            }

        } else {
            //未检测到人脸
           // printLog("未检测到人脸  ret="+ret);
        }

        return ret;
    }

    /**
     * 抽取特征点
     * @param pixelsBGR 待抽取特征点的图片（请保证图片是正的）
     * @param angle 图片允许偏移的最大角度（建议建模时20度以内，检测时30度以内）
     * @param facePos 图片对应的人脸位置
     * @return
     */
    public synchronized static byte[] faceFeatures(byte[] pixelsBGR, int width, int height, int angle, THFI_FacePos[] facePos) {

        if(pixelsBGR == null || facePos == null || facePos[0] == null){
            return null;
        }

        int ret = 1;
        if (ret > 0) {
            //检测到人脸，判断角度和人脸坐标准确度
            THFI_FacePos pos = facePos[0];

            if (pos.fAngle.confidence < 0.7
                    || Math.abs(pos.fAngle.pitch) > angle
                    || Math.abs(pos.fAngle.roll) > angle) {
                printLog("人脸定位准确度不足或人脸角度大于" + angle);
                return null;
            }

            int size = FaceCheck.GetFeaturesSize();
            byte[] feature = new byte[size];

            ret = FaceCheck.GetFeatures(pixelsBGR, width, height, 3, facePos[0], feature);

            if (ret == 1) {
                return feature;
            }
        } else {
            //未检测到人脸
            //printLog("未检测到人脸  ret=" + ret);
            return null;
        }

        printLog("特征点抽取失败  ret=" + ret);
        return null;
    }

    private static void mInsertUserInfo(byte[] feature, SqliteDataBase sd, String uid, String username, String userphonenumber, String usercompany, String useraddress, byte[] facebitmap) {
        FaceUserInfo faceUserInfo = new FaceUserInfo();
        faceUserInfo.m_Uid = uid;
        faceUserInfo.m_UserName = username;
        faceUserInfo.m_EnrollTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        faceUserInfo.facefeature=feature;
        faceUserInfo.mPhoneNumber=userphonenumber;
        faceUserInfo.mCompany=usercompany;
        faceUserInfo.mAddress = useraddress;
        faceUserInfo.mFaceBitmapArray=facebitmap;
        sd.insertUserData(faceUserInfo);
    }


    /**
     * 单个建模
     * @param feature 需要保存的特征
     * @param username
     * @param userphonenumber
     * @param usercompany
     * @param facebitmap
     * @return
     */
    public static boolean saveFaceFeatures(byte[] feature, String username, String userphonenumber, String usercompany, String useraddress, byte[] facebitmap, SqliteDataBase sd) {

        if (feature == null) {
            printLog("建模失败 特征点为空");
            return false;
        }
        mInsertUserInfo(feature, sd, "", username, userphonenumber, usercompany, useraddress, facebitmap);
        return true;
    }

    /**
     * 人脸比对 1比N
     * @param feature 比对者
     * @return 是否有匹配的人脸
     */
    public static int faceComparison1ToNMem (byte[] feature) {

        if (feature == null) {
            return -1;
        }

        int[] matchIndex = new int[1];
        float[] matchScore = new float[1];

        int ret = FaceCheck.ver1N(THFI_Param.EnrolledNum, feature, matchIndex, matchScore);

        return matchIndex[0];
    }

    /**
     * 获取bitmap中的像素数据
     * @param bmp
     * @return
     */
    public static byte[] getPixelsBGR(Bitmap bmp) {    // 耗时 2s

        if(bmp == null){
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        byte[] byteArrayDest = new byte[width * height * 3];  //开辟数组，存放像素内容
        byte[] byteArraySrc = new byte[width * height * 4];  //开辟数组，存放像素内容

        ByteBuffer src = ByteBuffer.wrap(byteArraySrc);
        bmp.copyPixelsToBuffer(src);  //从bitmap中取像素值到buffer中，像素值类型为ARGB
        FaceCheck.getPixelsBGR(byteArrayDest, byteArraySrc, width, height);

        return byteArrayDest;
    }

    /**
     * @param msg 详细信息
     */
    private static void printLog (String msg) {
        if (PRINTLOG)
            Log.d(targer, msg);
    }

    /**
     * 创建模型库存放目录
     * @return
     */
    private static boolean createDir () {
        if (savePath == null || savePath == "") {
            printLog("模型存放路径不能为空");
            return false;
        }

        //创建文件夹
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return true;
    }

    /**
     * 根据byte数组，生成文件
     */
    public static boolean getFile(byte[] bfile, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

}
