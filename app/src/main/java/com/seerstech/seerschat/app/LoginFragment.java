package com.seerstech.seerschat.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.seerstech.seerschat.app.auth.JWTTokenContainer;
import com.seerstech.seerschat.app.databinding.FragmentLoginBinding;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.seerschat.app.dialog.PopupMessageFragment;
import com.seerstech.seerschat.app.url.APIEndPoint;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private ChatClient chatClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatClient = new ChatClient(APIEndPoint.getInstance().getRestEndPoint(), APIEndPoint.getInstance().getWSEndPoint(), mListener);
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.doLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = binding.userId.getText().toString();
                String userPassword = binding.userPassword.getText().toString();
                chatClient.login(userId, userPassword);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    PopupMessageFragment.OnMessageListener mMessageListener = new PopupMessageFragment.OnMessageListener() {
        @Override
        public void onConfirm() {}
    };

    ChatClient.OnListener mListener = new ChatClient.OnListener() {
        @Override
        public void onLoginSuccess(String userId, String userNickname, String userRole) {
            JWTTokenContainer.getInstance().setToken(chatClient.getJWTToken());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_LobbyFragment);
                }
            });
        }

        @Override
        public void onLoginFail(String code, String message) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PopupMessageFragment popUp = PopupMessageFragment.newInstance("로그인 실패", message);
                    popUp.setListener(mMessageListener);
                    popUp.show(getChildFragmentManager(), null);
                }
            });
        }
    };
}