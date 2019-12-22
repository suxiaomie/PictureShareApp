package com.example.mcl.picshare.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import com.example.mcl.picshare.R;
import com.example.mcl.picshare.beans.Androidrecord;
import com.mob.MobSDK;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.mcl.picshare.utils.OkHttpUtil.addCountHost;
import static com.example.mcl.picshare.utils.OkHttpUtil.downHost;
import static com.example.mcl.picshare.utils.OkHttpUtil.jsonHost;
import static com.example.mcl.picshare.utils.OkHttpUtil.pageHost;

/**
 * A simple {@link Fragment} subclass.
 */
public class PicFragment extends Fragment {

    //private Button jsonBT;
    private ImageView picShowIV;
    private ImageButton lastBT;
    private Button countBT;
    private ImageButton nextBT;
    private ImageView countIV;
    private TextView countTV;

    //上传文件
    private Context context;
    public void setContext(Context context){this.context=context;}

    //相关数据
    //private int userId = 0; //用户id
    private int picID = -1;//当前图片id
    private int listIndex = 0;//当前list的下标
    private String picUrl = "";//当前图片名字
    private int picCount = -1;
    private List<Androidrecord> dataList = new ArrayList<>();

    //分页处理
    int page = 0;
    int allpage = 0;
    final int onePage = 5;

    //页码
    int allPic=0;
    int nowPage=1;
    //点赞
    private boolean isCount = true;//只有没点过才能点


