package com.kawsarit.konok.socialmediaapp.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kawsarit.konok.socialmediaapp.Interface.ItemClickListner;
import com.kawsarit.konok.socialmediaapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public CircleImageView findFriendImage;
    public TextView findFriendName, findFriendStatus;

    public ItemClickListner listner;       //Access the interface

    public FindFriendViewHolder(@NonNull View itemView) {
        super(itemView);

        findFriendImage = (CircleImageView) itemView.findViewById(R.id.find_friends_profile_image);
        findFriendName = (TextView) itemView.findViewById(R.id.find_friends_profile_full_name);
        findFriendStatus = (TextView) itemView.findViewById(R.id.find_friends_status);
    }

    public void setItemClickListner(ItemClickListner listner){      //don't understand properly

        this.listner = listner;
    }

    @Override
    public void onClick(View v) {

        listner.onClick(v, getAdapterPosition(), false);    //don't understand properly
    }
}
