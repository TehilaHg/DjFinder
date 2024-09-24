package com.example.DjFinder.restAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface QuoteApiService {
    @Headers({
            "x-rapidapi-key: 6c851ee562msh232e15fdf9e995cp1b5042jsna5f28ff86992",
            "x-rapidapi-host: quotes-inspirational-quotes-motivational-quotes.p.rapidapi.com"
    })
    @GET("quote")
    Call<Quote> getQuote(@Query("token") String token);
}





