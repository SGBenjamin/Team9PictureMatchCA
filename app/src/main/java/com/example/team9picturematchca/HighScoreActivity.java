package com.example.team9picturematchca;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HighScoreActivity extends AppCompatActivity {

    List<String> arrHighScore = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_score);

        TextView highscore1 = findViewById(R.id.highscore1);
        TextView highscore2 = findViewById(R.id.highscore2);
        TextView highscore3 = findViewById(R.id.highscore3);
        TextView highscore4 = findViewById(R.id.highscore4);
        TextView highscore5 = findViewById(R.id.highscore5);

        TextView[] highscores = {highscore1, highscore2, highscore3, highscore4, highscore5};
        arrHighScore = getHSArray();

        for (int i = 0; i < arrHighScore.size() ; i++) {
            highscores[i].setText(arrHighScore.get(i));
        }

        Button resetBtn = findViewById(R.id.resetButton);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < arrHighScore.size(); i++) {
                    highscores[i].setText("");
                }

                saveArray(new ArrayList<>());
                Toast.makeText(getApplicationContext(), "Scores reset successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        Button returnBtn = findViewById(R.id.returnButton);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        Button solo20Btn = findViewById(R.id.soloMatch20ScoreButton);
        solo20Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HighScoreActivity20.class);
                startActivity(intent);
            }
        });

    }

    public List<String> getHSArray() {
        SharedPreferences sp = this.getSharedPreferences("highScore", Activity.MODE_PRIVATE);
        String highScoreString = sp.getString("highscoreString", "");

        if (highScoreString == "") {
            return new ArrayList<String>();
        }
        else {
            List<String> highscoreList =
                    new ArrayList<String>(Arrays.asList(highScoreString.split(",")));
            return highscoreList;
        }
    }


    public void saveArray(List<String> highscores) {
        String highscoreString = "";

        SharedPreferences sp = this.getSharedPreferences("highScore", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (highscores != null) {
            highscoreString = String.join(",", highscores);
            editor.putString("highScore", highscoreString);
            editor.apply();
        }
    }
}