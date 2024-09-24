package com.example.DjFinder;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import com.example.DjFinder.databinding.FragmentLikedPostsBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.Post;
import com.example.DjFinder.model.User;
import java.util.ArrayList;
import java.util.List;

public class LikedPostsFragment extends Fragment {

    private FragmentLikedPostsBinding binding;
    private LikedPostsViewModel viewModel;
    private UserViewModel userViewModel;
    private PostRecyclerAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLikedPostsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(LikedPostsViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        View view = binding.getRoot();

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PostRecyclerAdapter(null, getLayoutInflater(), new ArrayList<>(), getContext());
        binding.recyclerView.setAdapter(adapter);

        userViewModel.getUser().observe(getViewLifecycleOwner(), this::handleUserUpdate);

        return view;
    }

    private void handleUserUpdate(User user) {
        if (user == null) {
            Log.e("LikedPostsFragment", "User is null");
            // Consider showing an error message to the user
            return;
        }

        adapter.setUser(user);

        if (user.likedPosts == null || user.likedPosts.isEmpty()) {
            Log.i("LikedPostsFragment", "User has no liked posts");
            viewModel.setLikedPosts(new ArrayList<>());
            adapter.setData(new ArrayList<>());
            return;
        }

        Model.getInstance().getLikedPosts(user.likedPosts, this::handleLikedPosts);
    }

    private void handleLikedPosts(List<Post> posts) {
        if (posts == null) {
            Log.e("LikedPostsFragment", "Failed to fetch liked posts");
            // Consider showing an error message to the user
            return;
        }

        viewModel.setLikedPosts(posts);
        adapter.setData(posts);

        adapter.setOnImageClickListener(pos -> {
            if (pos >= 0 && pos < posts.size()) {
                Post post = posts.get(pos);
                LikedPostsFragmentDirections.ActionLikedPostsFragmentToPostFragment action =
                        LikedPostsFragmentDirections.actionLikedPostsFragmentToPostFragment(post.id);
                Navigation.findNavController(requireView()).navigate(action);
            } else {
                Log.e("LikedPostsFragment", "Invalid position: " + pos);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}