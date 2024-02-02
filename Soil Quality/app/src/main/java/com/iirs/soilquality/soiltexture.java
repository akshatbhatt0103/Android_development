package com.iirs.soilquality;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class soiltexture extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_soiltexture);

        Button submitButton = findViewById(R.id.send_data);
        ImageView imageView = findViewById(R.id.imageshow);
        Intent intent = getIntent();

        // Check whether image has Bitmap or URI of the image
        // Placing in imageview
        if (intent != null) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
            Bitmap imageBitmap;
            if (imageUri != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    imageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // The image data is a Bitmap, so we can use it directly
                imageBitmap = intent.getParcelableExtra("photo");
                imageView.setImageBitmap(imageBitmap);
            }
        }

        //Calling func. to send data
        submitButton.setOnClickListener(v -> {
            RadioGroup radioGroup = findViewById(R.id.radio_group);
            int selectedRadioButtonID = radioGroup.getCheckedRadioButtonId();
            if (selectedRadioButtonID != -1) {
                try {
                    sendRequest();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                Toast.makeText(this, "Please select a Soil Texture!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //func. to send data to flask server
    private void sendRequest() throws IOException {
        //AsyncTask to compress image
        ImageView imageView = findViewById(R.id.imageshow);
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            ImageSaveTask imageSaveTask = new ImageSaveTask(this);
            imageSaveTask.execute(drawable);
        } else {
            Toast.makeText(this, "Drawable is null", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ImageSaveTask extends AsyncTask<Drawable, Void, byte[]> {

        public ImageSaveTask(Context context) {
            WeakReference<Context> contextRef = new WeakReference<>(context);
        }

        @Override
        protected byte[] doInBackground(Drawable... drawables) {
            Drawable drawable = drawables[0];
            byte[] imageBytes = null;
            if (drawable != null) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                imageBytes = outputStream.toByteArray();
                try {

                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return imageBytes;
        }

        protected void onPostExecute(byte[] imageBytes) {
            //Taking radio button value
            RadioGroup radioGroup = findViewById(R.id.radio_group);
            int selectedRadioButtonID = radioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonID);
            String radioButtonValue = selectedRadioButton.getText().toString();

            // Retrieve the JSON string from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String diffJsonString = preferences.getString("diff_json", null);

            // Convert the JSON string to a dictionary
            JSONObject diffJson = null;
            try {
                diffJson = new JSONObject(diffJsonString);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            double diff_L;
            try {
                diff_L = diffJson.getDouble("d_L");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            double diff_a;
            try {
                diff_a = diffJson.getDouble("d_a");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            double diff_b;
            try {
                diff_b = diffJson.getDouble("d_b");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            //Calling httpPostRequest
            HttpPostRequest okHttpPostRequest = new HttpPostRequest("http://10.0.2.2:5000/upload", radioButtonValue, imageBytes, diff_L, diff_a, diff_b, soiltexture.this);
            okHttpPostRequest.execute();
//            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}


