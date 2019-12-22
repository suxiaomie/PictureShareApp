package com.example.mcl.picshare;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.mcl.picshare.LoginActivity.USERKEY;
import static com.example.mcl.picshare.utils.OkHttpUtil.downHost;
import static com.example.mcl.picshare.utils.OkHttpUtil.updatePswdHost;

public class UpdatePswdActivity extends AppCompatActivity {

    private Context context;
    final int updateOK = 1;
    final int updateFail = 0;
    final int nofind = -1;
    private EditText oldPswdET;
    private EditText newPswdET;
    private Button updateBT;
    String oldPswd = "";
    String newPswd = "";
    String user = "";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case updateOK:
                    Toast.makeText(context,"修改成功，请重新登录",Toast.LENGTH_SHORT).show();
                    Intent intent_login = new Intent();
                    intent_login.setClass(context,LoginActivity.class);
                    intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //关键的一句，将新的activity置为栈顶
                    startActivity(intent_login);
                    finish();
                    break;
                case updateFail:
                    Toast.makeText(context,"修改失败",Toast.LENGTH_SHORT).show();
                    break;
                case nofind:
                    Toast.makeText(context,"原密码错误",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pswd);
        Intent intent = getIntent();

        user = intent.getStringExtra(USERKEY);
        context = UpdatePswdActivity.this;
        oldPswdET = findViewById(R.id.oldPswdET1);
        newPswdET = findViewById(R.id.newPswdET1);
        updateBT = findViewById(R.id.updateBT);


        updateBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("updateBT","updateBT");
                oldPswd = oldPswdET.getText().toString().trim();
                newPswd = newPswdET.getText().toString().trim();
                updatePSWD(user,oldPswd,newPswd);
            }
        });
    }

    public void updatePSWD(String user,String oldpswd,String newpswd){
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建RequestBody封装参数
        RequestBody builder = new FormBody.Builder()
                .add("user",user)
                .add("oldpswd",oldpswd)
                .add("newpswd",newpswd)
                .build();
        //创建request
        Request request = new Request.Builder()
                .url(updatePswdHost)
                .post(builder)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error","" + e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string().trim();
                Log.e("修改密码结果",temp);
                Message message = mHandler.obtainMessage();
                if("success".equals(temp)){
                    message.what = updateOK;
                }else if("nofind".equals(temp)){
                    message.what = nofind;
                }else{
                    message.what = updateFail;
                }
                mHandler.sendMessage(message);
            }
        });
    }
}
