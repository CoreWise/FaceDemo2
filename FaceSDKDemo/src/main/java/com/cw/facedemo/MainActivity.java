package com.cw.facedemo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.facesdk.CameraWrapper;
import com.huiyuenet.faceCheck.FaceCheck;
import com.huiyuenet.faceCheck.FaceFunction;
import com.huiyuenet.faceCheck.FaceUserInfo;
import com.huiyuenet.faceCheck.SqliteDataBase;
import com.huiyuenet.faceCheck.THFI_FacePos;
import com.huiyuenet.faceCheck.THFI_Param;
import com.huiyuenet.faceCheck.THFQ_Param;
import com.huiyuenet.faceCheck.THFQ_Result;
import com.usface.activation.asynctask.IDoInBackground;
import com.usface.activation.asynctask.IIsViewActive;
import com.usface.activation.asynctask.IPostExecute;
import com.usface.activation.asynctask.IPreExecute;
import com.usface.activation.asynctask.IPublishProgress;
import com.usface.activation.asynctask.MyAsyncTask;
import com.usface.activation.net.AuthAlgoRes;
import com.usface.activation.net.HttpUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {


    private static final String TAG ="MainActivity";

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraWrapper mCamera;


    private boolean mHasPermission = true;

    byte[] featureSingle = new byte[THFI_Param.faceFeatureSize];
    Integer[] rotationArray = new Integer[]{0, 90, 180, 270};
    private Bitmap gBitmap;
    private Matrix mMatrix;

    //private EditText mEtName;

    /*****************授权认证框相关控件 ******************/
    private AlertDialog certificationDialog;
    private Dialog messageDialog;
    /*****************授权认证框相关控件 ******************/

    private DetectFaceThread detectFaceThread;
    private LiveCheckThread liveCheckThread;
    private volatile float mLiveScore = 0f;
    private volatile boolean mLoadFeature = false;   // 是否在向内存加载特征中
    private boolean firstItemSelected;


    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;

    private static final int WHAT_DRAW = 0x01;
    private static final int WHAT_PROGRESS_SET_MAX = 0x03;
    private static final int WHAT_PROGRESS_VISIBLE = 0x04;
    private static final int WHAT_PROGRESS_INVISIBLE = 0x05;
    private static final int WHAT_PROGRESS_UPDATE_PROGRESS_MSG = 0x06;
    private static final int WHAT_PROGRESS_UPDATE_LIVERESULT_MSG = 0x07;
    private static final int WHAT_SHOW_TOAST_SHORT = 0x08;
    private static final int WHAT_SHOW_TOAST_LONG = 0x09;
    private static final int WHAT_SHOW_DIALOG = 0x10;
    private static final int WHAT_CERTIFICATE = 0x11;
    private static final int WHAT_INIT_FACEALGO = 0x12;

    private boolean bInitOk = false;
    private int iErrorCode = -1;

    private TextView tv_rotate;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_DRAW:
                    drawImage();
                    break;
                case WHAT_PROGRESS_UPDATE_PROGRESS_MSG:

                    showUserInfo(msg.arg1);
                    break;
                case WHAT_PROGRESS_UPDATE_LIVERESULT_MSG:

                    break;
                case WHAT_PROGRESS_SET_MAX:
                    break;
                case WHAT_PROGRESS_VISIBLE:
                    break;
                case WHAT_PROGRESS_INVISIBLE:
                    //mBtLoadFeature.setText("加载特征");
                    break;
                case WHAT_SHOW_TOAST_SHORT:
                    Toast.makeText(MainActivity.this, "" + msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case WHAT_SHOW_TOAST_LONG:
                    Toast.makeText(MainActivity.this, "" + msg.obj, Toast.LENGTH_LONG).show();
                    break;
                case WHAT_CERTIFICATE:

                    String password=sp.getString("password","");
                    String username=sp.getString("username","");

                    Log.i(TAG,"username: "+username+"       password: "+password);
                    startAuth(username, password);   // TODO 【请将 account 和 password 替换为自己的授权账号】

                    break;
                case WHAT_SHOW_DIALOG:
                    String message = (String) msg.obj;
                    showMessageDialog(message);
                    break;
                case WHAT_INIT_FACEALGO:
                    initFaceAlgo();
                    break;
            }
        }
    };
    private SqliteDataBase sd;
    private List<FaceUserInfo> faceUserInfos;
    private int windowW;
    private int windowH;
    private float scalew;
    private float scaleh;

    private String mUserName = "test";
    private String mUserPhoneNumber = "test";
    private String mUserCompany = "test";
    private String mUserAddress = "test";

    public VerifyThread verifyThread;
    private AlertDialog dialog;
    private boolean showCircle;
    private Paint paintName;
    private Paint paint;
    private int userIndex;
    private boolean needCertificate = true;

    private TextView tv_username;
    private TextView tv_phonenumber;
    private TextView tv_company;
    private TextView tv_address;
    private Paint paintNameUnknown;
    private int faceNum;
    private boolean canSaveUser;
    private boolean FeatureIsExist;
    private boolean LiveCheckGo;
    private SharedPreferences sp;
    private Button bt_switch_camera;
    private Spinner spinner;
    private CheckBox cb_horizontal;
    private int matchIndex;
    private LinearLayout ll_userinfo;
    private Button bt_person;
    private boolean EnrollThreadisGoing;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle("识别");

        sp = super.getSharedPreferences(THFI_Param.SP_FILE_NAME, Activity.MODE_PRIVATE);

        handler.sendEmptyMessageDelayed(WHAT_INIT_FACEALGO, 500);

        mSurfaceView = findViewById(R.id.am_face);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mSurfaceView.setLayoutParams(params);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(surfaceCallback);
        mSurfaceHolder.setKeepScreenOn(true);


        mCamera = new CameraWrapper(MainActivity.this, mCamePreviewCallback, mSurfaceHolder, false);
        mCamera.Config(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, THFI_Param.ACTUAL_WIDTH, THFI_Param.ACTUAL_HEIGHT, 1, -1, false);

        rs = RenderScript.create(this);
        yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));


        detectFaceThread = new DetectFaceThread();
        detectFaceThread.start();

        liveCheckThread = new LiveCheckThread();
        liveCheckThread.start();

        verifyThread = new VerifyThread();
        verifyThread.go = true;
        verifyThread.start();


        sd = SqliteDataBase.getInstance(getApplicationContext());
        userIndex = -2;
        EnrollThreadisGoing = false;

        initView();
        initDisPlay();
    }

    private void initView() {
        ll_userinfo = findViewById(R.id.ll_userinfo);

        tv_rotate = findViewById(R.id.tv_rotate);
        tv_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMatrix == null) {
                    return;
                }
                String key = THFI_Param.SP_KEY_MATRIX_ROTATE2;
                if (THFI_Param.CameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    key = THFI_Param.SP_KEY_MATRIX_ROTATE1;
                }
                int rotate = sp.getInt(key, -1);
                if (rotate == -1) {
                    rotate = mCamera.getRotateAngle();
                }
                rotate = (rotate % 360) + 90;
                mMatrix.setRotate(rotate);
                mMatrix.postScale(1, -1);

                sp.edit().putInt(key, rotate).commit();
            }
        });

        tv_username = findViewById(R.id.tv_username);
        tv_phonenumber = findViewById(R.id.tv_phonenumber);
        tv_company = findViewById(R.id.tv_company);
        tv_address = findViewById(R.id.tv_address);


        TextView tv_checkinfo = findViewById(R.id.tv_checkinfo);


        bt_switch_camera = findViewById(R.id.bt_switch_camera);
        bt_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.closeCamera();
                THFI_Param.CameraID = THFI_Param.CameraID == 0 ? 1 : 0;
                mCamera.openCamera(MainActivity.this, THFI_Param.CameraID);
                mCamera.startPreview();
            }
        });

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, rotationArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = super.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        for (int i = 0; i < rotationArray.length; i++) {
            if (THFI_Param.RotateAngle == rotationArray[i]) {
                spinner.setSelection(i);
            }
        }


        firstItemSelected = true;
        cb_horizontal = findViewById(R.id.cb_horizontal);
        cb_horizontal.setChecked(THFI_Param.ReversalHorizontal ? true : false);
        cb_horizontal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                THFI_Param.ReversalHorizontal = b;
                mMatrix = mCamera.getMatrix();
            }
        });

        final RelativeLayout rl_config = findViewById(R.id.rl_config);
        rl_config.setVisibility(View.INVISIBLE);
        ImageView iv_config = findViewById(R.id.iv_config);
        iv_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_config.setVisibility(rl_config.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            }
        });


    }


    private void initDisPlay() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        windowW = dm.widthPixels;
        windowH = dm.heightPixels;
        getCanvasScale();

        paintName = new Paint();
        paintName.setStrokeWidth(5);
        paintName.setColor(Color.GREEN);
        paintName.setTextAlign(Paint.Align.RIGHT);
        paintName.setTextSize(30);

        paintNameUnknown = new Paint();
        paintNameUnknown.setStrokeWidth(5);
        paintNameUnknown.setColor(Color.RED);
        paintNameUnknown.setTextAlign(Paint.Align.RIGHT);
        paintNameUnknown.setTextSize(30);


        paint = new Paint();
        paint.setColor(Color.parseColor("#A6A6A6"));
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth((float) 5.0);
    }

    private void initUserInfos() {
        if (faceUserInfos != null) {
            faceUserInfos.clear();
        }
        faceUserInfos = sd.queryAll();
        FaceCheck.clearFeature();
        for (int i = 0; i < faceUserInfos.size(); i++) {
            if (faceUserInfos.get(i).facefeature != null) {
                int j = FaceCheck.addFeature(i, faceUserInfos.get(i).facefeature);
            }
        }
        THFI_Param.EnrolledNum = faceUserInfos.size();

    }



    private void initFaceAlgo() {

        if (certificationDialog != null && certificationDialog.isShowing()) {
            certificationDialog.cancel();
        }

        new Thread() {   // 由于人脸初始化算法在某些性能较差的设备上用时过长，会有一定概率导致ANR发生，所以放在线程中初始化
            @Override
            public void run() {

                Message msg = null;
                bInitOk = false;
                iErrorCode = FaceCheck.init(MainActivity.this);

                if (iErrorCode > -1999 && iErrorCode < -999) {     // 授权检测错误，需要联网授权
                    String info = "";
                    if (iErrorCode == THFI_Param.ERROR_INVALID_DEVICE) {
                        info = "设备未授权";
                    } else if (iErrorCode == THFI_Param.ERROR_INVALID_DATE) {
                        info = "设备超过授权使用期限";
                    } else if (iErrorCode == THFI_Param.ERROR_INVALID_COUNT) {
                        info = "设备超过授权数量限制";
                    }

                    msg = Message.obtain();
                    msg.what = WHAT_SHOW_TOAST_LONG;
                    msg.obj = "授权文件校验失败，" + info + " code:" + iErrorCode;
                    handler.sendMessageDelayed(msg, 300);
                    if (needCertificate) {
                        handler.sendEmptyMessageDelayed(WHAT_CERTIFICATE, 500);  //certificate();
                        needCertificate = false;
                    }

                } else if (iErrorCode < 0) {    // 算法相关错误，需要根据具体错误码进行不同的处理
                    msg = Message.obtain();
                    msg.what = WHAT_SHOW_DIALOG;
                    msg.obj = "算法初始化错误，请重试，code:" + iErrorCode;
                    handler.sendMessageDelayed(msg, 500);

                } else if (iErrorCode >= 0) {
                    bInitOk = true;
                    initUserInfos();
                }
            }

        }.start();
    }



    private void startAuth(final String account, final String password) {

        MyAsyncTask.<Void, Void, AuthAlgoRes>newBuilder()
                .setPreExecute(new IPreExecute() {
                    @Override
                    public void onPreExecute() {
                    }
                })
                .setDoInBackground(new IDoInBackground<Void, Void, AuthAlgoRes>() {
                    @Override
                    public AuthAlgoRes doInBackground(IPublishProgress<Void> publishProgress, Void... params) {
                        return HttpUtil.verifyAlgo(MainActivity.this, account, password);
                    }
                })
                .setViewActive(new IIsViewActive() {
                    @Override
                    public boolean isViewActive() {
                        return MainActivity.this.isViewActive();
                    }
                })
                .setPostExecute(new IPostExecute<AuthAlgoRes>() {
                    @Override
                    public void onPostExecute(AuthAlgoRes result) {
                        processAuthResult(result);
                    }
                })
                .start();
    }

    private void processAuthResult(AuthAlgoRes result) {
        bInitOk = false;
        iErrorCode = -1;
        Message msg = Message.obtain();

        if (result != null) {
            msg.what = WHAT_SHOW_TOAST_LONG;
            if (result.code.equals("1000")) {
                msg.obj = "授权激活成功！";
                handler.sendEmptyMessage(WHAT_INIT_FACEALGO);
                // 激活成功后进行算法初始化操作

            } else {     // 激活失败，重新开启激活
                msg.obj = "授权激活失败，错误码：" + result.code + "，错误信息：" + result.msg;
            }
            handler.sendMessageDelayed(msg, 0);

        } else {    // 激活失败，重新开启激活
            msg.what = WHAT_SHOW_TOAST_LONG;
            msg.obj = "授权激活失败，请检查网络连接！";
            handler.sendMessageDelayed(msg, 0);
        }
    }


    private void showMessageDialog(String message) {
        if (messageDialog != null && messageDialog.isShowing()) {
            messageDialog.cancel();
            messageDialog = null;
        }
        messageDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("")
                .setIcon(R.drawable.shape_alert_title)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setMessage(message + "")
                .create();

        try {
            messageDialog.show();
        } catch (Exception e) {
            Log.i("face_test", "MainActivity.java MainActivity showMessageDialog error:" + e.getMessage());
        }
    }

    public boolean isViewActive() {
        return !(isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed()));
    }



    private void drawImage() {

        Canvas canvas = null;
        Bitmap bmp;
        List<FaceUserInfo> arr;
        int index;

        synchronized (MainActivity.class) {
            arr = faceUserInfos;
            index = userIndex;
        }

        try {
            bmp = Bitmap.createBitmap(gBitmap, 0, 0, THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, mMatrix, true);
            detectFaceThread.setBitmap(bmp.copy(bmp.getConfig(), false));

            canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                Canvas canvasBmp = new Canvas(bmp);

                Rect[] rect = detectFaceThread.getFaceRect();
                THFI_FacePos[] facePos = detectFaceThread.getFacePos();

                if (!showCircle) {
                    if (dialog == null || (dialog != null && !dialog.isShowing())) {
                        if (rect != null) {

                            for (int i = 0; i < rect.length; i++) {
                                float[] floats = new float[]{
                                        (float) rect[i].left, (float) rect[i].top, rect[i].left + (rect[i].right - rect[i].left) / 3, (float) rect[i].top,
                                        (float) rect[i].left, (float) rect[i].top, (float) rect[i].left, (float) rect[i].top + (rect[i].bottom - rect[i].top) / 3,
                                        (float) rect[i].right, (float) rect[i].top, (float) rect[i].right - (rect[i].right - rect[i].left) / 3, (float) rect[i].top,
                                        (float) rect[i].right, (float) rect[i].top, (float) rect[i].right, (float) rect[i].top + (rect[i].bottom - rect[i].top) / 3,
                                        (float) rect[i].left, (float) rect[i].bottom, rect[i].left + (rect[i].right - rect[i].left) / 3, (float) rect[i].bottom,
                                        (float) rect[i].left, (float) rect[i].bottom, rect[i].left, (float) rect[i].bottom - (rect[i].bottom - rect[i].top) / 3,
                                        (float) rect[i].right, (float) rect[i].bottom, rect[i].right - (rect[i].right - rect[i].left) / 3, (float) rect[i].bottom,
                                        (float) rect[i].right, (float) rect[i].bottom, rect[i].right, (float) rect[i].bottom - (rect[i].bottom - rect[i].top) / 3
                                };
                                canvasBmp.drawLines(floats, paint);
                            }

                            float visibilityLimitation = bmp.getWidth() * THFI_Param.FACE_VISIBILITY;
                            if (Math.abs(rect[0].right - rect[0].left) > visibilityLimitation) {   // 人脸图像无效，则提示请远离
                                canvasBmp.drawText("请远离", rect[0].right, rect[0].top - 10, paintNameUnknown);

                            } else if (paintName != null && index >= 0 && arr != null && arr.size() >= index && arr.size() > 0) {
                                canvasBmp.drawText(arr.get(index).m_UserName + "_" + mLiveScore, rect[0].right, rect[0].top - 10, paintName);

                            } else if (rect != null && index < 0) {
                                if (mLiveScore > THFI_Param.LIVE_THRESHOLD) {
                                    canvasBmp.drawText("未知" + "_" + mLiveScore, rect[0].right, rect[0].top - 10, paintName);
                                } else {
                                    canvasBmp.drawText("未知" + "_" + mLiveScore, rect[0].right, rect[0].top - 10, paintNameUnknown);
                                }
                            }
                        }

                        if (facePos != null) {
                            for (int i = 0; i < facePos.length; i++) {
                                THFI_FacePos pos = facePos[i];
                                canvasBmp.drawPoint(pos.ptLeftEye.x, pos.ptLeftEye.y, paint);
                                canvasBmp.drawPoint(pos.ptMouth.x, pos.ptMouth.y, paint);
                                canvasBmp.drawPoint(pos.ptNose.x, pos.ptNose.y, paint);
                                canvasBmp.drawPoint(pos.ptRightEye.x, pos.ptRightEye.y, paint);
                            }
                        }

                    }
                }

                if (showCircle) {
                    canvasBmp.drawCircle(THFI_Param.ACTUAL_WIDTH / 2, THFI_Param.ACTUAL_HEIGHT / 2, 170, paint);
                }

                canvas.scale(scalew, scalew);
                canvas.drawBitmap(bmp, 0, 0, null);
            }

        } catch (Exception e) {
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EnrollThreadisGoing = true;
        ll_userinfo.setVisibility(View.INVISIBLE);

        Log.e("wsm", "onActivityResult");
        Log.e("wsm", "requestCode " + requestCode + "resultCode " + resultCode);
        if (resultCode == 2) {
            try {
                mUserName = data.getStringExtra("username");
                mUserPhoneNumber = data.getStringExtra("userphonenumber");
                mUserCompany = data.getStringExtra("usercompany");
                mUserAddress = data.getStringExtra("useraddress");
                new EnrollThread().start();
               /* toolbar.setRightbuttonTitle("识别");
                toolbar.setMainTitle("注册");*/
            } catch (Exception e) {

            }
        } else {
            Toast.makeText(getApplicationContext(), "信息填写有误", Toast.LENGTH_SHORT).show();
            if (verifyThread != null) {
                verifyThread.go = true;
            }
            /*toolbar.setRightbuttonTitle("注册");
            toolbar.setMainTitle("识别");*/
            EnrollThreadisGoing = false;
        }
    }

    //    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void ShowDialog(int i, final byte[] finalBitmapByte, final byte[] faceFeat, final String mUserName, final String mUserPhoneNumber, final String mUserCompany, final String mUserAddress) {

        final int temp = i;
        View view = LayoutInflater.from(this).inflate(R.layout.item_dialog, null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_name = view.findViewById(R.id.tv_name);
        Button bt_sure = view.findViewById(R.id.bt_sure);
        bt_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog != null) {
                    if (temp != 1) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                       /* toolbar.setRightbuttonTitle("注册");
                        toolbar.setMainTitle("识别");*/
                        verifyThread.go = true;
                        EnrollThreadisGoing = false;
                    } else {
                        SqliteDataBase instance = SqliteDataBase.getInstance(getApplicationContext());
                        FaceUserInfo faceUserInfo = new FaceUserInfo();
                        faceUserInfo.m_Uid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
                        Log.e("UUID", faceUserInfo.m_Uid);
                        faceUserInfo.m_UserName = mUserName;
                        faceUserInfo.m_EnrollTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
                        faceUserInfo.facefeature = faceFeat;
                        faceUserInfo.mPhoneNumber = mUserPhoneNumber;
                        faceUserInfo.mCompany = mUserCompany;
                        faceUserInfo.mAddress = mUserAddress;
                        faceUserInfo.mFaceBitmapArray = finalBitmapByte;
                        instance.insertUserData(faceUserInfo);
                        initUserInfos();
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        verifyThread.go = true;
                        EnrollThreadisGoing = false;
                    }

                }
            }
        });

        Button bt_save_face = view.findViewById(R.id.bt_save_face);
        bt_save_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* toolbar.setRightbuttonTitle("识别");
                toolbar.setMainTitle("注册");*/
                verifyThread.go = false;
                if (dialog != null) {
                    dialog.dismiss();
                }
                new EnrollThread().start();
            }
        });

        Drawable drawableError = getResources().getDrawable(R.mipmap.error);
        drawableError.setBounds(0, 0, 100, 100);
        Drawable drawableSuccess = getResources().getDrawable(R.mipmap.ic_success);
        drawableSuccess.setBounds(0, 0, 100, 100);
        if (i != 1) {
            tv_title = view.findViewById(R.id.tv_title);

            tv_title.setCompoundDrawables(drawableError, null, null, null);
            tv_title.setText("注册失败");
            tv_title.setTextColor(Color.RED);
            tv_name.setText("请重试");
            bt_save_face.setVisibility(View.VISIBLE);

        } else {
            bt_save_face.setVisibility(View.GONE);
            tv_title.setText("注册成功");
            tv_title.setTextColor(Color.GREEN);
            tv_name.setText("姓名 : " + mUserName);
            tv_title.setCompoundDrawables(drawableSuccess, null, null, null);
        }

        if (!MainActivity.this.isFinishing()) {
            //创建对话框
            dialog = new AlertDialog.Builder(this, R.style.bottom_dialog_08).create();
            //dialog.setIcon(R.mipmap.ic_launcher);//设置图标
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            //添加布局
            dialog.setContentView(view);
            WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
            lp.alpha = 0.9f;
            dialog.getWindow().setAttributes(lp);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    verifyThread.go = true;
                    EnrollThreadisGoing = false;
                }
            });
        }
    }


    public void openCamera(View view) {

        if (mCamera == null) {
            mCamera = new CameraWrapper(MainActivity.this, mCamePreviewCallback, mSurfaceHolder, false);
            mCamera.Config(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, THFI_Param.ACTUAL_WIDTH, THFI_Param.ACTUAL_HEIGHT, 1, -1, false);
        }

        if (mHasPermission && !mCamera.isPreviewing) {
            mCamera.openCamera(MainActivity.this, THFI_Param.CameraID);
            mMatrix = mCamera.getMatrix();
            mCamera.startPreview();
        } else {

        }
    }


    @Override
    public void onBackPressed() {
        mLoadFeature = false;
        if (certificationDialog != null) {
            certificationDialog.cancel();
        }
        if (messageDialog != null) {
            messageDialog.cancel();
        }
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
        mLoadFeature = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("wsm", "onResume");
        if (verifyThread != null && EnrollThreadisGoing == false) {
            verifyThread.go = true;
        }

        liveCheckThread.clearmFaceFeatures();
        FeatureIsExist = false;
        initUserInfos();
        SetUserInfoContent("", "", "", "");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("wsm", "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("wsm", "onPause");
        if (verifyThread != null) {
            verifyThread.go = false;
        }
    }

    @Override
    protected void onDestroy() {

        if (detectFaceThread != null) {
            detectFaceThread.stopDetect();
            detectFaceThread = null;
        }
        if (liveCheckThread != null) {
            liveCheckThread.stopDetect();
            liveCheckThread = null;
        }
        if (verifyThread != null) {
            verifyThread.stopDetect();
            liveCheckThread = null;
        }

        if (gBitmap != null) {
            gBitmap.recycle();
            gBitmap = null;
        }

        if (yuvToRgbIntrinsic != null) {
            yuvToRgbIntrinsic.destroy();
            yuvToRgbIntrinsic = null;
        }
        if (rs != null) {
            rs.destroy();
            rs = null;
        }
        if (in != null) {
            in.destroy();
            in = null;
        }
        if (out != null) {
            out.destroy();
            out = null;
        }

        if (certificationDialog != null) {
            certificationDialog.cancel();
        }
        if (messageDialog != null) {
            messageDialog.cancel();
        }

        FaceCheck.Release();
        FaceCheck.FaceEngineRelease();
        THFI_Param.EnrolledNum = 0;
        super.onDestroy();
    }


    /**
     * Android Camera类的回调
     */
    private Camera.PreviewCallback mCamePreviewCallback = new Camera.PreviewCallback() {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            if (yuvType == null) {
                yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
                in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(THFI_Param.IMG_WIDTH).setY(THFI_Param.IMG_HEIGHT);
                out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }

            in.copyFrom(data);

            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);

            gBitmap = Bitmap.createBitmap(THFI_Param.IMG_WIDTH, THFI_Param.IMG_HEIGHT, Bitmap.Config.ARGB_8888);
            out.copyTo(gBitmap);

            if (gBitmap != null) {
                handler.sendEmptyMessage(WHAT_DRAW);

            } else {

            }

        }
    };


    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mHasPermission) {
                openCamera(null);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.closeCamera();
            }
        }
    };


    private volatile boolean isLoading = false;

    private class LoadFeatureThread extends Thread {

        public LoadFeatureThread() {
        }

        @Override
        public void run() {

            isLoading = true;
            handler.sendEmptyMessage(WHAT_PROGRESS_VISIBLE);
            synchronized (MainActivity.this) {
                loadFeature();
            }
            handler.sendEmptyMessage(WHAT_PROGRESS_INVISIBLE);
            isLoading = false;
        }

        private void loadFeature() {

            File folder = new File(FaceFunction.PATH);
            if (!folder.exists()) {
                return;
            }

            String[] fileNames = folder.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(THFI_Param.SUFFIX)) {
                        return true;
                    }
                    return false;
                }
            });

            Message msg = Message.obtain();
            msg.what = WHAT_PROGRESS_SET_MAX;
            msg.arg1 = fileNames.length;
            handler.sendMessage(msg);

            THFI_Param.EnrolledNum = 0;
            THFI_Param.FaceName.clear();
            //FaceCheck.clearFeature();

            mLoadFeature = true;
            for (String fileName : fileNames) {
                if (!mLoadFeature) {
                    Log.i("facedemo", "LoadFeature break, mLoadFeature is false");
                    break;
                }

                File currentFile = new File(FaceFunction.PATH + fileName);

                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(currentFile);
                    fis.read(featureSingle);

                    int addResult = FaceCheck.addFeature(THFI_Param.EnrolledNum, featureSingle);
                    if (addResult != THFI_Param.SUCCESS) {
                        Log.i("facedemo", "LoadFeature addFeature end addResult:" + addResult);
                        msg = Message.obtain();
                        msg.what = WHAT_SHOW_TOAST_SHORT;
                        msg.obj = "加载特征异常，错误码：" + addResult;
                        handler.sendMessage(msg);
                        break;
                    }
                    THFI_Param.FaceName.add(THFI_Param.EnrolledNum, currentFile.getName().split("\\.")[0]);
                    //THFI_Param.FacePhoneNumber.add(THFI_Param.EnrolledNum, currentFile.getName().split("\\.")[0].split("_").[1]);
                    THFI_Param.EnrolledNum++;

                    msg = Message.obtain();
                    msg.what = WHAT_PROGRESS_UPDATE_PROGRESS_MSG;
                    msg.arg1 = THFI_Param.EnrolledNum;
                    msg.arg2 = fileNames.length;
                    handler.sendMessage(msg);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (fis == null) {
                        } else {
                            fis.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private class EnrollThread extends Thread {


        public EnrollThread() {

        }

        @Override
        public void run() {
            verifyThread.go = false;
            canSaveUser = true;
            liveCheckThread.clearmFaceFeatures();
            liveCheckThread.clearBGR();
            showCircle = true;
//            switchLiveCheck(true);
//            final Bitmap faceBitMapWithBlocking = liveCheckThread.getFaceBitMapWithBlocking();//扣脸
//            byte[] bitmapByte=null;
//            if(faceBitMapWithBlocking!=null) {
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                faceBitMapWithBlocking.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                bitmapByte = baos.toByteArray();
//            }
            //final boolean enrollResult = FaceFunction.saveFaceFeatures(liveCheckThread.getFaceFeaturesWithBlocking(), mUserName,mUserPhoneNumber,mUserCompany,mUserAddress,bitmapByte,sd);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean enrollResult = false;
            final byte[] faceFeat = liveCheckThread.getFaceFeaturesWithBlocking();
            if (faceFeat != null) {
                enrollResult = true;
            } else {
                Log.e("EnrollThread", "EnrollThread_faceFeat=null");
                enrollResult = false;
            }
            //final byte[] finalBitmapByte = bitmapByte;
            final boolean finalEnrollResult = enrollResult;
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (finalEnrollResult) {
                        if (canSaveUser) {
                            saveUserInfo(null, faceFeat, mUserName, mUserPhoneNumber, mUserCompany, mUserAddress);
                            /*toolbar.setRightbuttonTitle("注册");
                            toolbar.setMainTitle("识别");*/
                        }

                        //FeatureIsExist=false;
                        //Toast.makeText(MainActivity.this,"建模成功",Toast.LENGTH_LONG).show();


//                            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
//
//                            intent.putExtra("bitmap", finalBitmapByte);
//                            intent.putExtra("faceFeat",faceFeat);
//                            intent.putExtra("username",mUserName);
//                            intent.putExtra("userphonenumber",mUserPhoneNumber);
//                            intent.putExtra("usercompany",mUserCompany);
//                            intent.putExtra("useraddress",mUserAddress);
                        //if(dialog!=null&&!dialog.isShowing())
                        //startActivityForResult(intent,3);

                        //ShowDialog();
                        //bt_save_face.setVisibility(View.INVISIBLE);
                    } else {
                        Toast.makeText(MainActivity.this, "建模失败", Toast.LENGTH_LONG).show();
                        if (canSaveUser) {
                            ShowDialog(0, null, null, null, null, null, null);
                        }
                        //bt_save_face.setVisibility(View.VISIBLE);
                    }
                    showCircle = false;
//                    try {
//                        TimeUnit.SECONDS.sleep(2);
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

//
// getPixelsBGRDrawRect(imageBitmap, new Rect(facePos[0].rcFace.left, facePos[0].rcFace.top, facePos[0].rcFace.right, facePos[0].rcFace.bottom), img);
                }

            });

            //switchLiveCheck(mCheckBoxStatus);
            // 恢复注册前的复选框状态
        }
    }

    private void saveUserInfo(byte[] finalBitmapByte, byte[] faceFeat, String mUserName, String mUserPhoneNumber, String mUserCompany, String mUserAddress) {
        if (null == mUserName) {
            Toast.makeText(MainActivity.this, "用户名不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        ShowDialog(1, finalBitmapByte, faceFeat, mUserName, mUserPhoneNumber, mUserCompany, mUserAddress);

    }

    private class VerifyThread extends Thread {
        public volatile boolean IsVerify = false;
        public boolean go = false;

        public VerifyThread() {
        }


        public void stopDetect() {
            IsVerify = false;
            try {
//                this.join();
                this.interrupt();
            } catch (Exception e) {
            }
        }

        @Override
        public synchronized void run() {
            IsVerify = true;
            final boolean a = true;
            while (IsVerify) {
//                try {
//                    sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                if (go) {
                    //switchLiveCheck(true);
                    byte[] faceFeaturesWithBlocking = liveCheckThread.getFaceFeaturesWithBlocking();
                    matchIndex = FaceFunction.faceComparison1ToNMem(faceFeaturesWithBlocking);
                    Log.e("wsm", "matchIndex =" + matchIndex);

                    Message msg = Message.obtain();
                    msg.what = WHAT_PROGRESS_UPDATE_PROGRESS_MSG;
                    msg.arg1 = matchIndex;
                    handler.sendMessage(msg);


                    //switchLiveCheck(mCheckBoxStatus);
                }
            }// 恢复识别前的复选框状态
        }
    }

    int count = 0;

    private void showUserInfo(int matchIndex) {
        userIndex = matchIndex;
        if (userIndex >= 0) {
            ll_userinfo.setVisibility(View.VISIBLE);
            UpDataUserInfo();
        } else {
            ll_userinfo.setVisibility(View.INVISIBLE);
        }
    }

    private void UpDataUserInfo() {
        SetUserInfoContent(faceUserInfos.get(userIndex).m_UserName, faceUserInfos.get(userIndex).mPhoneNumber, faceUserInfos.get(userIndex).mCompany, faceUserInfos.get(userIndex).mAddress);
    }

    void SetUserInfoContent(String m_UserName, String mPhoneNumber, String mCompany, String mAddress) {
        try {
            tv_username.setText(m_UserName);
            tv_phonenumber.setText(mPhoneNumber);
            tv_company.setText(mCompany);
            tv_address.setText(mAddress);
        } catch (Exception e) {

        }
    }

    private class DetectFaceThread extends Thread {

        private volatile boolean isDetecting = false;
        private boolean isFacePosValid = false;

        private Rect[] rect = null;
        private THFI_FacePos[] facePos = null;
        private Bitmap mBitmap;
        private Object object = new Object();


        public DetectFaceThread() {
        }

        public void stopDetect() {
            isDetecting = false;

            try {
                this.interrupt();
            } catch (Exception e) {
            }
        }

        public Rect[] getFaceRect() {

            return rect;
        }

        public THFI_FacePos[] getFacePos() {
            return facePos;
        }


        public void setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        @Override
        public void run() {

            isDetecting = true;

            while (isDetecting) {

                try {
                    TimeUnit.MILLISECONDS.sleep(2);
                } catch (Exception e) {
                }

                try {
                    if (mBitmap != null) {

                        THFI_FacePos[] facePos = new THFI_FacePos[THFI_Param.MAX_FACE_NUMS];
                        THFQ_Result[] faceResult = new THFQ_Result[THFI_Param.MAX_FACE_NUMS];
                        for (int i = 0; i < facePos.length; i++) {
                            facePos[i] = new THFI_FacePos();
                            faceResult[i] = new THFQ_Result();
                        }

                        byte[] pixelsBGR = FaceFunction.getPixelsBGR(mBitmap);
                        faceNum = FaceFunction.faceDetect(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), 30, facePos);

                        if (THFI_Param.ENABLE_FACE_QUALITY_CHECK && faceNum > 0) {  // 做人脸质量检测
                            THFQ_Param qParam = new THFQ_Param();
                            qParam.brightness_min = 25;
                            qParam.brightness_max = 75;
                            FaceCheck.THFQSetParam(qParam);

                            FaceFunction.faceQualityCheck(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, faceResult);
                            Log.i("facetest", "MainActivity.java DetectFaceThread run quality result blur:" + faceResult[0].blur
                                    + ", brightness:" + faceResult[0].brightness + ", glasses:" + faceResult[0].glasses
                                    + ", occlusion:" + faceResult[0].occlusion + ", hat:" + faceResult[0].hat);

                            int[] faceResultSingle = new int[1];
                            FaceFunction.faceQualityCheck(FaceCheck.CheckOperation.CHECK_BLUR, pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, faceResultSingle);
                            Log.i("facetest", "MainActivity.java DetectFaceThread run CHECK_BLUR:" + faceResultSingle[0]);

                            FaceFunction.faceQualityCheck(FaceCheck.CheckOperation.CHECK_BRIGHTNESS, pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, faceResultSingle);
                            Log.i("facetest", "MainActivity.java DetectFaceThread run CHECK_BRIGHTNESS:" + faceResultSingle[0]);

                            FaceFunction.faceQualityCheck(FaceCheck.CheckOperation.CHECK_GLASSES, pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, faceResultSingle);
                            Log.i("facetest", "MainActivity.java DetectFaceThread run CHECK_GLASSES:" + faceResultSingle[0]);

                            FaceFunction.faceQualityCheck(FaceCheck.CheckOperation.CHECK_HAT, pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, faceResultSingle);
                            Log.i("facetest", "MainActivity.java DetectFaceThread run CHECK_HAT:" + faceResultSingle[0]);

                            FaceFunction.faceQualityCheck(FaceCheck.CheckOperation.CHECK_OCCLUSION, pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, faceResultSingle);
                            Log.i("facetest", "MainActivity.java DetectFaceThread run CHECK_OCCLUSION:" + faceResultSingle[0]);
                        }

                        if (faceNum >= 1) {
                            this.facePos = facePos;
                            rect = new Rect[faceNum];
                            for (int i = 0; i < faceNum; i++) {
                                rect[i] = new Rect(facePos[i].rcFace.left, facePos[i].rcFace.top, facePos[i].rcFace.right, facePos[i].rcFace.bottom);

                                Log.e("rect", rect[0].bottom + "---" + rect[0].top + "---" + rect[0].left + "---" + rect[0].right + "---");
                            }
                            isFacePosValid = true;

                            liveCheckThread.setImageData(pixelsBGR, mBitmap.getWidth(), mBitmap.getHeight(), facePos, mBitmap);   // 活体检测
                        } else {
                            mLiveScore = 0;
                            rect = null;
                            this.facePos = null;
                            userIndex = -1;
                            LiveCheckGo = true;
                            if (ll_userinfo.getVisibility() == View.VISIBLE) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ll_userinfo.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        }

                    }

                } catch (Exception e) {

                } finally {
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        mBitmap = null;
                    }
                }
            }
        }
    }


    private class LiveCheckThread extends Thread {

        private Object object = new Object();

        private volatile boolean isDetecting = false;
        private byte[] mPixelsBGR;
        private int mWidth;
        private int mHeight;
        private THFI_FacePos[] mFaceRect;
        private byte[] mFaceFeatures;
        private Bitmap mBitmap;
        private Bitmap mFaceBitmap;

        public LiveCheckThread() {
        }

        public void stopDetect() {
            isDetecting = false;
            try {
//                this.join();
                this.interrupt();
            } catch (Exception e) {
            }
        }

        public void setImageData(byte[] pixelsBGR, int width, int height, THFI_FacePos[] rect, Bitmap Bitmap) {
            mPixelsBGR = pixelsBGR;
            mWidth = width;
            mHeight = height;
            mFaceRect = rect;
            this.mBitmap = Bitmap;
        }


        public byte[] getFaceFeaturesWithBlocking() {

            //stopLiveCheck = false;
//            mNeedFeature = true;

            synchronized (object) {
                try {
                    object.wait(THFI_Param.OPERATE_TIME_OUT);
                    return mFaceFeatures;
                } catch (InterruptedException e) {
                } finally {
//                    stopLiveCheck = true;
//                    mNeedFeature = false;
                }
                // return mFaceFeatures;
            }
            return null;
        }

        public Bitmap getFaceBitMapWithBlocking() {

            synchronized (object) {
                try {
                    object.wait(THFI_Param.OPERATE_TIME_OUT);
                    return mFaceBitmap;
                } catch (InterruptedException e) {
                } finally {
//                    stopLiveCheck = true;

                }
            }
            return null;
        }

        public void clearmFaceFeatures() {
            mFaceFeatures = null;
        }


        public void clearBGR() {
            mPixelsBGR = null;
        }

        @Override
        public void run() {

            isDetecting = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

            while (isDetecting) {

                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (Exception e) {
                }

                try {
                    if (mPixelsBGR != null && mFaceRect != null) {

                        if (LiveCheckGo) {
                            mFaceFeatures = null;

                            float visibilityLimitation = mBitmap.getWidth() * THFI_Param.FACE_VISIBILITY;
                            if (Math.abs(mFaceRect[0].rcFace.right - mFaceRect[0].rcFace.left) < visibilityLimitation) {   // 人脸图像在有效区域范围内再做活体检测
                                mLiveScore = FaceFunction.faceLiveCheck(mPixelsBGR, mFaceRect, mWidth, mHeight);
                            } else {
                                mLiveScore = 0;
//                                Log.e("LiveCheckThread","人脸图像无效");
                            }

//                            Log.e("LiveCheckThread","检测活体  分数 = " +mLiveScore);

                            if (mLiveScore > THFI_Param.LIVE_THRESHOLD) {
                                LiveCheckGo = false;
                                mFaceFeatures = FaceFunction.faceFeatures(mPixelsBGR, mWidth, mHeight, 30, mFaceRect);
                            } else {
                                LiveCheckGo = true;
                            }

                            if (mFaceFeatures != null) {
                                synchronized (object) {
                                    try {
                                        object.notifyAll();
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            handler.sendEmptyMessage(WHAT_PROGRESS_UPDATE_LIVERESULT_MSG);
                        } else {
//                            Log.e("LiveCheckThread"," 不检测" +mNeedFeature);
                            count++;
                            if (count >= 3) {
                                count = 0;
                                LiveCheckGo = true;
                            }
                        }
                    }

                } catch (Exception e) {

                } finally {
                }
            }
        }

        private void saveImageAndFeature(Bitmap bitmap, byte[] faceFeatures, float liveScore, int width, int height, String time) {

            try {
                File folder = new File("/sdcard/faceimages/");
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String baseName = time + "-" + liveScore + "-" + width + "-" + height;

                File file = new File(folder, baseName + ".jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                if (faceFeatures != null) {
                    FileUtil.saveData(faceFeatures, folder.getAbsolutePath(), baseName + ".dat");
                }
            } catch (Exception e) {

            }

        }
    }


    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Integer rotation = (Integer) MainActivity.this.spinner.getItemAtPosition(position);    // 【这里的处理】
            if (firstItemSelected) {
                firstItemSelected = false;
                return;
            }
            if (THFI_Param.RotateAngle == rotation) {
                return;
            }
            THFI_Param.RotateAngle = rotation;

//            if(position!=0) {
            Log.e("wsm", "清屏");
            for (int i = 0; i < 5; i++) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }

            mMatrix = mCamera.getMatrix();
            if (rotation == 0 || rotation == 180) {
                THFI_Param.ACTUAL_WIDTH = THFI_Param.IMG_WIDTH;
                THFI_Param.ACTUAL_HEIGHT = THFI_Param.IMG_HEIGHT;
                getCanvasScale();
            } else if (rotation == 90 || rotation == 270) {
                THFI_Param.ACTUAL_WIDTH = THFI_Param.IMG_HEIGHT;
                THFI_Param.ACTUAL_HEIGHT = THFI_Param.IMG_WIDTH;
                getCanvasScale();
            }

//                mCamera.closeCamera();
//                gBitmap=null;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            TimeUnit.SECONDS.sleep(5);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        mCamera.openCamera(MainActivity.this,THFI_Param.CameraID);
//                        mCamera.startPreview();
//                    }
//                }).start();
//            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private void getCanvasScale() {
        scalew = (float) windowW / (float) THFI_Param.ACTUAL_WIDTH;
        scaleh = (float) windowH / (float) THFI_Param.ACTUAL_HEIGHT;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, "注册")
                .setVisible(true)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        Intent intent = new Intent(MainActivity.this, EditUserInfoActivity.class);
                        startActivityForResult(intent, 2);

                        return false;
                    }
                })
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


}
