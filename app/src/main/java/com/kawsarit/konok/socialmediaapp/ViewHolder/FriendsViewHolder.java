package com.kawsarit.konok.socialmediaapp.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kawsarit.konok.socialmediaapp.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView myFriendImage;
    public TextView myFriendName, date;
    public ImageView onlineStatusView;

    public View mView;

    public FriendsViewHolder(@NonNull View itemView) {
        super(itemView);

        myFriendImage = (CircleImageView) itemView.findViewById(R.id.find_friends_profile_image);
        myFriendName = (TextView) itemView.findViewById(R.id.find_friends_profile_full_name);
        date = (TextView) itemView.findViewById(R.id.find_friends_status);
        onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_status_icon);

        mView = itemView;
    }
}
