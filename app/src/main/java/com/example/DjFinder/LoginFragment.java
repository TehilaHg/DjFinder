package com.example.DjFinder;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.DjFinder.databinding.FragmentLoginBinding;
import com.example.DjFinder.model.Model;


public class LoginFragment extends Fragment {

    FragmentLoginBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.loginUsername.getText().toString().trim();
                String password = binding.loginPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in both Username and Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                Model.getInstance().logIn(username, password, (isSuccessful) -> {
                    if (isSuccessful) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Username or Password are Incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return view;
    }
}