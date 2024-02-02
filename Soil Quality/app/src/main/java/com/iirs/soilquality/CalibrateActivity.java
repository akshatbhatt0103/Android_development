package com.iirs.soilquality;

import static com.iirs.soilquality.R.*;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CalibrateActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST=100;
    private static final int SELECT_IMAGE_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean calibrated = preferences.getBoolean("calibrated", false);
        if (calibrated) {
            // user has already calibrated, skip this activity and go to the next one
            startActivity(new Intent(this, HomeActivity.class));
            finish(); // finish this activity
            return; // exit the onCreate() method
        }

        setContentView(layout.calibration);
        Button calibrate=findViewById(id.calibrate);
        calibrate.setOnClickListener(view -> {
            Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(open_camera,CAMERA_REQUEST);

        });

        Button upload=findViewById(id.button2);
        upload.setOnClickListener(view -> {
            Intent Galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(Galleryintent,"Select source:"), SELECT_IMAGE_CODE);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == CAMERA_REQUEST){
                Bitmap photo = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
                new CalibrateTask().execute(photo);
            }
            else if (requestCode == SELECT_IMAGE_CODE) {
                Uri selectedImageUri = Objects.requireNonNull(data).getData();
                Bitmap imageBitmap;
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                new CalibrateTask().execute(imageBitmap);
            }
        }
    }

    private class CalibrateTask extends AsyncTask<Bitmap, Void, Void> {
        private ProgressDialog progressDialog;
        private boolean connectionEstablished = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(CalibrateActivity.this, "", "Calibrating your device...");
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            Bitmap photo = bitmaps[0];
            // send the photo to the Flask API using OkHttp or other HTTP client
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] photoBytes = outputStream.toByteArray();

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("photo", "photo.jpg", RequestBody.create(MediaType.parse("image/jpeg"), photoBytes))
                        .build();

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:5000/calibrate")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                outputStream.close();
                if (response.isSuccessful()) {
                    String diff_json = response.body().string();
                    try {
                        String diffJsonString = String.valueOf(new JSONObject(diff_json));
                        // Store the JSON string in SharedPreferences
                        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        preferences.edit().putString("diff_json", diffJsonString).apply();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // save that the user has calibrated
                    SharedPreferences preferences = getPreferences(MODE_PRIVATE);
                    preferences.edit().putBoolean("calibrated", true).apply();
//                    preferences.edit().putString("calibrated_values", response.body().string()).apply();
                    connectionEstablished = true;
                }
                else {
                    // Display a message to the user indicating that the connection with the server failed.
                    runOnUiThread(() -> Toast.makeText(CalibrateActivity.this, "Connection with server not established!", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if(connectionEstablished) {
                startActivity(new Intent(CalibrateActivity.this, HomeActivity.class));
                finish(); // finish this activity
            } else {
                runOnUiThread(() -> Toast.makeText(CalibrateActivity.this, "Connection with server not established!", Toast.LENGTH_SHORT).show());
            }
        }
    }
}
