package com.iirs.soilquality;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

import com.iirs.soilquality.R;

import java.util.Objects;


public class HomeActivity extends AppCompatActivity {
     private static final int SELECT_IMAGE_CODE=1;
     private static final int CAMERA_REQUEST=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button upload=findViewById(R.id.gallery_button);
        upload.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("NOTE");
            builder.setMessage("Please ensure that the picture you take only includes the soil with no other objects or background visible in the frame.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                Intent Galleryintent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(Galleryintent,"Select source:"), SELECT_IMAGE_CODE);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        Button camera = findViewById(R.id.camera_button);
        camera.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setTitle("NOTE");
            builder.setMessage("Please ensure that the picture you take only includes the soil with no other objects or background visible in the frame.");
            builder.setPositiveButton("OK", (dialog, which) -> {
                Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(open_camera,CAMERA_REQUEST);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE_CODE) {
                Uri selectedImageUri = Objects.requireNonNull(data).getData();
                Intent intent = new Intent(this, soiltexture.class);
                intent.putExtra("imageUri", selectedImageUri.toString());
                startActivity(intent);
            } else if (requestCode == CAMERA_REQUEST) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data).getExtras().get("data");
                Uri photoUri = saveImageToGallery(photo);
                Intent intent = new Intent(this, soiltexture.class);
                intent.putExtra("imageUri", photoUri.toString());
                startActivity(intent);
            }
        }
    }
    private Uri saveImageToGallery(Bitmap photo) {
        String savedImageURL = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "Picture", "description");
        return Uri.parse(savedImageURL);
    }
}
