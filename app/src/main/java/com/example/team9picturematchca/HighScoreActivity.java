package com.example.team9picturematchca;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HighScoreActivity extends AppCompatActivity {

    List<String> arrHighScore = new ArrayList<>();
    List<String> arrPlayersNames = new ArrayList<>();

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

/*        TextView player1st = (TextView) findViewById(R.id.player1st);
        TextView player2nd = (TextView) findViewById(R.id.player2nd);
        TextView player3rd = (TextView) findViewById(R.id.player3rd);
        TextView player4th = (TextView) findViewById(R.id.player4th);
        TextView player5th = (TextView) findViewById(R.id.player5th);

        TextView[] players = {player1st, player2nd, player3rd, player4th, player5th};
        arrPlayersNames = getPlayersArray();
        String test = "blank";

        for (int j = 0; j < arrPlayersNames.size(); j++) {
            players[j].setText(test);
        }*/

        Button resetBtn = findViewById(R.id.resetButton);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 5; i++) {
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
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
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

    public List<String> getPlayersArray(){
        SharedPreferences sp = this.getSharedPreferences("playerNames", Activity.MODE_PRIVATE);
        String playerNameString = sp.getString("playerNameString", "");

        if (playerNameString == "") {
            return new ArrayList<String>();
        }
        else {
            List<String> playerNameList =
                    new ArrayList<String>(Arrays.asList(playerNameString.split(",")));
            return playerNameList;
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