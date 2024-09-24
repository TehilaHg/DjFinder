package com.example.DjFinder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.DjFinder.databinding.FragmentWelcomeBinding;
import com.example.DjFinder.restAPI.Quote;
import com.example.DjFinder.restAPI.QuoteRepository;

public class WelcomeFragment extends Fragment {
    private QuoteRepository quoteRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentWelcomeBinding binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        quoteRepository = new QuoteRepository();
        quoteRepository.getRandomQuote(new QuoteRepository.QuoteCallback() {
            @Override
            public void onQuoteReceived(Quote quote) {
                // Update the UI with the received quote
                binding.quoteTextView.setText(quote.getText());
                binding.authorTextView.setText("- " + quote.getAuthor());
            }

            @Override
            public void onError(String errorMessage) {
                // Handle the error
                Toast.makeText(requireContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        binding.loginBtnWelcome.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_welcomeFragment_to_loginFragment));
        binding.signupBtnWelcome.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_welcomeFragment_to_signUpFragment));

        return view;
    }
}