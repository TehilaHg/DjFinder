package com.example.DjFinder.restAPI;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuoteRepository {
    private static final String BASE_URL = "https://quotes-inspirational-quotes-motivational-quotes.p.rapidapi.com/";

    private QuoteApiService quoteApiService;

    public QuoteRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        quoteApiService = retrofit.create(QuoteApiService.class);
    }

    public void getRandomQuote(QuoteCallback callback) {
        quoteApiService.getQuote("ipworld.info").enqueue(new Callback<Quote>() {
            @Override
            public void onResponse(Call<Quote> call, Response<Quote> response) {
                if (response.isSuccessful()) {
                    callback.onQuoteReceived(response.body());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<Quote> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface QuoteCallback {
        void onQuoteReceived(Quote quote);
        void onError(String errorMessage);
    }
}