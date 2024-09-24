package com.example.DjFinder;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.DjFinder.model.Model;

import java.util.List;


public class SpinnerAdapter {


    public static ArrayAdapter<String> setRecommenderCategoriesSpinner(Context context)
    {
        List<String> recommenderCategories = Model.getInstance().getRecommenderCategories();
        ArrayAdapter<String> RecommenderCategoriesSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item,recommenderCategories);
        RecommenderCategoriesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        return RecommenderCategoriesSpinnerAdapter;
    }

}
