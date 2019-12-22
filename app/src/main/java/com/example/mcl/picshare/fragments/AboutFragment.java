package com.example.mcl.picshare.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mcl.picshare.R;
import com.example.mcl.picshare.UpdatePswdActivity;

import cn.sharesdk.framework.ShareSDK;

import static com.example.mcl.picshare.LoginActivity.USERKEY;


public class AboutFragment extends Fragment {

    private TextView update_pswdTV;
    String user = "";
    public void setUser(String user){this.user = user;}
    Context context;
    public void setContext(Context context){this.context = context;}
    public AboutFragment() {
        // Required empty public constructor
    }


    @Override                 //没有被载入或者想要动态载入的界面，都需要使用LayoutInflater.inflate()来载入
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("AboutFragment","AboutFragment");
        View view = inflater.inflate(R.layout.fragment_about,  container,false);
        update_pswdTV = view.findViewById(R.id.update_pswdTV);
        update_pswdTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,UpdatePswdActivity.class);
                intent.putExtra(USERKEY,user);//putExtra("A",B)中，AB为键值对，第一个参数为键名，第二个参数为键对应的值。
                Log.e("AboutFragment",user);
                startActivity(intent);
            }
        });
        return view;
    }

}
