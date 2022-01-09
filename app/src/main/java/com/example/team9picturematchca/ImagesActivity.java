package com.example.team9picturematchca;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

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
import java.net.URL;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapter(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(recyclerAdapter);


        inputUrl = findViewById(R.id.textInputEditText);
        selectText = findViewById(R.id.selectText);
        // indicate info like start load images , download images , save images
        textView = findViewById(R.id.textView);
        findViewById(R.id.scrollText).setSelected(true);

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

        if (recyclerAdapter.getSelectedImgs().size() == 6) {
            startButton.setEnabled(true);
        } else if (startButton.isEnabled()) {
            startButton.setEnabled(false);
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
                url = "https://" + url;
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
                        bmp = Glide.with(getApplicationContext()).asBitmap().load(url).submit().get();
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

        public boolean save(Bitmap image, String filename) {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(dir, filename);

            if (file.exists()) {
                file.delete();
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

                    if (success) {
                        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                        startActivity(intent);
                    } else {
                        startButton.setVisibility(View.VISIBLE);
                        fetchButton.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Downloading of images failed, please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}




