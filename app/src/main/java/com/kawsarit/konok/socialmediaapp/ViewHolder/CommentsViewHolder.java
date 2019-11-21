package com.kawsarit.konok.socialmediaapp.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kawsarit.konok.socialmediaapp.Interface.ItemClickListner;
import com.kawsarit.konok.socialmediaapp.R;

public class CommentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView commentUsername, commentTime, commnetDate, commentedText;

    public ItemClickListner listner;        //Access the interface

    public CommentsViewHolder(@NonNull View itemView) {
        super(itemView);

        commentUsername = (TextView) itemView.findViewById(R.id.comment_username);
        commentTime = (TextView) itemView.findViewById(R.id.comment_time);
        commnetDate = (TextView) itemView.findViewById(R.id.comment_date);
        commentedText = (TextView) itemView.findViewById(R.id.comment_text);
    }

    public void setItemClickListner(ItemClickListner listner){      //don't understand properly

        this.listner = listner;
    }

    @Override
    public void onClick(View v) {

        listner.onClick(v, getAdapterPosition(), false);
    }
}
