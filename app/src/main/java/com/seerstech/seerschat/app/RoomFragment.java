package com.seerstech.seerschat.app;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.vo.ChatMessage;
import com.seerstech.chat.client.vo.ChatUser;
import com.seerstech.seerschat.app.adapter.MessageRecyclerViewAdapter;
import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.databinding.FragmentRoomBinding;
import com.seerstech.seerschat.app.dialog.PopupAddParticipantFragment;
import com.seerstech.seerschat.app.dialog.PopupMessageFragment;
import com.seerstech.seerschat.app.dialog.PopupRoomParticipantFragment;
import com.seerstech.seerschat.app.url.APIEndPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomFragment extends Fragment {

    private FragmentRoomBinding mBinding;
    private ChatClient mChatClient;
    private String mRoomId;
    private String mRoomName;
    private String mRoomDesc;
    private MessageRecyclerViewAdapter mAdapter;

    private Object mUserListLock;
    private HashMap<String, ChatUser> mUserList;
    private ArrayList<ChatMessage> mMessageList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomId = getArguments().getString("room_id");
            mRoomName = getArguments().getString("room_name");
            mRoomDesc = getArguments().getString("room_desc");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatClient = new ChatClient(APIEndPoint.getInstance().getRestEndPoint(), APIEndPoint.getInstance().getWSEndPoint(), mListener);
        JWTToken token = JWTTokenContainer.getInstance().getToken();
        mChatClient.setJWTToken(token);
        mChatClient.beginChatMessage(mRoomId);

        mBinding = FragmentRoomBinding.inflate(inflater, container, false);
        mBinding.roomName.setText(mRoomName);
        mBinding.roomDesc.setText(mRoomDesc);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        mBinding.messageList.setLayoutManager(layoutManager);
        return mBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserListLock = new Object();
        mUserList = new HashMap<String, ChatUser>();

        mBinding.doAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupAddParticipantFragment popUp = PopupAddParticipantFragment.newInstance();
                popUp.setListener(mOnAddParticipantListener);
                popUp.show(getChildFragmentManager(), null);
            }
        });
        mBinding.doShowUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupRoomParticipantFragment popUp = PopupRoomParticipantFragment.newInstance(mRoomId);
                popUp.setListener(mOnRoomParticipantListener);
                popUp.show(getChildFragmentManager(), null);
            }
        });
        mBinding.doSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mBinding.sendMessage.getText().toString();
                if(message.isEmpty()) {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("메시지 전송", "전달할 메시지를 입력해 주세요");
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                } else {
                    mChatClient.sendMessage(mRoomId, message);
                    mBinding.sendMessage.setText("");
                }
            }
        });
        mBinding.doSendFile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new ChooserDialog(getActivity())
                        //.withStartFile(path)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                //Toast.makeText(getActivity(), "FILE: " + path, Toast.LENGTH_SHORT).show();
                                mChatClient.uploadFile(path, mRoomId, JWTTokenContainer.getInstance().getToken().getUserId());
                            }
                        })
                        // to handle the back key pressed or clicked outside the dialog:
                        .withOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                //Log.d("CANCEL", "CANCEL");
                                dialog.cancel(); // MUST have
                            }
                        })
                        .build()
                        .show();
            }
        });

        mChatClient.getRoomUserList(mRoomId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mChatClient.endChatMessage();
        mBinding = null;
    }

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {}
    };

    PopupAddParticipantFragment.OnAddParticipantListener mOnAddParticipantListener = new PopupAddParticipantFragment.OnAddParticipantListener() {
        @Override
        public void onConfirm(String userId, String userNickname) {

            boolean bExist = false;
            for(Map.Entry<String, ChatUser> entry : mUserList.entrySet()) {
                String key = entry.getKey();
                if (key.equals(userId)) {
                    bExist = true;
                    break;
                }
            }

            if(!bExist) {
                mChatClient.inviteUser(mRoomId, userId);
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

    PopupRoomParticipantFragment.OnRoomParticipantListener mOnRoomParticipantListener = new PopupRoomParticipantFragment.OnRoomParticipantListener() {

        @Override
        public void onLeave() {
            mChatClient.leaveRoom(mRoomId);
        }

        @Override
        public void onCancel() {

        }
    };

    ChatClient.OnListener mListener = new ChatClient.OnListener() {
        public void onReissueNeeded() {

        }

        public void onReissueSuccess(String userId, String userNickname, String userRole) {

        }

        public void onReissueFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("토큰발행 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        public void onGetRoomUserListSuccess(String roomId, List<ChatUser> userList) {
            synchronized (mUserListLock) {
                userList.forEach(user -> {
                    mUserList.put(user.getUserId(), user);
                });
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChatClient.getRoomMessageList(mRoomId);
                }
            });
        }

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

        public void onLeaveRoomSuccess() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NavHostFragment.findNavController(RoomFragment.this).navigate(R.id.action_RoomFragment_to_LobbyFragment);
                }
            });
        }

        public void onLeaveRoomFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방탈퇴 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        public void onInviteUserSuccess() {
            mChatClient.getRoomUserList(mRoomId);
        }

        public void onInviteUserFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방참여자 추가 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        public void onGetRoomMessageListSuccess(String roomId, List<ChatMessage> messageList) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(messageList!=null) {
                        mMessageList = (ArrayList<ChatMessage>) messageList;
                    } else {
                        mMessageList = new ArrayList<ChatMessage>();
                    }
                    mAdapter = new MessageRecyclerViewAdapter((ArrayList<ChatMessage>)mMessageList, mUserList, RoomFragment.this);
                    mBinding.messageList.setAdapter(mAdapter);
                }
            });
        }

        public void onGetRoomMessageListFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("방메시지 목록조회 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        public void onMessageReceive(ChatMessage chatMessage) {
            mMessageList.add(chatMessage);
            mAdapter.notifyDataSetChanged();
            mBinding.messageList.scrollToPosition(mAdapter.getItemCount() - 1);
        }

        public void onUploadFileSuccess(String fileName, String fileDownloadUrl, String fileType, Long fileSize) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("파일전송", "파일 젅송이 성공했습니다.");
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        public void onUploadFileFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("파일전송 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }

        /*
        public void onDownloadFileSuccess(File file) {

        }

        public void onDownloadFileFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("파일다운로드 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }
        */
    };
}