package com.example.DjFinder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.DjFinder.databinding.FragmentEditPostBinding;
import com.example.DjFinder.model.Model;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EditPostFragment extends Fragment {

    FragmentEditPostBinding binding;
    ImageViewModel viewModel;
    boolean isImageSelected;
    ActivityResultLauncher<Void> cameraLauncher;
    ActivityResultLauncher<String> galleryLauncher;
    String recommendedType;
    String postId;
    String recommenderCategory;
    String djName;
    String djDescription;
    String eventDate;
    private Calendar calendar = Calendar.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicturePreview()
                ,new ActivityResultCallback<Bitmap>() {
            @Override
            public void onActivityResult(Bitmap o) {
                if (o != null) {
                    viewModel.setBitmap(o);
                    binding.djImgEditPost.setImageBitmap(viewModel.getBitmap());
                    isImageSelected = true;
                }
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent()
                ,new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri o) {
                if (o != null) {
                    viewModel.setUrl(o);
                    binding.djImgEditPost.setImageURI(viewModel.getUrl());
                    isImageSelected = true;
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditPostBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        viewModel=new ViewModelProvider(this).get(ImageViewModel.class);
        if (viewModel.getBitmap()!=null)
            binding.djImgEditPost.setImageBitmap(viewModel.getBitmap());
        if (viewModel.getUrl()!=null)
            binding.djImgEditPost.setImageURI(viewModel.getUrl());

        Spinner recommendedTypeSpinner = binding.recommenderSpinnerEditPost;
        recommendedTypeSpinner.setAdapter(SpinnerAdapter.setRecommenderCategoriesSpinner(getContext()));
        recommendedTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recommendedType = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.cameraBtnEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraLauncher.launch(null);
            }
        });

        binding.editImgBtnEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/*");
            }
        });

        binding.eventDateEditPost.setInputType(InputType.TYPE_NULL);
        binding.eventDateEditPost.setOnClickListener(v -> showDatePickerDialog());
        binding.eventDateEditPost.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePickerDialog();
            }
        });


        postId = EditPostFragmentArgs.fromBundle(getArguments()).getPostId();
        Model.getInstance().getPostById(postId).observe(getViewLifecycleOwner(),(post)-> {

            Picasso.get().load(Uri.parse(post.djUrl)).into(binding.djImgEditPost);
            binding.djNameEditPost.setText(post.djName);
            binding.djDescriptionEditPost.setText(post.djDescription);
            binding.eventDateEditPost.setText(post.eventDate);

            binding.confirmBtnEditPost.setOnClickListener(view1->{

                post.djName = binding.djNameEditPost.getText().toString();
                post.djDescription = binding.djDescriptionEditPost.getText().toString();
                post.eventDate = binding.eventDateEditPost.getText().toString();
                post.recommender = recommendedType;

                if (validateInput()) {
                    if (isImageSelected) {
                        binding.djImgEditPost.setDrawingCacheEnabled(true);
                        binding.djImgEditPost.buildDrawingCache();
                        Bitmap bitmap = ((BitmapDrawable) binding.djImgEditPost.getDrawable()).getBitmap();
                        Model.getInstance().uploadImage(postId, bitmap, (url) -> {
                            post.setDjUrl(url);
                            Model.getInstance().updatePost(post, (unused) -> {
                                Navigation.findNavController(view1).popBackStack(R.id.postFragment, false);
                            });
                        });
                    } else {
                        Model.getInstance().updatePost(post, (unused) -> {
                            Navigation.findNavController(view1).popBackStack(R.id.postFragment, false);
                        });
                    }
                }
            });
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
        binding.eventDateEditPost.setText(sdf.format(calendar.getTime()));
    }

    public boolean validateInput() {
        djName = binding.djNameEditPost.getText().toString();
        djDescription = binding.djDescriptionEditPost.getText().toString();
        eventDate = binding.eventDateEditPost.getText().toString();

        if (djName == null || djName.trim().isEmpty()) {
            makeAToast("Please write the name of the DJ");
            return false;
        } else if (djDescription == null || djDescription.trim().isEmpty()) {
            makeAToast("Please write a description for the DJ");
            return false;
        } else if (eventDate == null || eventDate.trim().isEmpty()) {
            makeAToast("Please enter the event date");
            return false;
        } else if (!isValidDate(eventDate)) {
            makeAToast("Please enter a valid date (today or earlier)");
            return false;
        } else if (recommendedType == null || recommendedType.equals("Recommender")) {
            makeAToast("Please choose if you are event owner or a guest");
            return false;
        }
        return true;
    }

    private boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date selectedDate = sdf.parse(dateStr);
            Date currentDate = new Date();
            return !selectedDate.after(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void makeAToast (String text){
        new AlertDialog.Builder(getContext())
                .setTitle("Invalid Input")
                .setMessage(text)
                .setPositiveButton("Ok", (dialog, which) -> {
                })
                .create().show();
    }
}

