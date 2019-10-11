package com.cw.facedemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.huiyuenet.faceCheck.THFI_Param;


public class StartActivity extends AppCompatActivity {

    boolean isLogin = false;
    private Button bt_go;
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;
    private EditText et_username;
    private EditText et_password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle("人脸识别Demo");
        requestPermission(
                new String[]{
                        "android.permission.WRITE_EXTERNAL_STORAGE",
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.CAMERA",
                        Manifest.permission.INTERNET
                }
        );
        sp = this.getSharedPreferences(THFI_Param.SP_FILE_NAME, MODE_PRIVATE);

        edit = sp.edit();
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        isLogin = sp.getBoolean("isLogin", false);

        String username = sp.getString("username", "");
        String password = sp.getString("password", "");

        et_username.setText(username);
        et_password.setText(password);


    }

    private void initView() {
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);


        bt_go = findViewById(R.id.bt_go);


        bt_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //如果没有登录，就开始判断相关，存储登录

                String username = et_username.getText().toString();
                String password = et_password.getText().toString();

                if (username.equals("")) {
                    Toast.makeText(StartActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.equals("")) {
                    Toast.makeText(StartActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                edit.putString("username", username);
                edit.putString("password", password);

                edit.putBoolean("isLogin", true);

                edit.commit();
                edit.apply();

                if(isNetworkAvailable(StartActivity.this)){
                    startActivity(new Intent(StartActivity.this,MainActivity.class));

                }else {

                    Toast.makeText(StartActivity.this, "第一次激活需要联网，请将该设备联网联网!", Toast.LENGTH_SHORT).show();

                }



            }
        });

    }


    // 方法，获取位置权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限通过
            } else {
                // 权限拒绝
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    // 禁止后不再询问了！
                } else {
                    // 用户此次选择了禁止权限
                }
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private final int PERMISSION_REQUEST_CODE = 2000;

    @SuppressLint("WrongConstant")
    private void requestPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int resultCode = 0;
            for (String str : permissions) {
                resultCode = checkSelfPermission(str);
                if (resultCode != 0) {
                    if (resultCode == PackageManager.PERMISSION_DENIED) {
                        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 判断网络是否有效.
     *
     * @param context the context
     * @return true, if is network available
     */
    public boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


}
