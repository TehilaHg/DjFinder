package com.example.DjFinder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import com.example.DjFinder.databinding.FragmentMyProfileBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.User;
import com.squareup.picasso.Picasso;
import com.example.DjFinder.model.Model;


public class MyProfileFragment extends Fragment {

    private static FragmentMyProfileBinding binding;
    private static UserRecyclerAdapter adapter;
    private static MyProfileViewModel myProfileViewModel;
    private UserViewModel userViewModel;
    private static User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myProfileViewModel = new ViewModelProvider(this).get(MyProfileViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {


            userViewModel.refreshUser();
        });
        setupButtons();

        return view;
    }

    void updateUI(User newUser) {
        if (newUser != null) {
            user = newUser;
            binding.userNameMyProfile.setText(user.userName);
            binding.emailMyProfile.setText(user.email);

            if (user.avatar != null && !user.avatar.isEmpty()) {
                Picasso.get().load(user.avatar).into(binding.avatarMyProfile);
            } else {
                binding.avatarMyProfile.setImageResource(R.drawable.profile_pic);
            }
            updatePosts();
        } else {
            Log.e("MyProfileFragment", "User is null");
        }
    }

    private void updatePosts() {
        myProfileViewModel.getData(user.userName).observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter = new UserRecyclerAdapter(posts, getLayoutInflater());
                binding.recyclerView.setAdapter(adapter);
                adapter.SetItemClickListener(pos -> {
                    if (pos >= 0 && pos < posts.size()) {
                        MyProfileFragmentDirections.ActionMyProfileFragmentToPostFragment action =
                                MyProfileFragmentDirections.actionMyProfileFragmentToPostFragment(posts.get(pos).id);
                        Navigation.findNavController(requireView()).navigate(action);
                    } else {
                        Log.e("MyProfileFragment", "Invalid position: " + pos);
                    }
                });
            } else {
                Log.e("MyProfileFragment", "Posts are null");
            }
        });
    }

    private void setupButtons() {
        binding.editBtnMyProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_myProfileFragment_to_editProfileFragment));

        binding.logOutBtnMyProfile.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("yes", (dialog, which) -> {
                    Model.getInstance().logOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("no", (dialog, which) -> {})
                .create().show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        userViewModel.refreshUser();
        userViewModel.getUser().observe(getViewLifecycleOwner(), this::updateUI);
    }






}