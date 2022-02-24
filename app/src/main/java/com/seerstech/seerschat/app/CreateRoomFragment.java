package com.seerstech.seerschat.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.vo.ChatUser;
import com.seerstech.seerschat.app.adapter.UserRecyclerViewAdapter;
import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.databinding.FragmentCreateRoomBinding;
import com.seerstech.seerschat.app.dialog.PopupAddParticipantFragment;
import com.seerstech.seerschat.app.dialog.PopupMessageFragment;
import com.seerstech.seerschat.app.url.APIEndPoint;

import java.util.ArrayList;

public class CreateRoomFragment extends Fragment {

    private FragmentCreateRoomBinding mBinding;
    private ChatClient mChatClient;
    private UserRecyclerViewAdapter mAdapter;
    private ArrayList<ChatUser> mUserList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatClient = new ChatClient(APIEndPoint.getInstance().getRestEndPoint(), APIEndPoint.getInstance().getWSEndPoint(), mListener);
        JWTToken token = JWTTokenContainer.getInstance().getToken();
        mChatClient.setJWTToken(token);

        mUserList = new ArrayList<ChatUser>();

        mBinding = FragmentCreateRoomBinding.inflate(inflater, container, false);
        mBinding.userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.doAddParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupAddParticipantFragment popUp = PopupAddParticipantFragment.newInstance();
                popUp.setListener(mOnAddParticipantListener);
                popUp.show(getChildFragmentManager(), null);
            }
        });
        mBinding.doCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String roomName = mBinding.roomName.getText().toString();
                String roomDesc = mBinding.roomDesc.getText().toString();

                if(roomName.isEmpty()) {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방 생성", "방 이름을 입력해 주세요.");
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                    return;
                }

                if(roomDesc.isEmpty()) {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방 생성", "방 설명을 입력해 주세요.");
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                    return;
                }

                ArrayList<String> userIDs = new ArrayList<String>();
                mUserList.forEach(user-> {
                    String userId = user.getUserId();
                    userIDs.add(userId);
                });

                mChatClient.createRoom(roomName, roomDesc, userIDs);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    PopupAddParticipantFragment.OnAddParticipantListener mOnAddParticipantListener = new PopupAddParticipantFragment.OnAddParticipantListener() {
        @Override
        public void onConfirm(String userId, String userNickname) {

            boolean bExist = false;
            for (ChatUser chatUser : mUserList) {
                if(chatUser.getUserId().equals(userId)) {
                    bExist = true;
                    break;
                }
            }

            if(!bExist) {
                ChatUser user = new ChatUser();
                user.setUserId(userId);
                user.setUserNickname(userNickname);
                mUserList.add(user);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter = new UserRecyclerViewAdapter(mUserList);
                        mBinding.userList.setAdapter(mAdapter);
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        PopupMessageFragment popUp = PopupMessageFragment.newInstance("참여자 추가", "이미 추가된 사용자 입니다.");
                        popUp.setListener(mMessageListener);
                        popUp.show(getChildFragmentManager(), null);
                    }
                });
            }
        }

        @Override
        public void onCancel() {}
    };

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {}
    };

    ChatClient.OnChatListener mListener = new ChatClient.OnChatListener() {
        @Override
        public void onReissueSuccess(String userId, String userNickname, String userRole) {

        }

        @Override
        public void onReissueFail(String code, String message) {

        }

        @Override
        public void onCreateRoomSuccess() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NavHostFragment.findNavController(CreateRoomFragment.this).navigate(R.id.action_CreateRoomFragment_to_LobbyFragment);
                }
            });
        }

        @Override
        public void onCreateRoomFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방 생성 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }
    };
}