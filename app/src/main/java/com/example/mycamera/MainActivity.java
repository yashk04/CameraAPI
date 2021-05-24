package com.example.mycamera;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    Camera camera;
    FrameLayout frameLayout;
    ShowCamera showCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = (FrameLayout)findViewById(R.id.frameLayout);

        //open camera

        camera = Camera.open();
        showCamera = new ShowCamera(this, camera);
        frameLayout.addView(showCamera);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage(v);
            }
        });

        frameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                camera.startPreview();
                return true;
            }
        });
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File picture_file  = getOutputMediaFile();
            File pic2=new File(String.valueOf(picture_file));
            Log.d("key", picture_file.getAbsolutePath());
            if(picture_file == null) {
                return;
            } else {
                try {
                    Retrofit retrofit = NetworkClient.getRetrofit();
                    RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), pic2);
                    MultipartBody.Part parts = MultipartBody.Part.createFormData("image=", pic2.getName()+";type=image/jpeg", requestBody);

//                    RequestBody someData = RequestBody.create(MediaType.parse("text/plain"), "This is a new Image");

                    UploadApis uploadApis = retrofit.create(UploadApis.class);
                    Call call = uploadApis.uploadImage(parts);
                    call.enqueue(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            Log.d("Key", response.toString());

                        }

                        @Override
                        public void onFailure(Call call, Throwable t) {
                            Log.d("fail", t.getMessage());
                        }
                    });
//                    String url = "http://max-image-caption-generator.codait-prod-41208c73af8fca213512856c7a09db52-0000.us-east.containers.appdomain.cloud/model/predict";
//                    URL urlObj = new URL(url);
//                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
//                    conn.setRequestMethod("POST");
//                    conn.setDoInput(true);
//                    conn.setDoOutput(true);
//                    conn.setRequestProperty("Connection", "Keep-Alive");
//                    conn.setRequestProperty("Content-Type", "multipart/form-data;");
//                    conn.setReadTimeout(10000);
//                    conn.setConnectTimeout(15000);
//
//                    conn.connect();
//                    DataOutputStream request = new DataOutputStream(conn.getOutputStream());
//                    request.write(data);
//                    InputStream in = new BufferedInputStream(conn.getInputStream());
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//                    StringBuilder result = new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        result.append(line);
//                    }
//
//                    Log.d("test", "result from server: " + result.toString());

//                    AsyncHttpClient client = new AsyncHttpClient();
//                    // Http Request Params Object
//                    RequestParams params = new RequestParams();
//                    String u = "B2mGaME";
//                    String au = "gamewrapperB2M";
//                    // String mob = "880xxxxxxxxxx";
//                    params.put("usr", u.toString());
//                    params.put("aut", au.toString());
//                    params.put("uph", MobileNo.toString());
//                    //  params.put("uph", mob.toString());
//                    client.post("http://196.6.13.01:88/ws/game_wrapper_reg_check.php", params, new AsyncHttpResponseHandler() {
//                        @Override
//                        public void onSuccess(String response) {
//                            playStatus = response;
//                            //////Get your Response/////
//                            Log.i(getClass().getSimpleName(), "Response SP Status. " + playStatus);
//                        }
//                        @Override
//                        public void onFailure(Throwable throwable) {
//                            super.onFailure(throwable);
//                        }
//                    });
//                    CallAPI api = new CallAPI();
//                    api.execute(picture_file.getPath());
                    FileOutputStream fos = new FileOutputStream(picture_file);
                    fos.write(data);
                    fos.close();
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        };

    private File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "GUI");
            if(!folder_gui.exists()) {
                folder_gui.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputFile = new File(folder_gui, "IMG_"+ timeStamp +".jpg");
            return outputFile;
        }
    }

    public void captureImage(View v) {
        if(camera != null) {
            camera.takePicture(null,null,mPictureCallback);
        }
    }

//    public class CallAPI extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... strings) {
//            Bitmap bitmap = BitmapFactory.decodeFile(strings[0]);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object
//            byte[] b = baos.toByteArray();
//            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
//            try {
//                URL url = new URL("http://max-image-caption-generator.codait-prod-41208c73af8fca213512856c7a09db52-0000.us-east.containers.appdomain.cloud/model/predict");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//                conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("Content-Type", "multipart/form-data;");
//                JSONObject jsonParam = new JSONObject();
//                jsonParam.put("image", encodedImage);
//                DataOutputStream request = new DataOutputStream(conn.getOutputStream());
////                Log.d("k1", strings[0]);
//                request.writeBytes(String.valueOf(jsonParam));
//                Log.d("k2", conn.getResponseMessage());
////                InputStream in = new BufferedInputStream(conn.getInputStream());
////                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
////                StringBuilder result = new StringBuilder();
////                String line;
////                while ((line = reader.readLine()) != null) {
////                    result.append(line);
////                }
////
////                Log.d("test", "result from server: " + result.toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }

}