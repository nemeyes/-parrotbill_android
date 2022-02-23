package com.seerstech.seerschat.app.dialog;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seerstech.seerschat.app.databinding.FragmentPopupMessageBinding;

public class PopupMessageFragment  extends DialogFragment {

    public interface OnMessageListener {
        void onConfirm();
    }

    OnMessageListener mMessageListener;
    FragmentPopupMessageBinding mBinding;
    public static final String TITLE = "title";
    public static final String MESSAGE = "message";

    private String mTitle;
    private String mMessage;
    public static PopupMessageFragment newInstance(String title, String message) {
        PopupMessageFragment fragment = new PopupMessageFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(TITLE);
            mMessage = getArguments().getString(MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentPopupMessageBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mTitle==null) {
            mBinding.tvTitle.setVisibility(View.GONE);
        } else {
            mBinding.tvTitle.setText(mTitle);
        }

        if (mMessage==null) {
            dismiss();
            return;
        }

        mBinding.tvMessage.setText(mMessage);
        mBinding.tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (mMessageListener != null) {
                    mMessageListener.onConfirm();
                }
            }
        });
    }
    public void setListener(PopupMessageFragment.OnMessageListener listener) {
        this.mMessageListener = listener;
    }
}