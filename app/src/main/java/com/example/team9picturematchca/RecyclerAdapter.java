package com.example.team9picturematchca;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    ArrayList<Bitmap> imgs;
    HashMap<Integer, BitmapDrawable> selected;
    final private Context context;
    final private ListItemClickListener onClickListener;

    public RecyclerAdapter(Context context){
        this.context = context;
        imgs = new ArrayList<>();
        selected = new HashMap<>();
        this.onClickListener = (ListItemClickListener) context;
    }

    public void AddImage(String url, Bitmap bitmap){
        imgs.add(bitmap);
        // indicates that any reflection of the data at position is out of date and should be updated
        this.notifyItemChanged(imgs.size() - 1);
    }

    public ArrayList<BitmapDrawable> getSelectedImgs(){
        return new ArrayList<>(selected.values());
    }

    public void clearImages(){
        imgs.clear();
        selected.clear();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.activity_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        ImageView imageView = holder.imageView;
        ImageView checkBox = holder.checkBox;

        imageView.setImageBitmap(imgs.get(position));

        if(selected.containsKey(position)){
            imageView.setBackground(ContextCompat.getDrawable(context, R.drawable.border));
            checkBox.setVisibility(View.VISIBLE);
        } else{
            imageView.setBackground(null);
            checkBox.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return imgs.size(); // no. of rows in Recycler View
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        ImageView checkBox;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            imageView = itemView.findViewById(R.id.imgView);
            checkBox = itemView.findViewById(R.id.checkBox);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if(selected.containsKey(position)){
                selected.remove(position);
            } else{
                selected.put(position, (BitmapDrawable) imageView.getDrawable());
            }
            onClickListener.onListItemClick(position);
        }
    }

}
