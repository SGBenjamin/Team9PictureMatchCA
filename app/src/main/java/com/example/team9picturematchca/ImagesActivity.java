package com.example.team9picturematchca;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ImagesActivity extends AppCompatActivity implements View.OnClickListener, ListItemClickListener {

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;
    ProgressBar progressBar;
    TextView inputUrl;
    TextView textView;
    TextView selectText;
    Thread thread = null;
    Button startButton;
    Button fetchButton;
    Handler Handler = new Handler();
    String gamemode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        Intent intent = getIntent();
        gamemode = intent.getStringExtra("gamemode");
        System.out.println("gamemode: " + gamemode);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(recyclerAdapter);


        inputUrl = findViewById(R.id.textInputEditText);
        selectText = findViewById(R.id.selectText);
        // indicate info like start load images , download images , save images
        textView = findViewById(R.id.textView);
        findViewById(R.id.scrollText).setSelected(true);

        if(gamemode.equals("sole") || gamemode.equals("multi")){
            selectText.setText("Please select  6 images");
        }else if(gamemode.equals("sole20") ||gamemode.equals("multi20") ){
            selectText.setText("Please select  10 images");
        }


        progressBar = findViewById(R.id.progressBar);

        fetchButton = findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(this);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recyclerAdapter.clearImages();
        progressBar.setVisibility(View.INVISIBLE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        textView.setVisibility(View.INVISIBLE);
        fetchButton.setEnabled(true);
        selectText.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        startButton.setEnabled(false);
    }

    @Override
    public void onListItemClick(int position) {
        View itemView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
        ImageView imageView = itemView.findViewById(R.id.imgView);
        ImageView checkBox = itemView.findViewById(R.id.checkBox);

        if (imageView.getBackground() == null) {
            imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
            checkBox.setVisibility(View.VISIBLE);
        } else {
            imageView.setBackground(null);
            checkBox.setVisibility(View.INVISIBLE);
        }
        if(gamemode.equals("sole")||gamemode.equals("multi")){
            if (recyclerAdapter.getSelectedImgs().size() == 6) {
                startButton.setEnabled(true);
            } else if (startButton.isEnabled()) {
                startButton.setEnabled(false);
            }
        }else if(gamemode.equals("sole20")||gamemode.equals("multi20")){
            if (recyclerAdapter.getSelectedImgs().size() == 10) {
                startButton.setEnabled(true);
            } else if (startButton.isEnabled()) {
                startButton.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        if (buttonId == R.id.fetchButton) {

            hideKeyboard(this);
            if (inputUrl.getText() != null) {
                if (thread != null) {
                    thread.interrupt();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                thread = new Thread(new DownLoadImagesTask(inputUrl.getText().toString()));
                thread.start();

            }
        }else if (buttonId == R.id.startButton) {
            // Start button implementation
            Thread imageDownload = new Thread(new SaveImagesTask(recyclerAdapter.getSelectedImgs()));
            imageDownload.start();

        }


    }

    private void hideKeyboard(ImagesActivity Activity) {
        InputMethodManager imm = (InputMethodManager) Activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = Activity.getCurrentFocus();
        if (view == null) {
            view = new View(Activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private class DownLoadImagesTask implements Runnable {

        private String url;
        private ArrayList<String> images;

        public DownLoadImagesTask(String url) {
            url = checkURL(url);
            this.url = url;
        }

        public String checkURL (String url) {
            if (!url.contains("https")) {
                if (url.contains("http")) {
                    url = url.replace("http", "https");
                } else {
                    url = "https://" + url;
                }
            }

            return url;
        }

        @Override
        public void run() {
            try {
                if (!URLUtil.isValidUrl(url)) {
                    Handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "URL is not valid, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                startUI();

                Document doc = Jsoup.connect(url).get();
                Elements elements = doc.select("img[src~=(?i).(png|jpe?g)]");
                images = (ArrayList<String>) elements.stream()
                        .map(e -> e.attr("src").startsWith("http") ? e.attr("src") : url + e.attr("src"))
                        .collect(Collectors.toList());

                // extract first 20 images
                images.subList(20, images.size()).clear();
                for(String url : images){
                    System.out.println("url : " + url);
                }
                progressBar.setMax(images.size());
                for (int i = 0; i < images.size(); i++) {
                    if (Thread.interrupted()) {
                        concludeUI(false);
                        break;
                    }

                    String sourceAttribute = images.get(i);
                    updateUI(i + 1, sourceAttribute);
                }

                concludeUI(true);


            } catch (IOException ioException) {
                ioException.printStackTrace();
                concludeUI(false);
            }


        }

        private void updateUI(int progress, String url) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bmp;
                        bmp = Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(url)
                                .override(125,125)
                                .centerCrop()
                                .submit()
                                .get();
                        Handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                                textView.setText(String.format(Locale.ENGLISH, "Downloading image %d of %d...", progress, 20));
                                recyclerAdapter.AddImage(url, bmp);
                            }
                        });
                    } catch ( InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }).run();


        }

        private void startUI() {
            Handler.post(new Runnable() {
                @Override
                public void run() {
                    recyclerAdapter.clearImages();
                    progressBar.setProgress(0);
                    progressBar.setVisibility(View.VISIBLE);
                    textView.setText("Starting to load images..");
                    textView.setVisibility(View.VISIBLE);
                    startButton.setVisibility(View.INVISIBLE);
                    selectText.setVisibility(View.INVISIBLE);
                }
            });
        }

        private void concludeUI(boolean success) {
            Handler.post(new Runnable() {
                @Override
                public void run() {
                    if (success) {
                        Toast.makeText(getApplicationContext(), "Images processed!", Toast.LENGTH_SHORT).show();
                    } else {
                        recyclerAdapter.clearImages();
                        Toast.makeText(getApplicationContext(), "Image loading failed!", Toast.LENGTH_SHORT).show();
                    }

                    thread = null;
                    progressBar.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    if (success) {
                        startButton.setVisibility(View.VISIBLE);
                        selectText.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }


    private class SaveImagesTask implements Runnable {
        private ArrayList<BitmapDrawable> bitmaps;

        SaveImagesTask(ArrayList<BitmapDrawable> bitmaps) {
            this.bitmaps = bitmaps;
        }

        @Override
        public void run() {
            startUI();
            delallsavedImages();
            for (int i = 0; i < bitmaps.size(); i++) {
                if (Thread.interrupted()) {
                    concludeUI(false);
                    break;
                }

                updateUI(i + 1);

                if (!save(bitmaps.get(i).getBitmap(), "selectedImage" + (i + 1) + ".jpg")) {
                    Thread.currentThread().interrupt();
                    concludeUI(false);
                    break;
                }
            }

            concludeUI(true);

        }

        public void delallsavedImages(){
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            String[]entries = dir.list();
            if(dir.list().length >0 ){
                for(String s: entries){
                    File currentFile = new File(dir.getPath(),s);
                    System.out.println("del file name : " +  currentFile );
                    currentFile.delete();
                }
            }

        }

        public boolean save(Bitmap image, String filename) {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            System.out.println("save dir :" + Environment.DIRECTORY_PICTURES);
            File file = new File(dir, filename);

            if (file.exists()) {
                file.delete();
                System.out.println("del file name : " +  dir.list().length );
            }


            try {
                FileOutputStream out = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private void startUI() {
            Handler.post(new Runnable() {
                @Override
                public void run() {
                    fetchButton.setEnabled(false);
                    startButton.setVisibility(View.INVISIBLE);
                    progressBar.setProgress(0);
                    progressBar.setMax(bitmaps.size());
                    progressBar.setVisibility(View.VISIBLE);
                    textView.setText("Starting to download images..");
                    textView.setVisibility(View.VISIBLE);
                }
            });
        }

        private void updateUI(int progress) {
            Handler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(progress);
                    textView.setText(String.format(Locale.ENGLISH, "Saving image %d of %d...", progress, bitmaps.size()));
                }
            });

        }

        private void concludeUI(boolean success) {
            Handler.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.INVISIBLE);
                    System.out.println("con gamemode:" + gamemode);
                    if (success ) {
                        if( gamemode.equals("sole")){
                            System.out.println("con gamemode:" + gamemode + "success");
                            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                            startActivity(intent);
                        }else if( gamemode.equals("multi") ){
                            System.out.println("con gamemode:" + gamemode + "success");
                            Intent intent = new Intent(getApplicationContext(), MultiplayerActivity.class);
                            startActivity(intent);
                        }else if( gamemode.equals("sole20")){
                            System.out.println("con gamemode:" + gamemode + "success");
                            Intent intent = new Intent(getApplicationContext(), GameActivity5X4.class);
                            startActivity(intent);
                        }else if( gamemode.equals("multi20") ){
                            System.out.println("con gamemode:" + gamemode + "success");
                            Intent intent = new Intent(getApplicationContext(), MultiplayerActivity5X4.class);
                            startActivity(intent);
                        }
                    }
                    else {
                        startButton.setVisibility(View.VISIBLE);
                        fetchButton.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Downloading of images failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}




