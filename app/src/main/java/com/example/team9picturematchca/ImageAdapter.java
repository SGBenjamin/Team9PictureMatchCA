package com.example.team9picturematchca;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private boolean useGlide;
    private List<MatchImage> matchImages;

    public ImageAdapter(boolean useGlide, List<MatchImage> matchImages) {
        this.useGlide = useGlide;
        this.matchImages = matchImages;
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {//Check with the rest on what is this block of code for
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        listener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }


    @NonNull
    @Override
    //This is correct based off https://developer.android.com/guide/topics/ui/layout/recyclerview
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.activity_image_configuration, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        MatchImage matchImage = matchImages.get(position);
        ImageView imageView = viewHolder.imageView;
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
//        RequestOptions options = new RequestOptions();
//        options.centerCrop();
//
//        if (useGlide) {
                System.out.println("image adapter:" + matchImage.getImgPath());
//               Glide.with(imageView
//                      .getContext())
//                       .load(matchImage.getImgPath())//this is where the URL will be accessed from
//                       .override(125,125)
//                       .centerCrop()
//                       .into(imageView);
//        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(matchImage.getImgPath());
            imageView.setImageBitmap(bitmap);
//        }
    }

    @Override
    public int getItemCount(){
        return matchImages.size();
    }

}