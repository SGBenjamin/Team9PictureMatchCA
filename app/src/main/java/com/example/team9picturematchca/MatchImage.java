package com.example.team9picturematchca;

import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class MatchImage {

    private int imagenum;
    private String imgPath;

    public MatchImage(int imagenum, String imgPath) {
        this.imagenum = imagenum;
        this.imgPath = imgPath;
    }

    public int getImagenum() {
        return imagenum;
    }

    public String getImgPath() {
        return imgPath;
    }

    public static ArrayList<MatchImage> createMatchImgList (Context context){//connected to GameActivity.java
        ArrayList<MatchImage> images = new ArrayList<>();

        File imgDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] imgInDir = imgDir.listFiles();

        int id = 1;

        for (File file : imgInDir){
            String imgPath =file.getAbsolutePath();
            MatchImage matchImage = new MatchImage(id, imgPath);
            images.add(matchImage);
            images.add(matchImage);//need to have 2 because the game required pairs of cards to be matched (Should we check if match 3 can be integrated??)
            id++;
        }

        Collections.shuffle(images);

        return images;
    }
}
