package com.example.team9picturematchca;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button downloadImgBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        downloadImgBtn = findViewById(R.id.dlImages);
        downloadImgBtn.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        if(buttonId == R.id.dlImages){
            Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);
            startActivity(intent);
        }
    }
}