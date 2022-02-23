package com.seerstech.seerschat.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.seerstech.chat.client.vo.ChatRoom;
import com.seerstech.chat.client.vo.ChatUser;
import com.seerstech.seerschat.app.R;

import java.util.ArrayList;

public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ChatUser> mData = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView name;
        TextView id;
        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.user_layout);
            name = itemView.findViewById(R.id.user_name);
            id = itemView.findViewById(R.id.user_id);
        }
    }

    public UserRecyclerViewAdapter(ArrayList<ChatUser> list) {
        mData = list ;
    }

    @Override
    public UserRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recycler_user_item, parent, false);
        UserRecyclerViewAdapter.ViewHolder vh = new UserRecyclerViewAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(UserRecyclerViewAdapter.ViewHolder holder, int position) {
        ChatUser item = mData.get(position);
        if(position%2==0) {
            holder.layout.setBackgroundColor(Color.parseColor("#FFFFEE"));
        } else {
            holder.layout.setBackgroundColor(Color.parseColor("#13c5bf"));
        }
        holder.name.setText(item.getUserNickname());
        holder.id.setText(item.getUserId());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
