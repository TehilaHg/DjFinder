package com.example.DjFinder;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.DjFinder.databinding.FragmentEditProfileBinding;
import com.example.DjFinder.databinding.FragmentMyProfileBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.User;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class EditProfileFragment extends Fragment {

    FragmentEditProfileBinding binding;
    ActivityResultLauncher<String> galleryLauncher;
    ActivityResultLauncher<Void> cameraLauncher;
    UserViewModel userViewModel;
    Boolean isAvatarSelected = false;
    User currentUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                if (o != null) {
                    binding.editProfileAvatar.setImageURI(o);
                    isAvatarSelected = true;
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), new ActivityResultCallback<Bitmap>() {
            @Override
            public void onActivityResult(Bitmap o) {
                if (o != null) {
                    binding.editProfileAvatar.setImageBitmap(o);
                    isAvatarSelected = true;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.editProfileGalleryBtn.setOnClickListener(view1 -> galleryLauncher.launch("image/*"));
        binding.editProfileCameraBtn.setOnClickListener(view1 -> cameraLauncher.launch(null));

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                updateUIWithUserData(user);
            }
        });

        binding.editProfileConfirmBtn.setOnClickListener(view1 -> {
            String newUsername = binding.editUsername.getText().toString().trim();
            if (validateInput(newUsername)) {
                updateProfile(newUsername);
            }
        });

        return view;
    }

    private void updateUIWithUserData(User user) {
        if (user.avatar != null && !user.avatar.isEmpty()) {
            Picasso.get().load(user.avatar).into(binding.editProfileAvatar);
        } else {
            binding.editProfileAvatar.setImageResource(R.drawable.profile_pic);
        }
        binding.editUsername.setText(user.userName);
    }

    private boolean validateInput(String newUsername) {
        if (newUsername.isEmpty()) {
            binding.editUsername.setError("שם משתמש לא יכול להיות ריק");
            return false;
        }
        if (newUsername.equals(currentUser.userName) && !isAvatarSelected) {
            makeAToast("אין שינויים לבצע");
            return false;
        }
        return true;
    }

    private void updateProfile(String newUsername) {
        Model.getInstance().isUsernameTaken(newUsername, isTaken -> {
            if (isTaken && !newUsername.equals(currentUser.userName)) {
                binding.editUsername.setError("שם משתמש זה כבר קיים");
            } else {
                if (isAvatarSelected) {
                    uploadNewAvatar(newUsername);
                } else {
                    updateUserData(newUsername, currentUser.avatar);
                }
            }
        });
    }

    private void uploadNewAvatar(String newUsername) {
        binding.editProfileAvatar.setDrawingCacheEnabled(true);
        binding.editProfileAvatar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) binding.editProfileAvatar.getDrawable()).getBitmap();
        String id = UUID.randomUUID().toString();

        Model.getInstance().uploadImage(id, bitmap, avatarUrl -> {
            updateUserData(newUsername, avatarUrl);
        });
    }

    private void updateUserData(String newUsername, String avatarUrl) {
        String oldUsername = currentUser.userName;
        User updatedUser = new User(newUsername, avatarUrl, currentUser.email, currentUser.likedPosts);

        Model.getInstance().updateUser(updatedUser, oldUsername, result -> {
            if (result == null) {
                if (!newUsername.equals(oldUsername)) {
                    Model.getInstance().updatePostsUsername(oldUsername, newUsername, () -> {
                        finishUpdate();
                    });
                } else {
                    finishUpdate();
                }
            } else {
                Toast.makeText(getContext(), "כשל בעדכון הפרופיל", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishUpdate() {
        Model.getInstance().refreshAllUsers();
        userViewModel.refreshUser();
        Toast.makeText(getContext(), "הפרופיל עודכן בהצלחה", Toast.LENGTH_SHORT).show();
        navigateToMyProfile();
    }

    private void navigateToMyProfile() {
        Navigation.findNavController(requireView()).navigate(R.id.action_editProfileFragment_to_myProfileFragment);
    }

    public void makeAToast(String text) {
        new AlertDialog.Builder(getContext())
                .setTitle("הודעה")
                .setMessage(text)
                .setPositiveButton("אישור", (dialog, which) -> {})
                .create().show();
    }
}