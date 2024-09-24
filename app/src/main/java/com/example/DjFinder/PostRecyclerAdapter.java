package com.example.DjFinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.Post;
import com.example.DjFinder.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


// Post View Holder
class PostViewHolder extends RecyclerView.ViewHolder {
    TextView userNameTv;
    TextView djNameTv;
    ImageView avatarImg;
    ImageView djImg;
    ImageButton likeBtn;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        userNameTv = itemView.findViewById(R.id.userName_plr);
        djNameTv = itemView.findViewById(R.id.dj_name_plr);
        avatarImg = itemView.findViewById((R.id.avatar_plr));
        djImg = itemView.findViewById(R.id.dj_img_plr);
        likeBtn = itemView.findViewById(R.id.post_like_btn);
    }

    public void bind(User user, Post post, PostRecyclerAdapter.OnItemClickListener onImageClickListener,
                     PostRecyclerAdapter.OnItemClickListener onUsernameClickListener, Context context) {

        userNameTv.setText(post.userName);
        // set user avatar
        if (Objects.equals(post.getAvatarUrl(), "")) {
            avatarImg.setImageResource(R.drawable.profile_pic);
        }else {Picasso.get().load(post.getAvatarUrl()).into(avatarImg);}

        djNameTv.setText(post.djName);

        if (!Objects.equals(post.getDjUrl(), "")) {
            Picasso.get().load(post.getDjUrl()).placeholder(R.drawable.djfinder).into(djImg);
        } else {djImg.setImageResource(R.drawable.djfinder);}

//        likeBtn.setImageResource(R.drawable.like_btn);

        if (user.likedPosts.contains(post.id)){
            this.likeBtn.setImageResource(R.drawable.liked_black_icon);
        }

        likeBtn.setOnClickListener(view -> {
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

        this.djImg.setOnClickListener(view->{
            onImageClickListener.onItemClick(getAdapterPosition());
        });

        this.userNameTv.setOnClickListener(view->{
            onUsernameClickListener.onItemClick(getAdapterPosition());
        });
    }
}


// Post Adapter - makes the rows of the list
public class PostRecyclerAdapter extends RecyclerView.Adapter<PostViewHolder> {

    LayoutInflater inflater;
    Context context;
    List<Post> data;
    User user;
    OnItemClickListener listener;
    OnItemClickListener onImageClickListener;
    OnItemClickListener onUsernameClickListener;

    public PostRecyclerAdapter(User user, LayoutInflater inflater, List<Post> data, Context context){
        this.inflater = inflater;
        this.context = context;
        this.data = data;
        this.user = user;
    }

    public static interface OnItemClickListener {
        void onItemClick(int pos);
    }

    void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public void setOnImageClickListener(OnItemClickListener onImageClickListener){
        this.onImageClickListener = onImageClickListener;
    }
    public void setOnUsernameClickListener(OnItemClickListener onUsernameClickListener){
        this.onUsernameClickListener = onUsernameClickListener;
    }

    public void setData(List<Post> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.post_list_row, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = data.get(position);
        holder.bind(user,post,onImageClickListener,onUsernameClickListener,context);
    }

    @Override
    public int getItemCount() {
        if (data== null) return 0;
        return data.size();
    }

    public void setUser(User user) {
        this.user = user;
    }
}

