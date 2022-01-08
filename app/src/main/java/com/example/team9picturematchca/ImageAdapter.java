package com.example.team9picturematchca;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<MatchImage> matchImages;

    public ImageAdapter(List<MatchImage> matchImages) {
        this.matchImages = matchImages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView imageView;

        public ViewHolder(View view){
            super(view);

            imageView = (ImageView) view.findViewById(R.id.imageView);
        }
        public ImageView getImageView(){
            return imageView;
        }
    }

    public void CustomAdapter (List<MatchImage> imageSet){//Check with the rest on what is this block of code for
        matchImages = imageSet;
    }
    @NonNull
    @Override
    //This is correct based off https://developer.android.com/guide/topics/ui/layout/recyclerview
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.game_row_item, viewGroup, false);
        return new ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        MatchImage matchImage = matchImages.get(position);
        ImageView imageView = viewHolder.imageView;
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Glide.with(imageView
                    .getContext())
                    .load(matchImage.getImgPath())
                    .into(imageView);
    }
    @Override
    public int getImageCount(){
        return matchImages.length;
    }

}