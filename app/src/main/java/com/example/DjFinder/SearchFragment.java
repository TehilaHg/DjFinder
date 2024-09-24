package com.example.DjFinder;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.DjFinder.databinding.FragmentSearchBinding;
import com.example.DjFinder.model.Model;
import com.example.DjFinder.model.Post;


public class SearchFragment extends Fragment {

    FragmentSearchBinding binding;
    SeachViewModel searchViewModel;
    RecyclerView recyclerView;
    UserViewModel userViewModel;
    PostRecyclerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchViewModel = new ViewModelProvider(this).get(SeachViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.searchViewSearchFrag.clearFocus();

        adapter = new PostRecyclerAdapter(userViewModel.getUser().getValue(), getLayoutInflater(),
                searchViewModel.getData(), getContext());
        recyclerView.setAdapter(adapter);

        binding.searchViewSearchFrag.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                Model.getInstance().getPostsByDjName(newText, (data) -> {

                    adapter.setOnImageClickListener(pos -> {
                        Post post = data.get(pos);
                        SearchFragmentDirections.ActionSearchFragmentToPostFragment action = SearchFragmentDirections.actionSearchFragmentToPostFragment(post.id);
                        Navigation.findNavController(view).navigate(action);

                    });

                    searchViewModel.setData(data);
                    adapter.setData(data);
                });
                return true;
            }
        });

        binding.searchBtnSearchFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String djName = binding.searchViewSearchFrag.getQuery().toString();
                Model.getInstance().getPostsByDjName(djName, (data) -> {
                    searchViewModel.setData(data);
                    adapter.setData(data);
                });
                hideKeyboard();
            }
        });

        return view;
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

