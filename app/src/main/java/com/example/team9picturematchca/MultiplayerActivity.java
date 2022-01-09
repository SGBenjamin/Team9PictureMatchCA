package com.example.team9picturematchca;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class MultiplayerActivity extends AppCompatActivity
        implements View.OnClickListener {

//    GameActivity gameactivity = new GameActivity();
    private int timer;
    private int turn = 1;
    private int p1Score = 0, p2Score = 0;
    private boolean timerIsRunning;
    private int numCardOpened;
    private ImageView firstCard, secondCard;
    private int firstCardId, secondCardId;
    private boolean gamePaused, gameStarted;
    private boolean flipping, processing, wrongImagePairIsStillOpen;
    private Button pauseBtn, backBtn;
    private TextView infoTextView;
    private TextView pauseForeground;
    private TextView tv_p1, tv_p2;
    private MediaPlayer mediaPlayer;
    private ArrayList<MediaPlayer> mediaPlayers;
    private ArrayList<MatchImage> cardImages;
    private int maxScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        pauseBtn = findViewById(R.id.multipauseBtn);
        pauseBtn.setOnClickListener(this);

        backBtn = findViewById(R.id.multibackBtn);
        backBtn.setOnClickListener(this);

        tv_p1 = findViewById(R.id.tv_p1);
        tv_p2 = findViewById(R.id.tv_p2);

        pauseForeground = findViewById(R.id.multipauseForeground);

        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        mediaPlayers = new ArrayList<>();

        RecyclerView gameRecyclerView = findViewById(R.id.gameRecyclerView);
        cardImages = MatchImage.createMatchImgList(this);
        ImageAdapter adapter = new ImageAdapter( sharedPreferences.getString("glide", "No").equals("Yes"), cardImages);
        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                tv_p2 = findViewById(R.id.tv_p2);
                tv_p1 = findViewById(R.id.tv_p1);
                // tv_p2.setTextColor(Color.GRAY);
                setPlayerColour(tv_p1, tv_p2, turn);
                infoTextView = findViewById(R.id.multitextInfo);
                if (!gameStarted) {
                    gamePaused = false;
                    gameStarted = true;
                    pauseBtn.setVisibility(View.VISIBLE);
                }

                if (gamePaused || flipping || processing ||
                        itemView.findViewById(R.id.imageView)
                                .getForeground() == null) { // layout game_row_item
                    return;
                }
                if (wrongImagePairIsStillOpen) {
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
                        // Images matched, add points to p1
                        if(turn ==1 ) {
                            p1Score++;
                            playSound(R.raw.matched);
                            tv_p1.setText("P1: " + p1Score);
                        } else if (turn ==2) {
                            p2Score++;
                            playSound(R.raw.matched);
                            tv_p2.setText("P2: "+ p2Score);
                        }

                        int score = p1Score + p2Score;
                        maxScore = cardImages.size()/ 2;
                        if (score == maxScore) {
                            pauseBtn.setEnabled(false);
                            winGameText();
                        }

                    } else{
                        if (turn == 1) {
                            turn = 2;
                            setPlayerColour(tv_p1, tv_p2, turn);
                        } else if (turn == 2) {
                            turn = 1;
                            setPlayerColour(tv_p1, tv_p2, turn);
                        }
                        wrongImagePairIsStillOpen = true;
                        didNotMatchText();
                        playSound(R.raw.notmatched);
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
        p1Score = 0;
        p2Score = 0;

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



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.multibackBtn){
            finish();
        } else if (id == R.id.multipauseBtn) {
            if(gamePaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        }
    }

    protected void didNotMatchText() {
        infoTextView.setText(R.string.no_match_text);
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
                                    ContextCompat.getColor(MultiplayerActivity.this, R.color.teal_200)));
                            v.setRotationY(90);
                            v.animate().withLayer().rotationY(0).setDuration(300).start();
                            flipping = false;
                        }
                    }
            ).start();
        }
    }

    public void pauseGame() {
        gamePaused = true;
        // playSound(R.raw.game_pause);
        pauseForeground.setVisibility(View.VISIBLE);
        pauseBtn.setText("Resume");
        stopTimer();
    }
    protected void stopTimer() {
        timerIsRunning = false;
    }
    protected void closeBothImagesAfterTwoSeconds() {
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
    protected void selectImageText() {
        infoTextView.setText(R.string.select_cards_text);
    }

    public void releaseMediaPlayers() {
        for (MediaPlayer mp : mediaPlayers) {
            mp.reset();
            mp.release();
        }
    }

    public void resumeGame() {
        gamePaused = false;
        // timerIsRunning = true;
        // playSound(R.raw.game_resume);
        pauseForeground.setVisibility(View.INVISIBLE);
        pauseBtn.setText("Pause");
        // startTimer();
    }

   /* private void startTimer() {
        final TextView timerTextView = findViewById(R.id.textTimer);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes = (timer % 3600) / 60;
                int seconds = timer % 60;
                String time = String.format(Locale.getDefault(), "Time: %02d:%02d", minutes, seconds);
                timerTextView.setText(time);
                if (timerIsRunning) {
                    timer++;
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }*/

    private void setPlayerColour (TextView tv_p1, TextView tv_p2, int turn) {
        if(turn == 2){
            tv_p1.setTextColor(Color.GRAY);
            tv_p2.setTextColor(Color.BLACK);
        } else if (turn == 1) {
            tv_p2.setTextColor(Color.GRAY);
            tv_p1.setTextColor(Color.BLACK);
        }
    }

    private void winGameText() {
        String winner = "";
        String msg = "";
        if (p1Score > p2Score) {
            winner = "P1 wins!";
            msg = getString(R.string.p1winMsg);
        } else if (p2Score > p1Score) {
            winner = "P2 wins!";
            msg = getString(R.string.p2winMsg);
        } else if (p1Score == p2Score) {
            winner = "Its a draw!";
            msg = getString(R.string.drawMsg);
        }

        //infoTextView.setText(R.string.winGame_text);
        AlertDialog.Builder dlg = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.gameover) + " " + winner)
                .setMessage(msg)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(getBaseContext(), MultiplayerActivity.class));
                            }
                        })
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                            }
                        })
                .setIcon(android.R.drawable.ic_popup_reminder);
        dlg.show();
    }

}

