package com.example.WhichPlant2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Bundle extras = getIntent().getExtras();
        String plant_name = extras.getString("plant_name");
        String filepath = extras.getString("filepath");
        File imgFile = new File(filepath);
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        int exifDegree = extras.getInt("exifDegree");
        ((ImageView) findViewById(R.id.image_namu)).setImageBitmap(rotate(bitmap, exifDegree));

        TextView tmp = (TextView) findViewById(R.id.this_plant_is);
        tmp.setText("이 식물은 " + plant_name + "입니다!");

        Button imageButton = (Button) findViewById(R.id.btn_recapture);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}