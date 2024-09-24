package com.example.DjFinder;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.DjFinder.databinding.FragmentPostBinding;
import com.example.DjFinder.model.Model;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PostFragment extends Fragment {

    String postId;
    FragmentPostBinding binding;
    UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        // get the post id by Argument from PostListFragment Navigation
        postId = PostFragmentArgs.fromBundle(getArguments()).getPostId();

        Model.getInstance().getPostById(postId).observe(getViewLifecycleOwner(), (post)->{
            if (post != null) {
                binding.postUsername.setText(post.userName);
                binding.postDjName.setText(post.djName);
                Picasso.get().load(post.djUrl).into(binding.postDjImg);
                binding.postDescription.setText(post.djDescription);
                binding.postEventDate.setText(post.eventDate);
                binding.recommenderPostFrag.setText(post.recommender);

                userViewModel.getUser().observe(getViewLifecycleOwner(), (user)->{

                    if (user != null)
                    {
                        // the user is not the owner of the post
                        if (Objects.equals(post.userName, user.userName)){
                            binding.postLikeBtn.setVisibility(View.GONE);
                            binding.postEditBtn.setVisibility(View.VISIBLE);
                            binding.postDeleteBtn.setVisibility(View.VISIBLE);
                            binding.postEditBtn.setOnClickListener(Navigation.createNavigateOnClickListener
                                    (PostFragmentDirections.actionPostFragmentToEditPostFragment(postId)));
                        }
                        // the user is not the owner of the post
                        else {
                            binding.postLikeBtn.setVisibility(View.VISIBLE);
                            binding.postEditBtn.setVisibility(View.GONE);
                            binding.postDeleteBtn.setVisibility(View.GONE);

                            ImageButton likeBtn = binding.postLikeBtn;
                            if (user.likedPosts.contains(post.id)) {
                                likeBtn.setImageResource(R.drawable.liked_black_icon);
                            }

                            likeBtn.setOnClickListener(v -> {
                                List<String> likedPostsList = new ArrayList<>(user.getLikedPosts());

                                if (!user.likedPosts.contains(post.id)) {
                                    likedPostsList.add(post.id);
                                    user.setLikedPosts(likedPostsList);
                                    Model.getInstance().updateLikedPosts(user);
                                    likeBtn.setImageResource(R.drawable.liked_black_icon);
                                }
                                else{
                                    likedPostsList.remove(post.id);
                                    user.setLikedPosts(likedPostsList);
                                    Model.getInstance().updateLikedPosts(user);
                                    likeBtn.setImageResource(R.drawable.like_btn);
                                }
                            });
                        }
                        binding.postDeleteBtn.setOnClickListener((view1)->{
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Are you sure you want to delete the post?")
                                    .setMessage("Your post will be permanently deleted")
                                    .setPositiveButton("Yes", (dialog,which)->{
                                        Model.getInstance().deletePost(post,(unused)->{
                                            Navigation.findNavController(view1).popBackStack(R.id.myProfileFragment,false);
                                        });
                                    }).setNegativeButton("No",(dialog,which)->{})
                                    .create().show();
                        });
                    }
                });
            } else {
                Navigation.findNavController(view).popBackStack();

            }
        });

        return view;
    }
}