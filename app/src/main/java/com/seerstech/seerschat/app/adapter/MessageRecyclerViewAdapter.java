package com.seerstech.seerschat.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.seerstech.chat.client.vo.ChatMessage;
import com.seerstech.chat.client.vo.ChatMessageEnum;
import com.seerstech.chat.client.vo.ChatUser;
import com.seerstech.seerschat.app.R;
import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.dialog.PopupMessageFragment;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private ArrayList<ChatMessage> mData = null;
    private Map<String, ChatUser> mUserList = null;
    private Fragment mFragment = null;
    private Fetch mFetch;

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout remote_layout;
        LinearLayout remote_user_layout;
        TextView remote_user_nickname;
        TextView remote_timestamp;
        TextView remote_message;
        ImageView remote_image;
        LinearLayout local_layout;
        LinearLayout local_user_layout;
        TextView local_user_nickname;
        TextView local_timestamp;
        TextView local_message;
        ImageView local_image;
        ViewHolder(View itemView) {
            super(itemView);
            remote_layout = itemView.findViewById(R.id.remote_layout);
            remote_user_layout = itemView.findViewById(R.id.remote_user_layout);
            remote_user_nickname = itemView.findViewById(R.id.remote_user_nickname);
            remote_timestamp = itemView.findViewById(R.id.remote_timestamp);
            remote_message = itemView.findViewById(R.id.remote_message);
            remote_image = itemView.findViewById(R.id.remote_image);

            local_layout = itemView.findViewById(R.id.local_layout);
            local_user_layout = itemView.findViewById(R.id.local_user_layout);
            local_user_nickname = itemView.findViewById(R.id.local_user_nickname);
            local_timestamp = itemView.findViewById(R.id.local_timestamp);
            local_message = itemView.findViewById(R.id.local_message);
            local_image = itemView.findViewById(R.id.local_image);
        }
    }

    public MessageRecyclerViewAdapter(ArrayList<ChatMessage> list, Map<String, ChatUser> userList, Fragment fragment) {
        mData = list;
        mUserList = userList;
        mFragment = fragment;
    }

    @Override
    public MessageRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recycler_message_item, parent, false);
        MessageRecyclerViewAdapter.ViewHolder vh = new MessageRecyclerViewAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MessageRecyclerViewAdapter.ViewHolder holder, int position) {
        ChatMessage item = mData.get(position);
        if(item.getUserId().equals(JWTTokenContainer.getInstance().getToken().getUserId())) {
            holder.remote_layout.setVisibility(View.GONE);
            holder.local_layout.setVisibility(View.VISIBLE);

            holder.local_user_nickname.setVisibility(View.VISIBLE);
            holder.local_user_nickname.setText(mUserList.get(item.getUserId()).getUserNickname());
            String dateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(item.getCreatedTime()));
            holder.local_timestamp.setText(dateTime);
            holder.local_message.setVisibility(View.VISIBLE);
            holder.local_image.setVisibility(View.GONE);

            if(item.getType()== ChatMessageEnum.MSG_ENTER) {
                holder.local_message.setText(mUserList.get(item.getUserId()).getUserNickname() + "님이 입장하였습니다.");

            } else if(item.getType()==ChatMessageEnum.MSG_QUIT) {
                holder.local_message.setText(mUserList.get(item.getUserId()).getUserNickname() + "님이 퇴장하였습니다.");

            } else if(item.getType()==ChatMessageEnum.MSG_NOTI) {
                holder.local_user_nickname.setVisibility(View.GONE);
                holder.local_message.setText(item.getMessage());

            } else if(item.getType()==ChatMessageEnum.MSG_FILE) {
                if(item.getMimeType().equals("image/gif") || item.getMimeType().equals("image/png") || item.getMimeType().equals("image/jpeg")) {
                    holder.local_message.setVisibility(View.GONE);
                    holder.local_image.setVisibility(View.VISIBLE);
                    Glide.with(mFragment).load(item.getDownloadPath()).centerCrop().placeholder(R.drawable.loading_spinner).into(holder.local_image);
                } else {
                    holder.local_message.setVisibility(View.GONE);
                    holder.local_image.setVisibility(View.VISIBLE);
                    Glide.with(mFragment)
                            .load(R.drawable.download)
                            .override(dpToPx(40, mFragment.getActivity()), dpToPx(40, mFragment.getActivity()))
                            .into(holder.local_image);

                    holder.local_image.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            mFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(mFragment.getActivity())
                                            .setDownloadConcurrentLimit(3)
                                            .build();

                                    mFetch = Fetch.Impl.getInstance(fetchConfiguration);
                                    mFetch.addListener(mFetchListener);

                                    String url = item.getDownloadPath();
                                    String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());

                                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                                    String filePath = path + fileName;

                                    final Request request = new Request(item.getDownloadPath(), filePath);
                                    request.setPriority(Priority.HIGH);
                                    request.setNetworkType(NetworkType.ALL);

                                    mFetch.enqueue(request, updatedRequest -> {
                                        Toast.makeText(mFragment.getActivity(), "파일다운로드 시작", Toast.LENGTH_LONG);
                                    }, error -> {
                                        Toast.makeText(mFragment.getActivity(), "파일다운로드 실패", Toast.LENGTH_LONG);
                                    });
                                }
                            });
                        }
                    });
                }
            } else {
                holder.local_message.setText(item.getMessage());
            }
        } else {

            ChatUser chatUser = mUserList.get(item.getUserId());
            holder.local_layout.setVisibility(View.GONE);
            holder.remote_layout.setVisibility(View.VISIBLE);

            holder.remote_user_nickname.setVisibility(View.VISIBLE);
            holder.remote_user_nickname.setText(chatUser==null?"알수없음":chatUser.getUserNickname());
            String dateTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date (item.getCreatedTime()));
            holder.remote_timestamp.setText(dateTime);
            holder.remote_message.setVisibility(View.VISIBLE);
            holder.remote_image.setVisibility(View.GONE);

            if(item.getType()== ChatMessageEnum.MSG_ENTER) {
                holder.remote_message.setText((chatUser==null?"알수없음":chatUser.getUserNickname()) + "님이 입장하였습니다.");

            } else if(item.getType()==ChatMessageEnum.MSG_QUIT) {
                holder.remote_message.setText((chatUser==null?"알수없음":chatUser.getUserNickname()) + "님이 퇴장하였습니다.");

            } else if(item.getType()==ChatMessageEnum.MSG_NOTI) {
                holder.remote_user_nickname.setVisibility(View.GONE);
                holder.remote_message.setText(item.getMessage());

            } else if(item.getType()==ChatMessageEnum.MSG_FILE) {
                if(item.getMimeType().equals("image/gif") || item.getMimeType().equals("image/png") || item.getMimeType().equals("image/jpeg")) {
                    holder.remote_message.setVisibility(View.GONE);
                    holder.remote_image.setVisibility(View.VISIBLE);
                    Glide.with(mFragment).load(item.getDownloadPath()).centerCrop().placeholder(R.drawable.loading_spinner).into(holder.remote_image);
                } else {
                    holder.remote_message.setVisibility(View.GONE);
                    holder.remote_image.setVisibility(View.VISIBLE);
                    Glide.with(mFragment)
                            .load(R.drawable.download)
                            .override(dpToPx(40, mFragment.getActivity()), dpToPx(40, mFragment.getActivity()))
                            .into(holder.remote_image);

                    holder.remote_image.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            mFragment.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(mFragment.getActivity())
                                            .setDownloadConcurrentLimit(3)
                                            .build();

                                    mFetch = Fetch.Impl.getInstance(fetchConfiguration);
                                    mFetch.addListener(mFetchListener);

                                    String url = item.getDownloadPath();
                                    String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());

                                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                                    String filePath = path + fileName;

                                    final Request request = new Request(item.getDownloadPath(), filePath);
                                    request.setPriority(Priority.HIGH);
                                    request.setNetworkType(NetworkType.ALL);

                                    mFetch.enqueue(request, updatedRequest -> {
                                        Toast.makeText(mFragment.getActivity(), "파일다운로드 시작", Toast.LENGTH_LONG);
                                    }, error -> {
                                        Toast.makeText(mFragment.getActivity(), "파일다운로드 실패", Toast.LENGTH_LONG);
                                    });
                                }
                            });
                        }
                    });
                }
            } else {
                holder.remote_message.setText(item.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    FetchListener mFetchListener = new FetchListener() {

        @Override
        public void onWaitingNetwork(@NonNull Download download) {

        }

        @Override
        public void onStarted(@NonNull Download download, @NonNull List<? extends DownloadBlock> list, int i) {

        }

        @Override
        public void onResumed(@NonNull Download download) {

        }

        @Override
        public void onRemoved(@NonNull Download download) {

        }

        @Override
        public void onQueued(@NonNull Download download, boolean b) {

        }

        @Override
        public void onProgress(@NonNull Download download, long l, long l1) {

        }

        @Override
        public void onPaused(@NonNull Download download) {

        }

        @Override
        public void onError(@NonNull Download download, @NonNull Error error, @Nullable Throwable throwable) {
            PopupMessageFragment popUp = PopupMessageFragment.newInstance("파일다운로드", "파일다운로드에 실패했습니다.");
            popUp.setListener(mMessageListener);
            popUp.show(mFragment.getChildFragmentManager(), null);
        }

        @Override
        public void onDownloadBlockUpdated(@NonNull Download download, @NonNull DownloadBlock downloadBlock, int i) {

        }

        @Override
        public void onDeleted(@NonNull Download download) {

        }

        @Override
        public void onCompleted(@NonNull Download download) {
            PopupMessageFragment popUp = PopupMessageFragment.newInstance("파일다운로드", "파일다운로드에 성공했습니다. 다운로드폴더를 확인하세요");
            popUp.setListener(mMessageListener);
            popUp.show(mFragment.getChildFragmentManager(), null);
        }

        @Override
        public void onCancelled(@NonNull Download download) {

        }

        @Override
        public void onAdded(@NonNull Download download) {

        }
    };

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {}
    };
}
