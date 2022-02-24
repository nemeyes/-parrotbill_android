package com.seerstech.seerschat.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.vo.ChatRoom;
import com.seerstech.seerschat.app.adapter.RoomRecyclerViewAdapter;
import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.databinding.FragmentLobbyBinding;
import com.seerstech.seerschat.app.dialog.PopupMessageFragment;
import com.seerstech.seerschat.app.url.APIEndPoint;

import java.util.ArrayList;
import java.util.List;

public class LobbyFragment extends Fragment {

    private FragmentLobbyBinding mBinding;
    private ChatClient mChatClient;
    private RoomRecyclerViewAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatClient = new ChatClient(APIEndPoint.getInstance().getRestEndPoint(), APIEndPoint.getInstance().getWSEndPoint(), mListener);
        JWTToken token = JWTTokenContainer.getInstance().getToken();
        mChatClient.setJWTToken(token);
        mBinding = FragmentLobbyBinding.inflate(inflater, container, false);
        mBinding.roomList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.doLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatClient.logout();
            }
        });
        mBinding.doCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(LobbyFragment.this).navigate(R.id.action_LobbyFragment_to_CreateRoomFragment);
            }
        });

        mChatClient.getRoomList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {}
    };

    RoomRecyclerViewAdapter.OnRoomClickListener mItemClickListener = new RoomRecyclerViewAdapter.OnRoomClickListener() {
        @Override
        public void onRoomClick(ChatRoom room) {
            Bundle bundle = new Bundle();
            bundle.putString("room_id", room.getRoomId());
            bundle.putString("room_name", room.getRoomName());
            bundle.putString("room_desc", room.getRoomDescription());
            NavHostFragment.findNavController(LobbyFragment.this).navigate(R.id.action_LobbyFragment_to_RoomFragment, bundle);
        }
    };

    ChatClient.OnChatListener mListener = new ChatClient.OnChatListener() {
        @Override
        public void onLogoutSuccess() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NavHostFragment.findNavController(LobbyFragment.this).navigate(R.id.action_LobbyFragment_to_LoginFragment);
                }
            });
        }

        @Override
        public void onLogoutFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("로그아웃 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        @Override
        public void onReissueSuccess(String userId, String userNickname, String userRole) {

        }

        @Override
        public void onReissueFail(String code, String message) {

        }

        @Override
        public void onGetRoomListSuccess(List<ChatRoom> roomList) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter = new RoomRecyclerViewAdapter((ArrayList<ChatRoom>)roomList);
                    mAdapter.setListener(mItemClickListener);
                    mBinding.roomList.setAdapter(mAdapter);
                }
            });
        }

        @Override
        public void onGetRoomListFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방 목록조회 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }
    };
}