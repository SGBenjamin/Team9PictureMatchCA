package com.example.team9picturematchca;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button multiMatch;
    Button soloMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


       //downloadImgBtn = findViewById(R.id.dlImages);
        //downloadImgBtn.setOnClickListener(this);

        soloMatch = findViewById(R.id.soloMatch);
        soloMatch.setOnClickListener(this);

        multiMatch = findViewById(R.id.multiMatch);
        multiMatch.setOnClickListener(this);



    }



    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);

        if(buttonId == R.id.soloMatch ){
            intent.putExtra("gamemode" , "sole");
        }else if(buttonId == R.id.multiMatch){
            intent.putExtra("gamemode" , "multi");
        }

        startActivity(intent);
    }
}