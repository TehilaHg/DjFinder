package com.example.DjFinder;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.DjFinder.databinding.FragmentCreatePostBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.Post;
import com.example.DjFinder.model.User;

import java.util.Objects;
import java.util.UUID;

import android.app.DatePickerDialog;
import android.text.InputType;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import java.text.ParseException;
import java.util.Date;

public class CreatePostFragment extends Fragment {
    FragmentCreatePostBinding binding;
    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;
    Boolean isDjImageSelected = false;
    UserViewModel userViewModel;
    User user;
    String recommenderCategory;
    String djName;
    String djDescription;
    String eventDate;
    private Calendar calendar = Calendar.getInstance();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                if (o != null) {
                    binding.djImgCreatePost.setImageURI(o);
                    isDjImageSelected = true;
                }
            }
        });
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), new ActivityResultCallback<Bitmap>() {
            @Override
            public void onActivityResult(Bitmap result) {
                if (result != null) {
                    binding.djImgCreatePost.setImageBitmap(result);
                    isDjImageSelected = true;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();


        binding.eventDateCreatePost.setInputType(InputType.TYPE_NULL);
        binding.eventDateCreatePost.setOnClickListener(v -> showDatePickerDialog());
        binding.eventDateCreatePost.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePickerDialog();
            }
        });
        userViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User newUser) {
                user = newUser;
            }
        });

        Spinner recommenderCategorySpinner = binding.categorySpinnerCreatePost;
        recommenderCategorySpinner.setAdapter(SpinnerAdapter.setRecommenderCategoriesSpinner(getContext()));

        recommenderCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recommenderCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.confirmBtnCreatePost.setOnClickListener(view1 ->
        {
            djName = binding.djNameCreatePost.getText().toString();
            djDescription = binding.djDescriptionCreatePost.getText().toString();
            eventDate = binding.eventDateCreatePost.getText().toString();

            if (validateInput()) {
                binding.djImgCreatePost.setDrawingCacheEnabled(true);
                binding.djImgCreatePost.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) binding.djImgCreatePost.getDrawable()).getBitmap();
                String id = UUID.randomUUID().toString();
                Post post = new Post(id, user.userName, recommenderCategory, djName, djDescription, eventDate, user.avatar, "", " ");

                Model.getInstance().uploadImage(id, bitmap, url -> {
                    if (url != null) {
                        post.setDjUrl(url);
                    }
                    Model.getInstance().addPost(post, (unused) -> {
                        Navigation.findNavController(view1).popBackStack(R.id.postListFragment, false);
                    });
                });
            }
        });

        binding.cameraBtnCreatePost.setOnClickListener(view1 -> {
            cameraLauncher.launch(null);
        });

        binding.editImgBtnCreatePost.setOnClickListener(view1 -> {
            galleryLauncher.launch("image/*");
        });

        return view;
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateInView();
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }
    private void updateDateInView() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        binding.eventDateCreatePost.setText(sdf.format(calendar.getTime()));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    public boolean validateInput() {
        if (!isDjImageSelected) {
            makeAToast("Please choose a picture for the Dj");
            return false;
        } else if (Objects.equals(djName, "")) {
            makeAToast("Please write the name of the Dj");
            return false;
        } else if (Objects.equals(djDescription, "")) {
            makeAToast("Please write a description for the Dj");
            return false;
        } else if (Objects.equals(eventDate, "")) {
            makeAToast("Please enter the date event");
            return false;
        } else if (!isValidDate(eventDate)) {
            makeAToast("Please enter a valid date (today or earlier)");
            return false;
        } else if (Objects.equals(recommenderCategory, "Recommender")) {
            makeAToast("Please choose if you are event owner or a guest");
            return false;
        }
        return true;
    }
    public void makeAToast (String text){
        new AlertDialog.Builder(getContext())
                .setTitle("Invalid Input")
                .setMessage(text)
                .setPositiveButton("Ok", (dialog, which) -> {
                })
                .create().show();
    }

    private boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date selectedDate = sdf.parse(dateStr);
            Date currentDate = new Date();
            return !selectedDate.after(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

    }
}