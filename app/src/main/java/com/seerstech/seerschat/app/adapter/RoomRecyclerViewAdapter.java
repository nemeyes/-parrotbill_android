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
import com.seerstech.seerschat.app.R;

import java.util.ArrayList;

public class RoomRecyclerViewAdapter extends RecyclerView.Adapter<RoomRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ChatRoom> mData = null;
    private OnRoomClickListener mRoomClickListener;

    public interface OnRoomClickListener {
        void onRoomClick(ChatRoom room);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        ImageView icon;
        TextView name;
        TextView desc;
        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.room_layout);
            icon = itemView.findViewById(R.id.room_icon);
            name = itemView.findViewById(R.id.room_name);
            desc = itemView.findViewById(R.id.room_desc);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(mRoomClickListener !=null && RecyclerView.NO_POSITION!=pos && mData!=null) {
                        ChatRoom chatRoom = mData.get(pos);
                        mRoomClickListener.onRoomClick(chatRoom);
                    }
                }
            });
        }


    }

    public RoomRecyclerViewAdapter(ArrayList<ChatRoom> list) {
        mData = list ;
    }

    @Override
    public RoomRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recycler_room_item, parent, false);
        RoomRecyclerViewAdapter.ViewHolder vh = new RoomRecyclerViewAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(RoomRecyclerViewAdapter.ViewHolder holder, int position) {
        ChatRoom item = mData.get(position);
        if(position%2==0) {
            holder.layout.setBackgroundColor(Color.parseColor("#FFFFEE"));
        } else {
            holder.layout.setBackgroundColor(Color.parseColor("#13c5bf"));
        }
        holder.icon.setImageResource(R.drawable.room);
        holder.name.setText(item.getRoomName());
        holder.desc.setText(item.getRoomDescription());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setListener(OnRoomClickListener listener) {
        mRoomClickListener = listener;
    }
}
