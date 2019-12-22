package com.example.mcl.picshare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.mcl.picshare.utils.OkHttpUtil.loginHost;
import static com.example.mcl.picshare.utils.OkHttpUtil.signinHost;


public class LoginActivity extends AppCompatActivity {

    private Context context;
    private EditText userET;
    private EditText pswdET;
    private Button loginBT;
    private Button sighinBT;

    private Boolean isSwitch = false;   //默认密文
    boolean isRemenber;
    private ImageView switch_pic;
    private CheckBox rememberCB;
    private SharedPreferences sharedPref;



    private int userId = 0; //用户id
    static final String USERID = "USERID";
    public static final String USERKEY = "USERKEY";
    static final String PSWDKEY = "PSWDKEY";
    static final String ISREMENBER = "ISREMENBER";

    final int exit = -12;
    final int signOK = -11;
    final int signFail = -10;
    final int noExit = -3;
    final int logFail = -2;
    final int logOK = 1;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case exit:
                    Toast.makeText(context,"账号已存在",Toast.LENGTH_SHORT).show();
                    break;
                case signFail:
                    Toast.makeText(context,"注册失败",Toast.LENGTH_SHORT).show();
                    break;
                case signOK:
                    Toast.makeText(context,"注册成功",Toast.LENGTH_SHORT).show();
                    break;
                case noExit:
                    Toast.makeText(context,"账号未注册",Toast.LENGTH_SHORT).show();
                    break;
                case logFail:
                    Toast.makeText(context,"登录失败",Toast.LENGTH_SHORT).show();
                    break;
                case logOK:
                    Toast.makeText(context,"欢迎",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(context,TestActivity.class);
                    intent.putExtra(USERID,userId);
                    intent.putExtra(USERKEY,userET.getText().toString().trim());

                    startActivity(intent);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = LoginActivity.this;
        uiBind();
        regLinstener();
        requestPermission();
        init();

    }
    //组件绑定
    public void uiBind(){
        userET = findViewById(R.id.userET);
        pswdET = findViewById(R.id.pswdET);

        switch_pic = findViewById(R.id.switch_pic);
        rememberCB = findViewById(R.id.remember_pswd);

        loginBT = findViewById(R.id.loginBT);
        sighinBT = findViewById(R.id.sighinBT);

    }

    //注册监听器
    public void regLinstener(){

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = userET.getText().toString().trim();
                String pswd = pswdET.getText().toString().trim();
                //先保存密码
                rememberPswd(user,pswd);

                Log.e("login","login doing");
                login(user,pswd,loginHost);
            }
        });

        sighinBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("login","signin doing");
                String user = userET.getText().toString().trim();
                String pswd = pswdET.getText().toString().trim();
                login(user,pswd,signinHost);
            }
        });

        //明暗文转换
        switch_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSwitch = !isSwitch;
                if(isSwitch){
                    switch_pic.setImageResource(R.drawable.ic_visibility_black_24dp);
                    pswdET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    switch_pic.setImageResource(R.drawable.ic_visibility_off_black_24dp);
                    pswdET.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD |InputType .TYPE_CLASS_TEXT);
                    pswdET. setTypeface(Typeface.DEFAULT) ;
                }
            }
        });
    }

    //初始化并恢复密码
    public void init(){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String user = sharedPref.getString(USERKEY,"");
        String pswd = sharedPref.getString(PSWDKEY,"");
        isRemenber = sharedPref.getBoolean(ISREMENBER,false);//默认未保存
        if(isRemenber){
            userET.setText(user);
            pswdET.setText(pswd);
            rememberCB.setChecked(true);//选中
            Toast.makeText(context,"恢复密码",Toast.LENGTH_SHORT).show();
            Log.e("恢复密码","已恢复");
        }
    }

    /**
     * 记住密码
     * @param user
     * @param pswd
     */
    private void rememberPswd(String user,String pswd){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(rememberCB.isChecked()){
            editor.putString(USERKEY,user);
            editor.putString(PSWDKEY,pswd);
            editor.putBoolean(ISREMENBER,true);
            Log.e("记住密码","已记住");
        }else{
            editor.clear();
            Log.e("记住密码","已清空");
        }
        editor.apply();
    }

    /**
     * 注册登录验证
     * @param user 账号
     * @param pswd 密码
     * @param url 服务器接口地址
     */
    private void login(String user,String pswd,String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建RequestBody封装参数
        RequestBody builder = new FormBody.Builder()
                .add("user",user)
                .add("pswd",pswd)
                .build();
        //创建request
        Request request = new Request.Builder()
                .url(url)
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
                //注意加trim() 服务端加上了\n
                /*
                 *  -12 exit
                 *  -11 sign ok
                 *  -10 sign fail
                 *
                 *  -2 login fail
                 *  1-xxx userId
                 * */
                Log.e("login/sign",temp);
                userId = Integer.parseInt(temp);
                Message message = mHandler.obtainMessage();
                if(userId>0){
                    message.what = logOK;
                }else {
                    message.what = userId;
                }
                mHandler.sendMessage(message);
            }
        });

    }

    /**
     * 权限请求
     */
    public void requestPermission() {
        //检查是否已经有该权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },1);
            }else{
                Log.e("log","权限通过");
            }
        }
    }
}
