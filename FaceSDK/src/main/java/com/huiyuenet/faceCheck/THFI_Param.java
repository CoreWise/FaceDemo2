package com.huiyuenet.faceCheck;

import java.util.ArrayList;
import java.util.List;

public class THFI_Param {
	public static int nMinFaceSize = 50;//min face width size can be detected,default is 50 pixels
	public static int nRollAngle = 30;//max face roll angle,default is 30(degree)
	public static int bOnlyDetect = -1;//ingored
	public static int dwReserved = 0;//reserved value,must be NULL
    public static int faceFeatureSize = 2560;

    public static final String SUFFIX = ".v10";

    public static final int IMG_HEIGHT = 480;
    public static final int IMG_WIDTH = 640;

    public static  int ACTUAL_HEIGHT = IMG_HEIGHT;
    public static  int ACTUAL_WIDTH = IMG_WIDTH;

    /**
     * 人脸检测图像降采样比，值越小 表示人脸可检测距离越远，同时人脸检测耗时也越多，目前建议值：360、240、0，如果使用
     * 场景不需要远距离人脸（1m之外），那么建议将该值配置成 240 或 360
     *
     * 说明：这个参数会影响检测距离和检测速度。应该根据产品的应用场景合理设置这个参数。在满足工作距离的前提下，尽可能多的缩小图像检测，而带来处理速度的提升。
     */
    public static final int SAMPLE_SIZE = 360; // 0; //360;  // 240

    public static float LIVE_THRESHOLD = 0.94f;  // 活体分数阈值
    public static final float FACE_VISIBILITY = 0.35f;   // 人脸在整个图像中的可见比，如果大于该范围，表示距离过近，视为图像无效
    public static final boolean ENABLE_FACE_QUALITY_CHECK = false;

    public static final int MAX_FACE_NUMS = 1;  // 最大支持的检测人脸数量

    public static List<String> FaceName = new ArrayList<>();
    public static List<String> FacePhoneNumber = new ArrayList<>();
    public static int EnrolledNum = 0;

    public static long DetectTime = 0;
    public static final int OPERATE_TIME_OUT = 5000;    // 建模、比对超时时间（单位：ms）

    public static int CameraID = 1;
    public static int RotateAngle = 0;
    public static boolean ReversalHorizontal = false;

    public static final String SP_FILE_NAME = "sp_matrix";
    public static final String SP_KEY_MATRIX_ROTATE1 = "sp_matrix_rotate1";
    public static final String SP_KEY_MATRIX_ROTATE2 = "sp_matrix_rotate2";


    // ==================人脸算法函数执行返回错误码===============================
    public static final int  ERROR_1                =    -1;       //
    public static final int  ERROR_2                =    -2;       //
    public static final int  ERROR_3                =    -3;       //
    public static final int  ERROR_99               =    -99;      // invalid license
    // ==================人脸算法函数执行返回错误码===============================


    // ==================授权检测相关错误码===============================
    public static final int  VALID                  =    0;           // 成功。仅当此时获取encrypt
    public static final int  ERROR_PARAM            =    -1000;       // 参数校验错误.一般是由于有参数值为空.
    public static final int  ERROR_INVALID_DEVICE   =    -1001;       // 无法匹配到设备类型
    public static final int  ERROR_INVALID_DATE     =    -1002;       // 请求时间超出授权期限
    public static final int  ERROR_INVALID_COUNT    =    -1003;       // 超出该设备的授权数量
    public static final int  ERROR_DEFAULT          =    -1004;       // 系统错误
    // ==================授权检测相关错误码===============================


    // ==================算法校验相关错误码===============================
    public static final int  ALG_VALID              =     0;        // 成功。
    public static final int  ERR_RANDOM             =    -2000;     // 请求随机数错误
    public static final int  ERR_INVALID_AES		=	 -2099;     // AES验证错误
    // ==================算法校验相关错误码===============================



    // ====================人脸算法相关错误码=============================
    public static final int SUCCESS                 =    0;               // 成功
    public static final int ERR_OVER_CUR_CAPACITY   =    -3000;           // 特征容量超过当前已分配空间限制，需要重新分配空间
    public static final int ERR_OVER_MAX_CAPACITY   =    -3001;           // 特征容量超过限制，目前最大支持 200000
    public static final int ERR_INVALID_PARAM       =    -3002;           // 应用层传入参数错误
    public static final int ERR_UNINIT              =    -3003;           // 未执行初始化相关方法
    // ====================人脸算法相关错误码=============================

}



