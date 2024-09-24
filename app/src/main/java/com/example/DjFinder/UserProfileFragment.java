package com.example.DjFinder;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.DjFinder.databinding.FragmentUserProfileBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.User;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class UserProfileFragment extends Fragment {

    FragmentUserProfileBinding binding;
    UserRecyclerAdapter adapter;
    UserProfileViewModel viewModel;
    User user;
    View view;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUserProfileBinding.inflate(inflater,container,false);
        view = binding.getRoot();
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        username = UserProfileFragmentArgs.fromBundle(getArguments()).getUsername();

        Model.getInstance().getOtherUser(username).observe(getViewLifecycleOwner(), (otherUser)->{
            if (otherUser != null){
                this.user = otherUser;
                setProfile();
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
    }

    public void setProfile() {

        binding.userNameUserProfile.setText(user.userName);
        binding.emailUserProfile.setText(user.email);

        if (Objects.equals(user.avatar, "")) {
            binding.avatarUserProfile.setImageResource(R.drawable.profile_pic);
        } else {
            Picasso.get().load(user.avatar).into(binding.avatarUserProfile);
        }

        viewModel.getData(user.userName).observe(getViewLifecycleOwner(), (posts) -> {
            adapter = new UserRecyclerAdapter(posts, getLayoutInflater());
            binding.recyclerView.setAdapter(adapter);

            adapter.SetItemClickListener(new UserRecyclerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int pos) {
                    UserProfileFragmentDirections.ActionUserProfileFragmentToPostFragment action = UserProfileFragmentDirections.actionUserProfileFragmentToPostFragment(posts.get(pos).id);
                    Navigation.findNavController(view).navigate(action);
                }
            });
        });
    }

}
