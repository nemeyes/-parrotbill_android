package com.seerstech.seerschat.app.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.vo.ChatUser;
import com.seerstech.seerschat.app.adapter.UserRecyclerViewAdapter;
import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.databinding.FragmentPopupRoomParticipantBinding;
import com.seerstech.seerschat.app.url.APIEndPoint;

import java.util.ArrayList;
import java.util.List;

public class PopupRoomParticipantFragment extends DialogFragment {

    private FragmentPopupRoomParticipantBinding mBinding;
    private ChatClient mChatClient;
    private String mRoomId;
    private UserRecyclerViewAdapter mAdapter;
    private ArrayList<ChatUser> mUserList;
    private OnRoomParticipantListener mRoomParticipantListener;

    public interface OnRoomParticipantListener {
        void onLeave();
        void onCancel();
    }

    public static PopupRoomParticipantFragment newInstance(String roomId) {
        PopupRoomParticipantFragment fragment = new PopupRoomParticipantFragment();
        Bundle bundle = new Bundle();
        bundle.putString("room_id", roomId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString("room_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatClient = new ChatClient(APIEndPoint.getInstance().getRestEndPoint(), APIEndPoint.getInstance().getWSEndPoint(), mListener);
        JWTToken token = JWTTokenContainer.getInstance().getToken();
        mChatClient.setJWTToken(token);

        mBinding = FragmentPopupRoomParticipantBinding.inflate(inflater, container, false);
        mBinding.userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.doLeaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mRoomParticipantListener.onLeave();
            }
        });

        mBinding.doCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                mRoomParticipantListener.onCancel();
            }
        });

        mChatClient.getRoomUserList(mRoomId);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {}
    };

    ChatClient.OnListener mListener = new ChatClient.OnListener() {
        @Override
        public void onGetRoomUserListSuccess(String roomId, List<ChatUser> userList) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUserList = (ArrayList<ChatUser>)userList;
                    mAdapter = new UserRecyclerViewAdapter(mUserList);
                    mBinding.userList.setAdapter(mAdapter);
                }
            });
        }

        @Override
        public void onGetRoomUserListFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방참여자 목록조회 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }
    };

    public void setListener(OnRoomParticipantListener listener) {
        mRoomParticipantListener = listener;
    }
}