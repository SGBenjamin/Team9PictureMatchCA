package com.example.team9picturematchca;

import android.content.Context;
import android.os.Environment;

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
        System.out.println("match dir :" + Environment.DIRECTORY_PICTURES);

        File[] imgInDir = imgDir.listFiles();

        int id = 1;

        for (File file : imgInDir){
            System.out.println("match pic path: " + file);
            String imgPath =file.getAbsolutePath();
            MatchImage matchImage = new MatchImage(id, imgPath);
            images.add(matchImage);
            images.add(matchImage);
            id++;
        }

        Collections.shuffle(images);

        return images;
    }
}
