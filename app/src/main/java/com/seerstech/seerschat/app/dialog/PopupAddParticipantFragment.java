package com.seerstech.seerschat.app.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.databinding.FragmentPopupAddParticipantBinding;
import com.seerstech.seerschat.app.url.APIEndPoint;

public class PopupAddParticipantFragment extends DialogFragment {

    private FragmentPopupAddParticipantBinding mBinding;
    private OnAddParticipantListener mOnAddParticipantListener;
    private ChatClient mChatClient;

    public interface OnAddParticipantListener {
        void onConfirm(String userId, String userNickname);
        void onCancel();
    }

    public static PopupAddParticipantFragment newInstance() {
        PopupAddParticipantFragment fragment = new PopupAddParticipantFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatClient = new ChatClient(APIEndPoint.getInstance().getRestEndPoint(), APIEndPoint.getInstance().getWSEndPoint(), mListener);
        JWTToken token = JWTTokenContainer.getInstance().getToken();
        mChatClient.setJWTToken(token);

        mBinding = FragmentPopupAddParticipantBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = mBinding.tvUserId.getText().toString();
                if(userId.isEmpty()) {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("참여자 추가", "해당 사용자가 존재하지 않습니다.");
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                } else {
                    mChatClient.findUser(userId);
                }
            }
        });

        mBinding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mOnAddParticipantListener != null) {
                    mOnAddParticipantListener.onCancel();
                }
            }
        });
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

    public void setListener(OnAddParticipantListener listener) {
        this.mOnAddParticipantListener = listener;
    }

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {

        }
    };

    ChatClient.OnListener mListener = new ChatClient.OnListener() {
        @Override
        public void onReissueNeeded() {

        }

        @Override
        public void onReissueSuccess(String userId, String userNickname, String userRole) {

        }

        @Override
        public void onReissueFail(String code, String message) {

        }

        @Override
        public void onFindUserSuccess(String userId, String userNickname) {
            dismiss();
            if (mOnAddParticipantListener != null) {
                mOnAddParticipantListener.onConfirm(userId, userNickname);
            }
        }

        @Override
        public void onFindUserFail(String code, String message) {
            PopupMessageFragment popUp = PopupMessageFragment.newInstance("참여자 추가", message);
            popUp.setListener(mMessageListener);
            popUp.show(getChildFragmentManager(), null);
        }
    };
}