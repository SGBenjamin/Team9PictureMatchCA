package com.example.team9picturematchca;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<MatchImage> cardImages;

    private int numCardOpened;
    private ImageView firstCard;
    private ImageView secondCard;
    private int firstCardId, secondCardId;
    private int score;
    private int maxScore;

    private boolean gameStarted;
    private boolean wrongImagePairIsStillOpen;
    private boolean flipping;
    private boolean processing;
    private boolean timerIsRunning;
    private boolean gamePaused;
    private int timer;

    private Button pauseBtn;
    private TextView infoTextView;
    private TextView pauseForeground;
    //private String infoText;

    private List<String> strHighscores = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private ArrayList<MediaPlayer> mediaPlayers;

    //new by YJ
//    private int turn = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        strHighscores = getArray();

        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);

        pauseBtn = findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(this);

        pauseForeground = findViewById(R.id.pauseForeground);

        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        mediaPlayers = new ArrayList<>();

        RecyclerView gameRecyclerView = findViewById(R.id.gameRecyclerView);
        cardImages = MatchImage.createMatchImgList(this);
        ImageAdapter adapter = new ImageAdapter( sharedPreferences.getString("glide", "No").equals("Yes"), cardImages);
        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                // Start timer on first click
                if (!gameStarted) {
                    timerIsRunning = true;
                    gamePaused = false;
                    gameStarted = true;
                    pauseBtn.setVisibility(View.VISIBLE);
                    startTimer();
                }

                if (gamePaused || flipping || processing ||
                        itemView.findViewById(R.id.imageView).getForeground() == null) { // layout game_row_item
                    return;
                }

                if (wrongImagePairIsStillOpen) {
                    waitToast();
                    return;
                }

                if (numCardOpened == 0) {
                    // Clicked on first image
                    firstCard = itemView.findViewById(R.id.imageView); // layout game_row_item
                    // Reveal image
                    flipCard(firstCard);
                    firstCardId = cardImages.get(position).getImagenum();
                    numCardOpened = 1;
                } else if (numCardOpened == 1) {
                    // Clicked on second image
                    secondCard = itemView.findViewById(R.id.imageView); // layout game_row_item
                    // Reveal image
                    flipCard(secondCard);
                    secondCardId = cardImages.get(position).getImagenum();
                    processing = true;
                    if (firstCardId == secondCardId) {
                        // Images matched
                        updateScore();

                        if (score == maxScore) {
                            // Game ended
                            stopTimer();
                            pauseBtn.setEnabled(false);
                            //Save high scores
                            if (strHighscores.size() < 5 || timer < convertTime(strHighscores.get(4))) {
                                // Sound effect for highscore
                                // playSound(R.raw.game_highscore);
                                highScoreText();
                                String score = convertTime(timer);
                                strHighscores.add(score);
                                saveArray(strHighscores);
                            } else {
                                // Sound effect for winning
                                // playSound(R.raw.win_audio);
                                winGameText();
                            }

                            returnToMainActivityAfterFourSeconds();
                        } else {
                            // Game not yet end
                            matchedText();
                            // Sound effect for matching
                            // playSound(R.raw.success_bell2);
                        }
                    } else {
                        // Images did not match
                        wrongImagePairIsStillOpen = true;
                        didNotMatchText();
                        // Sound effect for wrong match
                        // playSound(R.raw.failure_beep);
                        closeBothImagesAfterTwoSeconds();
                    }
                    processing = false;
                    numCardOpened = 0;
                }
            }
        });
        gameRecyclerView.setAdapter(adapter);
        int GRID_COLUMNS = 3;
        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, GRID_COLUMNS));

        numCardOpened = 0;
        score = 0;
        maxScore = cardImages.size() / 2;
        timerIsRunning = false;
        timer = 0;
        infoTextView = findViewById(R.id.textInfo);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameStarted) {
            pauseGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayers();
    }

    public void playSound(int soundId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), soundId);
            mediaPlayers.add(mediaPlayer);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                    mediaPlayer = null;
                }
            });
            mediaPlayer.start();
        } else {
            MediaPlayer extraMediaPlayer = MediaPlayer.create(getApplicationContext(), soundId);
            mediaPlayers.add(extraMediaPlayer);
            extraMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayers.remove(mp);
                    mp.release();
                }
            });
            extraMediaPlayer.start();
        }
    }

    public void releaseMediaPlayers() {
        for (MediaPlayer mp : mediaPlayers) {
            mp.reset();
            mp.release();
        }
    }

    public void flipCard(View v) {
        flipping = true;
        if (v.getForeground() != null) {
            v.animate().withLayer().rotationY(90).setDuration(300).withEndAction(
                    new Runnable() {
                        @Override public void run() {
                            // second quarter turn
                            v.setForeground(null);
                            v.setRotationY(-90);
                            v.animate().withLayer().rotationY(0).setDuration(300).start();
                            flipping = false;
                        }
                    }
            ).start();
        } else {
            v.animate().withLayer().rotationY(-90).setDuration(300).withEndAction(
                    new Runnable() {
                        @Override public void run() {
                            // second quarter turn
                            v.setForeground(new ColorDrawable(
                                    ContextCompat.getColor(GameActivity.this, R.color.teal_200)));
                            v.setRotationY(90);
                            v.animate().withLayer().rotationY(0).setDuration(300).start();
                            flipping = false;
                        }
                    }
            ).start();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.backBtn) {
            finish();
        } else if (id == R.id.pauseBtn) {
            if (gamePaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
    }

    public void pauseGame() {
        gamePaused = true;
        // playSound(R.raw.game_pause);
        pauseForeground.setVisibility(View.VISIBLE);
        pauseBtn.setText("Resume");
        stopTimer();
    }

    public void resumeGame() {
        gamePaused = false;
        timerIsRunning = true;
        // playSound(R.raw.game_resume);
        pauseForeground.setVisibility(View.INVISIBLE);
        pauseBtn.setText("Pause");
        startTimer();
    }

    private void closeBothImagesAfterTwoSeconds() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flipCard(firstCard);
                flipCard(secondCard);

                wrongImagePairIsStillOpen = false;
                selectImageText();
            }
        }, 2000);
    }

    private void updateScore() {
        score++;
        String textScore = score + "/6 matched";
        TextView textMatches = findViewById(R.id.textMatches);
        textMatches.setText(textScore);
    }

    //Timer
    private void startTimer() {
        final TextView timerTextView = findViewById(R.id.textTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes = (timer % 3600) / 60;
                int seconds = timer % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
                timerTextView.setText(time);
                if (timerIsRunning) {
                    timer++;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void stopTimer() {
        timerIsRunning = false;
    }

    private void didNotMatchText() {
        infoTextView.setText(R.string.no_match_text);
    }

    private void selectImageText() {
        infoTextView.setText(R.string.select_cards_text);
    }

    private void matchedText() {
        infoTextView.setText(R.string.cards_matched_text);
    }

    private void winGameText() {
        infoTextView.setText(R.string.winGame_text);
    }

    private void highScoreText() {
        infoTextView.setText(R.string.highScore_text);
    }

    private void waitToast() {
        Toast.makeText(this, "Please wait for wrong image pair to close.",
                Toast.LENGTH_SHORT).show();
    }

    private void returnToMainActivityAfterFourSeconds() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 4000);
    }

    public void saveArray(List<String> highscoreList) {
        String highscoreString;
        SharedPreferences sp = this.getSharedPreferences("highScore", Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        if (highscoreList != null) {
            highscoreList.sort(Comparator.comparingInt(this::convertTime));
            if (highscoreList.size() > 5) {
                highscoreList.subList(5, highscoreList.size()).clear();
            }
            highscoreString = String.join(",", highscoreList);
            mEdit1.putString("highscoreString", highscoreString);
            mEdit1.apply();
        }
    }

    public List<String> getArray(){
        SharedPreferences sp = this.getSharedPreferences("highScore", Activity.MODE_PRIVATE);
        String highscoreString = sp.getString("highscoreString","");
        if (highscoreString.equals("")){
            return new ArrayList<>();
        }
        else {
            ArrayList<String> highScores = new ArrayList<>(Arrays.asList(highscoreString.split(",")));
            highScores.sort(Comparator.comparingInt(this::convertTime));
            return highScores;
        }
    }

    public String convertTime(Integer intTime){
        int hours = intTime / 3600;
        int minutes = (intTime % 3600) / 60;
        int seconds = intTime % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                hours, minutes, seconds);
    }

    public int convertTime(String strTime) {
        String[] timeUnits = strTime.split(":");
        int hours = Integer.parseInt(timeUnits[0]) * 60 * 60;
        int minutes = Integer.parseInt(timeUnits[1]) * 60;
        int seconds = Integer.parseInt(timeUnits[2]);
        return hours + minutes + seconds;
    }
}