    //Handler异步刷新
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    //拿到obj中的数据
                    byte[] bytes = (byte[]) msg.obj;
                    //使用BitmapFactory将字节转换成bitmap类型
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    //设置图片
                    picShowIV.setImageBitmap(bitmap);
                    String t = Integer.toString(picCount);
                    countTV.setText(t);
                    countBT.setText(nowPage+"/"+allPic);
                    isCount = false;
                    break;
                case 0:
                    Toast.makeText(context, "请求数据失败", Toast.LENGTH_SHORT).show();
                    break;
                case 0x123:
                    picCount++;
                    //更新缓存
                    dataList.get(listIndex).setCount(picCount);
                    t = Integer.toString(picCount);
                    countTV.setText(t);
                    isCount = true;
                    break;
            }
        }
    };

    public PicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("PicFragment","PicFragment");
        View view = inflater.inflate(R.layout.fragment_pic,  container,false);
        picShowIV = view.findViewById(R.id.picShowIV);
        //jsonBT = view.findViewById(R.id.jsonBT);
        lastBT = view.findViewById(R.id.lastPicBT);
        countBT = view.findViewById(R.id.countBT);
        nextBT = view.findViewById(R.id.nextPicBT);
        countIV = view.findViewById(R.id.countIV);
        countTV = view.findViewById(R.id.countTV);
        regLinstener();
        initData();
        return view;
    }

    /**
     * //重置或者初始化数据，每次进入此fragment时调用
     */
    public void initData(){
        page = 0;
        listIndex = 0;
        loadJson(0,5,jsonHost);
    }

    /**
     * 注册监听器
     */
    public void regLinstener(){
//        jsonBT.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //重置数据
//                page = 0;
//                listIndex = 0;
//                loadJson(0,5,jsonHost);
//            }
//        });

        lastBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countIV.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                lastPic();
            }
        });

        countIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //未点击才能点击
                if(!isCount){
                    countIV.setImageResource(R.drawable.ic_favorite_black_24dp);
                    addCount(picID,addCountHost);
                }
            }
        });

        nextBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countIV.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                nextPic();
            }
        });

        //长按保存
        picShowIV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                registerForContextMenu (picShowIV);  //注册上下文菜单的方法
                Log.e("长按保存", "doing");
                return false;
            }
        });

    }

    /**
     * 从picUrl下载图片，通知刷新
     * @param picUrl
     */
    private void downLoad(String picUrl) {
        //创建okHttp
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .build();
        //创建request
        Request request = new Request.Builder()
                .url(downHost + picUrl).build();
        //创建call
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.sendEmptyMessage(0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //将流转换成字节
                byte[] bytes = response.body().bytes();
                //获取message对象
                Message message = mHandler.obtainMessage();
                //将字节存入obj
                message.obj = bytes ;
                //设置一个标识
                message.what = 1;
                //发送
                mHandler.sendMessage(message);
            }
        });
    }

    /**
     * 请求json数据，调用json解析
     * @param startIndex 从startIndex条记录开始
     * @param count 请求count条数据
     * @param url 服务器接口地址
     */
    private void loadJson(int startIndex,int count,String url){

        getPage(pageHost);
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建RequestBody封装参数
        RequestBody builder = new FormBody.Builder()
                .add("startIndex",Integer.toString(startIndex))
                .add("count",Integer.toString(count))
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
                String temp = response.body().string();
                Log.e("请求json","请求完毕");
                parseJson(temp);
            }
        });
    }

    /**
     * 将json字符串解析为List集合，并异步加载第一张图片
     * @param list
     */
    public void parseJson(String list){
        if(dataList!=null)  dataList.clear();

        dataList = JSON.parseArray(list,Androidrecord.class);
        if(dataList.size()==0)  return ;
        //加载图片的相关数据
        picID = dataList.get(listIndex).getId();
        picUrl = dataList.get(listIndex).getName();
        picCount = dataList.get(listIndex).getCount();
        downLoad(picUrl);
    }

    /**
     * 计算分页数量
     * @param url   服务器接口地址
     */
    public void getPage(String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建RequestBody封装参数
        RequestBody  builder = new FormBody.Builder().build();
        //创建request
        Request request = new Request.Builder()
                .url(url)
                .post(builder)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Page","" + e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string().trim();
                allPic = Integer.parseInt(temp);
                allpage = Integer.parseInt(temp)/onePage + 1;
                Log.e("分页数量","" + allpage);
            }
        });
    }

    /**
     * 上一张图片，并自动加载上一批
     */
    public void lastPic(){
        Log.e("page",page+";index"+listIndex);
        if(listIndex>=0)listIndex--;
        if(listIndex>=0){
//            listIndex--;
            picID = dataList.get(listIndex).getId();
            picUrl = dataList.get(listIndex).getName();
            picCount = dataList.get(listIndex).getCount();
            //下载图片，异步加载
            downLoad(picUrl);
            nowPage--;
        }else{
            //加载上一批
            if(page>0){
                page--;
                nowPage--;
                listIndex = 4;
                Log.e("page",page+"\t;Index:" + listIndex);
                loadJson((page)*onePage,onePage,jsonHost);
            }else{
                Log.e("无法加载","已经到最前page:" + page);
                Toast.makeText(context,"已经到最前",Toast.LENGTH_SHORT).show();
            }

        }

    }

    /**
     * 下一张图片，并自动加载下一批
     */
    public void nextPic(){
        //预先加载了第1张 到4，再加1即5
        Log.e("page",page+";index"+listIndex);
        if(listIndex<dataList.size()-1){
            if(listIndex<=-1)   listIndex = 0;
            listIndex++;
            picID = dataList.get(listIndex).getId();
            picUrl = dataList.get(listIndex).getName();
            picCount = dataList.get(listIndex).getCount();
            downLoad(picUrl);
            nowPage++;
        }else{
            //加载下一批
            if(page<allpage-1){
                page++;
                nowPage++;
                listIndex = 0;
                loadJson(page*onePage,onePage,jsonHost);
            }else{
                Log.e("无法加载","已经到最后page:"+page);
                Toast.makeText(context,"已经到最后",Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * 根据图片id点赞，并通知刷新
     * @param id    图片id
     * @param url   服务器接口地址
     */
    public void addCount(int id,String url){
        OkHttpClient okHttpClient = new OkHttpClient();
        //创建RequestBody封装参数
        RequestBody  builder = new FormBody.Builder()
                .add("picId",Integer.toString(id))
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
                Log.e("addCount", temp);
                if("点赞成功".equals(temp)){
                    Message message = mHandler.obtainMessage();
                    message.what = 0x123;
                    mHandler.sendMessage(message);

                }
            }
        });
    }


    //长按弹出菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //在fragment中调用下activity
        getActivity().getMenuInflater().inflate (R.menu.contextmenu,menu);
    }
    //菜单项绑定点击事件

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId ()){
            case R.id.save:
                Log.e("长按图片", "保存");
                saveBitmap(picShowIV);
                break;
            case  R.id.share:
                Log.e("长按图片", "分享");
                shareSingleImage(picShowIV);
                break;
        }
        return super.onOptionsItemSelected (item);
    }

    //分享图片
    public void shareSingleImage(ImageView view) {
        Log.e("分享图片","准备中");
        //获取bitmap
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap();
        view.setDrawingCacheEnabled(false);
        //Bitmap bitmap = ((BitmapDrawable) view.getBackground()).getBitmap();
//        Drawable drawable = view.getDrawable();
//        Bitmap bitmap = getBitmap(drawable);
        //得到uri
        Uri imageUri = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, null, null));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }


    //保存图片1  仅限ImageView
    public void saveBitmap(ImageView view) {
        Drawable drawable = view.getDrawable();
        if (drawable == null) {
            return;
        }
        FileOutputStream outStream = null;
        String local_file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/";
        //创建文件夹
        File picPath = new File(local_file);
        if(!picPath.exists()){
            picPath.mkdirs();
            Toast.makeText(context,"文件夹创建成功",Toast.LENGTH_LONG).show();
        }
        //创建输出流
        File file = new File(picPath, Calendar.getInstance().getTimeInMillis() + ".jpg");
        try {
            outStream = new FileOutputStream(file);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            bitmap.recycle();
            Toast.makeText(context,"图片保存成功",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
