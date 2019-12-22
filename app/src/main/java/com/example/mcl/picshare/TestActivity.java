package com.example.mcl.picshare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mcl.picshare.fragments.AboutFragment;
import com.example.mcl.picshare.fragments.PicFragment;
import com.example.mcl.picshare.fragments.UpLoadFragment;
import com.mob.MobSDK;


import static com.example.mcl.picshare.LoginActivity.USERID;
import static com.example.mcl.picshare.LoginActivity.USERKEY;

public class TestActivity extends AppCompatActivity {

    //底部导航栏
    private RelativeLayout bar1;
    private RelativeLayout bar2;
    private RelativeLayout bar3;

    //底部导航栏-文字
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;

    //底部导航栏-图片
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;


    //碎片
    FragmentManager fragmentManager = getSupportFragmentManager();//获取fragmrnt
    FragmentTransaction fragmentTransaction = null;
    Fragment fragment = null;


    Context context;
    private String user = "";
    private int userId = 0; //用户id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        context = TestActivity.this;
        Intent intent = getIntent();
        userId = intent.getIntExtra(USERID,0);
        user = intent.getStringExtra(USERKEY);
        uiBind();
        regListener();
        firstFragment();
    }

    private void uiBind(){
        bar1=findViewById(R.id.bar1);
        bar2=findViewById(R.id.bar2);
        bar3=findViewById(R.id.bar3);

        tv1 = findViewById(R.id.txt_1);
        tv2 = findViewById(R.id.txt_2);
        tv3 = findViewById(R.id.txt_3);

        iv1=findViewById(R.id.iv_bar_img1);
        iv2=findViewById(R.id.iv_bar_img2);
        iv3=findViewById(R.id.iv_bar_img3);

    }

    private void regListener(){
        bar1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("tv1","tv1");
                setSelectStatus(1);
                fragment = new UpLoadFragment();
                ((UpLoadFragment) fragment).setContext(context);
                ((UpLoadFragment) fragment).setUserId(userId);

                fragmentTransaction = fragmentManager.beginTransaction();//开启一个事务
                fragmentTransaction.replace(R.id.content_frament,fragment);//替换fragment
                fragmentTransaction.commit();//提交事务
            }
        });
        bar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstFragment();
            }
        });
        bar3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("tv3","tv3");
                setSelectStatus(3);
                fragment = new AboutFragment();
                ((AboutFragment) fragment).setContext(context);
                ((AboutFragment) fragment).setUser(user);
                fragmentTransaction = fragmentManager.beginTransaction();//开启一个事务
                fragmentTransaction.replace(R.id.content_frament,fragment);//替换fragment
                fragmentTransaction.commit();//提交事务
            }
        });
    }

    private void setSelectStatus(int index){
        switch (index){
            case 1:
                tv1.setTextColor(Color.parseColor("#000000"));
                tv2.setTextColor(Color.parseColor("#008577"));
                tv3.setTextColor(Color.parseColor("#008577"));

                iv1.setImageResource(R.drawable.ic_folder_black_24dp);
                iv2.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
                iv3.setImageResource(R.drawable.ic_person_outline_black_24dp);

                break;
            case 2:
                tv2.setTextColor(Color.parseColor("#000000"));
                tv1.setTextColor(Color.parseColor("#008577"));
                tv3.setTextColor(Color.parseColor("#008577"));

                iv2.setImageResource(R.drawable.ic_radio_button_checked_black_24dp);
                iv1.setImageResource(R.drawable.ic_folder_open_black_24dp);
                iv3.setImageResource(R.drawable.ic_person_outline_black_24dp);
                break;
            case 3:
                tv3.setTextColor(Color.parseColor("#000000"));
                tv2.setTextColor(Color.parseColor("#008577"));
                tv1.setTextColor(Color.parseColor("#008577"));

                iv3.setImageResource(R.drawable.ic_person_black_24dp);
                iv1.setImageResource(R.drawable.ic_folder_open_black_24dp);
                iv2.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
                break;
        }
    }

    //设为主fragment
    public void firstFragment(){
        Log.e("tv2","tv2");
        setSelectStatus(2);
        fragment = new PicFragment();
        ((PicFragment) fragment).setContext(context);
        fragmentTransaction = fragmentManager.beginTransaction();//开启一个事务
        fragmentTransaction.replace(R.id.content_frament,fragment);//替换fragment
        fragmentTransaction.commit();//提交事务
    }
}
