package com.example.DjFinder;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.DjFinder.databinding.FragmentPostListBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.Post;

import java.util.LinkedList;
import java.util.List;


public class PostListFragment extends Fragment {

    FragmentPostListBinding binding;
    PostRecyclerAdapter adapter;
    PostListFragmentViewModel viewModel;
    UserViewModel userViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(PostListFragmentViewModel.class);
        // Inflate the layout for this fragment
        binding = FragmentPostListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        Model.getInstance().EventPostsListLoadingState.observe(getViewLifecycleOwner(),status->{
            binding.swipeRefresh.setRefreshing(status == Model.LoadingState.LOADING);
        });

        binding.swipeRefresh.setOnRefreshListener(()->{
            reloadData();
        });

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userViewModel.getUser().observe(getViewLifecycleOwner(),(user) -> {

            if (user != null) {

                viewModel.getData().observe(getViewLifecycleOwner(), list -> {
                    adapter = new PostRecyclerAdapter(user, getLayoutInflater(), list, getContext());
                    binding.recyclerView.setAdapter(adapter);

                    adapter.setOnImageClickListener((pos) -> {
                        Post post = list.get(pos);
                        PostListFragmentDirections.ActionPostListFragmentToPostFragment action =
                                PostListFragmentDirections.actionPostListFragmentToPostFragment(post.id);
                        Navigation.findNavController(view).navigate(action);
                    });

                    // username clicked to user profile fragment
                    adapter.setOnUsernameClickListener((pos) -> {
                        Post post = list.get(pos);
                        PostListFragmentDirections.ActionPostListFragmentToUserProfileFragment action =
                                PostListFragmentDirections.actionPostListFragmentToUserProfileFragment(post.userName);
                        Navigation.findNavController(view).navigate(action);
                    });
                });
            }
        });

        binding.progressBar.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    void reloadData() {
        Model.getInstance().refreshAllPosts();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}