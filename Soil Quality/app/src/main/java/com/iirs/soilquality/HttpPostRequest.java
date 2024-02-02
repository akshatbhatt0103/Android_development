package com.iirs.soilquality;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.IOException;
import java.lang.ref.WeakReference;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPostRequest extends AsyncTask<Void, Void, String> {
    private final Double diff_L;
    private final Double diff_a;
    private final Double diff_b;
    private ProgressDialog progressDialog;
    private final String url;
    private final String radioButton;
    private final WeakReference<Context> contextRef;
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final byte[] imageBytes;

    public HttpPostRequest(String url, String radioButton, byte[] imageBytes, Double diff_L, Double diff_a, Double diff_b, Context context) {
        this.url = url;
        this.radioButton = radioButton;
        this.imageBytes = imageBytes;
        this.context = context;
        this.diff_L = diff_L;
        this.diff_a = diff_a;
        this.diff_b = diff_b;
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading ..... Please Wait!");
        progressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        Context context = contextRef.get();
        if (context != null){
            try {
                OkHttpClient client = new OkHttpClient();
                // Adding radiobutton value and image file to the request
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("radioButtonValue", radioButton)
                        .addFormDataPart("image", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageBytes))
                        .addFormDataPart("d_L", String.valueOf(diff_L))
                        .addFormDataPart("d_a", String.valueOf(diff_a))
                        .addFormDataPart("d_b", String.valueOf(diff_b))
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()){
                    return response.body().string();
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Context context = contextRef.get();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (context != null){
            if (result != null) {
                // Handle response from server
                new Handler(Looper.getMainLooper()).post(() -> {
                    Intent intent = new Intent(context, OutputActivity.class);
                    intent.putExtra("responseData", result);
                    context.startActivity(intent);
                });
            }
            else {
                // Display message if result is null
                Toast.makeText(context, "Connection with server not established!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
