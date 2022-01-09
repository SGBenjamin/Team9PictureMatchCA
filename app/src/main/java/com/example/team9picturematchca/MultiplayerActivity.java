package com.example.team9picturematchca;

import androidx.appcompat.app.AppCompatActivity;
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

public class MultiplayerActivity extends AppCompatActivity
        implements View.OnClickListener {

    GameActivity gameactivity = new GameActivity();

    private int turn = 1;
    private int p1Score = 0, p2Score = 0;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        pauseBtn = findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(this);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);

        pauseForeground = findViewById(R.id.pauseForeground);

        SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);

        mediaPlayers = new ArrayList<>();

        RecyclerView gameRecyclerView = findViewById(R.id.gameRecyclerView);
        cardImages = MatchImage.createMatchImgList(this);
        ImageAdapter adapter = new ImageAdapter( sharedPreferences.getString("glide", "No").equals("Yes"), cardImages);
        adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                tv_p2.setTextColor(Color.GRAY);

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
                    //waitToast();
                    return;
                }

                if (numCardOpened == 0) {
                    // Clicked on first image
                    firstCard = itemView.findViewById(R.id.imageView); // layout game_row_item
                    // Reveal image
                    gameactivity.flipCard(firstCard);
                    firstCardId = cardImages.get(position).getImagenum();
                    numCardOpened = 1;
                } else if (numCardOpened == 1) {
                    // Clicked on second image
                    secondCard = itemView.findViewById(R.id.imageView); // layout game_row_item
                    // Reveal image
                    gameactivity.flipCard(secondCard);
                    secondCardId = cardImages.get(position).getImagenum();
                    processing = true;

                    if (firstCardId == secondCardId) {
                        // Images matched, add points to p1
                        if(turn ==1 ) {
                            p1Score++;
                            gameactivity.playSound(R.raw.matched);
                            tv_p1.setText("P1: " + p1Score);
                        } else if (turn ==2) {
                            p2Score++;
                            gameactivity.playSound(R.raw.matched);
                            tv_p2.setText("P2: "+ p2Score);
                        }

                        if(turn == 1){
                            turn = 2;
                            tv_p1.setTextColor(Color.GRAY);
                            tv_p2.setTextColor(Color.BLACK);
                        } else if (turn == 2) {
                            turn = 1;
                            tv_p2.setTextColor(Color.GRAY);
                            tv_p1.setTextColor(Color.BLACK);
                        }
                    } else{
                        wrongImagePairIsStillOpen = true;
                        gameactivity.didNotMatchText();
                        gameactivity.playSound(R.raw.notmatched);
                        gameactivity.closeBothImagesAfterTwoSeconds();
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
        infoTextView = findViewById(R.id.textInfo);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gameStarted) {
            gameactivity.pauseGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameactivity.releaseMediaPlayers();
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.backBtn){
            finish();
        } else if (id == R.id.pauseBtn) {
            if(gamePaused) {
                gameactivity.resumeGame();
            } else {
                gameactivity.pauseGame();
            }
        }
    }



}

