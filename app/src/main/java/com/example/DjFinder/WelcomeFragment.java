package com.example.DjFinder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.DjFinder.databinding.FragmentWelcomeBinding;

public class WelcomeFragment extends Fragment {
    FragmentWelcomeBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        binding.loginBtnWelcome.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_welcomeFragment_to_loginFragment));
        binding.signupBtnWelcome.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_welcomeFragment_to_signUpFragment));
        return view;
    }
}