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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.DjFinder.databinding.FragmentSignUpBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.User;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpFragment extends Fragment {

    FragmentSignUpBinding binding;
    ActivityResultLauncher<String> galleryLauncher;
    ActivityResultLauncher<Void> cameraLauncher;
    Boolean isAvatarSelected = false;
    String username;
    String email;
    String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                if (o != null){
                    binding.signupAvatar.setImageURI(o);
                    isAvatarSelected = true;
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), new ActivityResultCallback<Bitmap>() {
            @Override
            public void onActivityResult(Bitmap o) {
                if (o != null) {
                    binding.signupAvatar.setImageBitmap(o);
                    isAvatarSelected = true;
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.signupSubmitBtn.setOnClickListener(view1 -> {

            username = binding.signupUsername.getText().toString().trim();
            email = binding.signupEmail.getText().toString().trim();
            password = binding.signupPassword.getText().toString();

            if (validateInput()) {
                Model.getInstance().isUsernameTaken(username, (usernameTaken) -> {
                    if (usernameTaken) {
                        makeAToast("Username is Already Taken");
                    } else {
                        Model.getInstance().isEmailTaken(email, (emailTaken) -> {
                            if (emailTaken) {
                                makeAToast("Email Is Already Taken");
                            } else {
                                if (isAvatarSelected) {
                                    binding.signupAvatar.setDrawingCacheEnabled(true);
                                    binding.signupAvatar.buildDrawingCache();
                                    Bitmap bitmap = ((BitmapDrawable) binding.signupAvatar.getDrawable()).getBitmap();
                                    String id = UUID.randomUUID().toString();

                                    Model.getInstance().uploadImage(id, bitmap, avatarUrl -> {

                                        User newUser = new User(username, avatarUrl, email, new ArrayList<>());
                                        Model.getInstance().signUp(newUser, password, (unused) -> {
                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                            startActivity(intent);
                                            getActivity().finish();
                                        });
                                    });
                                } else {
                                    User newUser = new User(username, "", email, new ArrayList<>());
                                    Model.getInstance().signUp(newUser, password, (unused) -> {
                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });


        binding.signupUploadAvatar.setOnClickListener(view1->{
            galleryLauncher.launch("image/*");
        });

        binding.signupCameraBtn.setOnClickListener(view1->{
            cameraLauncher.launch(null);
        });

        return view;
    }

    public void makeAToast(String text){
        new AlertDialog.Builder(getContext())
                .setTitle("Invalid Input")
                .setMessage(text)
                .setPositiveButton("Ok", (dialog,which)->{
                })
                .create().show();
    }

    public boolean validateInput() {
        if (username.trim().isEmpty()) {
            makeAToast("Username cannot be empty");
            return false;
        } else if (!isValidEmail(email)) {
            makeAToast("Please enter a valid email");
            return false;
        } else if (password.length() < 6) {
            makeAToast("Password must contain at least 6 characters");
            return false;
        }
        return true;
    }

    public boolean isValidEmail(String email){
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}