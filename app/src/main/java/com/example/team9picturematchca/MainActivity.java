package com.example.team9picturematchca;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String EXTERNAL_URL = "externalUrl";

    Button multiMatch;
    Button soloMatch;
    Button multiMatch20;
    Button soloMatch20;
    Button highScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soloMatch = findViewById(R.id.soloMatch);
        soloMatch.setOnClickListener(this);

        multiMatch = findViewById(R.id.multiMatch);
        multiMatch.setOnClickListener(this);

        soloMatch20 = findViewById(R.id.soloMatch20);
        soloMatch20.setOnClickListener(this);

        multiMatch20 = findViewById(R.id.multiMatch20);
        multiMatch20.setOnClickListener(this);

        highScores = findViewById(R.id.highscoreBtn);
        highScores.setOnClickListener(this);

        Button abtT9Btn = findViewById(R.id.abtT9Btn);
        abtT9Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String externalUrl =
                        "https://www.iss.nus.edu.sg/graduate-programmes/programme/detail/graduate-diploma-in-systems-analysis";
                launchExternalPage(externalUrl);
            }
        });
    }
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();

        if(buttonId == R.id.soloMatch ){
            Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);
            intent.putExtra("gamemode" , "sole");
            startActivity(intent);
        }else if(buttonId == R.id.multiMatch) {
            Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);
            intent.putExtra("gamemode", "multi");
            startActivity(intent);
        }else if(buttonId == R.id.soloMatch20 ){
                Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);
                intent.putExtra("gamemode" , "sole20");
                startActivity(intent);
        }else if(buttonId == R.id.multiMatch20){
                Intent intent = new Intent(getApplicationContext(), ImagesActivity.class);
                intent.putExtra("gamemode" , "multi20");
                startActivity(intent);
        } else if (buttonId == R.id.highscoreBtn){
            Intent intent = new Intent(getApplicationContext(), HighScoreActivity.class);
            startActivity(intent);
        }
    }

    private void launchExternalPage(String externalUrl) {
        Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
        intent.putExtra(EXTERNAL_URL, externalUrl);
        startActivity(intent);
    }
}