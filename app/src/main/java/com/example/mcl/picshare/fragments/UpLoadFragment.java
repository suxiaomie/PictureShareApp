package com.example.mcl.picshare.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mcl.picshare.R;
import com.example.mcl.picshare.TestActivity;
import com.example.mcl.picshare.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.example.mcl.picshare.utils.OkHttpUtil.ip;
import static com.example.mcl.picshare.utils.OkHttpUtil.uploadHost;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpLoadFragment extends Fragment {

    private int userId = 0; //用户id
    private String upLoadFilePath = "";
    private String upLoadFileName = "";

    Context context;
    private Button chooseBT;
    private Button uploadBT;
    private ImageView uploadIMG;
    boolean isSelect = false;//未选择不允许上传，上传完成后不允许多次上传

    public void setUserId(int userId) {this.userId = userId; }

    public void setContext(Context context){this.context = context;}

    public UpLoadFragment() {

    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Toast.makeText(context,"上传成功",Toast.LENGTH_SHORT).show();
                    isSelect = false;
                    uploadIMG.setImageBitmap(null);
                    break;
                case 0:
                    Toast.makeText(context,"上传失败",Toast.LENGTH_SHORT).show();
                    isSelect = true;//允许继续上传
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e("UpLoadFragment","UpLoadFragment");
        View view = inflater.inflate(R.layout.fragment_up_load,  container,false);
        chooseBT = view.findViewById(R.id.chooseBT);
        uploadBT = view.findViewById(R.id.uploadBT);
        uploadIMG = view.findViewById(R.id.uploadIMG);
        regLinstener();
        return view;
    }

    /**
     * 注册监听器
     */
    public void regLinstener() {
        chooseBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("chooseBT","click");
                //registerForContextMenu (chooseBT);
                //单击弹出，取消默认长按
                view.showContextMenu();
//                Intent intent = new Intent();
//                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
//                startActivityForResult(intent, 103);
            }
        });
        uploadBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSelect){
                    upLoad(userId, upLoadFilePath, upLoadFileName);
                }else{
                    Toast.makeText(context,"您已上传过文件或者未选择文件",Toast.LENGTH_SHORT).show();
                }

            }
        });
        registerForContextMenu (chooseBT);
    }

    /**
     * 上传文件
     * @param fromId 上传者ID
     * @param filePath 上传图片的路径
     * @param fileName 上传图片的名字
     */
    private void upLoad(int fromId,String filePath,String fileName) {
        //创建文件对象
        File file = new File(filePath);
        //创建RequestBody封装参数
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        //创建MultiparBody,给RequestBody进行设置  顺便封装参数
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", fileName, fileBody)
                .addFormDataPart("fromId",Integer.toString(fromId)) //上传id
                .build();
        //创建Request
        Request request = new Request.Builder()
                .url(uploadHost)
                .post(multipartBody)
                .build();
        //创建okHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(8, TimeUnit.SECONDS)
                .connectTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8,TimeUnit.SECONDS)
                .build();
        //创建call对象
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("上传文件","e=" + e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String temp = response.body().string().trim();
                Log.e("上传文件",temp);
                Message message = mHandler.obtainMessage();
                if("上传成功".equals(temp)) {
                    message.what = 1;
                }
                else message.what = 0;
                mHandler.sendMessage(message);
            }
        });
    }


    /**
     *  返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e("onActivityResult","onActivityResult requestCode:" + requestCode);
        switch (requestCode){
            case 103:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    uploadIMG.setImageURI(uri);
                    if (uri != null) {
                        String path = FileUtil.getPath(context,uri);
                        //getPath(this, uri);
                        Log.e("path",path+"");
                        if (path != null) {
                            File file = new File(path);
                            if (file.exists()) {
                                upLoadFilePath = file.toString();
                                upLoadFileName = file.getName();
                                isSelect = true;
                                Log.e("upLoadFileName",upLoadFileName);
                                Log.e("upLoadFilePath",upLoadFilePath);
                            }
                        }
                    }
                }
                break;
            case 102:           //处理相机返回
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    uploadIMG.setImageBitmap(bitmap);

                    String local_file = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/";
                    File picPath = new File(local_file);
                    if(!picPath.exists()){ picPath.mkdirs(); }
                    File file = new File(picPath, Calendar.getInstance().getTimeInMillis() + ".jpg");

                    //save
                    FileOutputStream fOut = null;
                    try {
                        file.createNewFile();
                        fOut = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                        fOut.flush();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file.exists()) {
                        upLoadFilePath = file.toString();
                        upLoadFileName = file.getName();
                        isSelect = true;
                        Log.e("相机upLoadFileName",upLoadFileName);
                        Log.e("相机upLoadFilePath",upLoadFilePath);
                    }

                }
                break;
        }


    }

    //长按弹出菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //在fragment中调用下activity
        getActivity().getMenuInflater().inflate (R.menu.choose_pic_menu,menu);
    }
    //菜单项绑定点击事件

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId ()){
            case R.id.local:
                Log.e("选择图片", "本地");
                /*
                Intent intent = new Intent();
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 103);
                */
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent,103);

                break;
            case  R.id.camera:
                Log.e("选择图片", "相机");
                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent1, 102);
                break;
        }
        return super.onOptionsItemSelected (item);

    }
}
