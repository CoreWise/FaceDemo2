package com.cw.facedemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditUserInfoActivity extends AppCompatActivity implements View.OnClickListener {


    private EditText et_username;
    private EditText et_phonenumber;
    private EditText et_company;
    private EditText et_address;
    private Button bt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edituserinfo);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setTitle("录入注册信息");
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        et_username = (EditText) findViewById(R.id.et_username);
        et_phonenumber = (EditText) findViewById(R.id.et_phonenumber);
        et_company = (EditText) findViewById(R.id.et_company);
        et_address = (EditText) findViewById(R.id.et_address);
        Button bt_save_face = findViewById(R.id.bt_save_face);
        bt_save_face.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.bt_save_face:
                Intent intent = new Intent();
                // 获取用户计算后的结果
                String username = et_username.getText().toString();
                if (username.equals("")) {
                    Toast.makeText(EditUserInfoActivity.this, "用户名不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                String phonenumber = et_phonenumber.getText().toString();
                if (phonenumber.equals("")) {
                    Toast.makeText(EditUserInfoActivity.this, "电话号码不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                String company = et_company.getText().toString();
                if (company.equals("")) {
                    Toast.makeText(EditUserInfoActivity.this, "公司不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                String address = et_address.getText().toString();
                if (address.equals("")) {
                    Toast.makeText(EditUserInfoActivity.this, "公司不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                intent.putExtra("username", username);
                intent.putExtra("userphonenumber", phonenumber);
                intent.putExtra("usercompany", company);
                intent.putExtra("useraddress", address);
                setResult(2, intent);
                finish();
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
