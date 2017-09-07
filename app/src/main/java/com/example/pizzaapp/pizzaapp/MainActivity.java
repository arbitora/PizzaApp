package com.example.pizzaapp.pizzaapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ArrayList<PizzaData.Pizza> PizzaArrayList; // Has the full list of all the pizzas

    private static PizzaAdapter mPizzaAdapter;

    private ListView pizzaListView;
    private EditText etxt_SearchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize the toppings for each pizza. Needs to be done if language was changed.
        PizzaData.setToppings(getResources().getStringArray(R.array.txt_desc_toppings_array));

        // Connect the components from the view to variables.
        pizzaListView = (ListView) findViewById(R.id.listPizzaView);
        etxt_SearchField = (EditText) findViewById(R.id.ETXT_Search);
        //btn_Checkout = (Button) findViewById(R.id.BTN_Checkout);


        // Initializes PizzaArrayList, getting mockup data from PizzaData.java.
        fillPizzaArrayList(true);

        // Bind ArrayList to adapter and set the adapter to ListView.
        mPizzaAdapter = new PizzaAdapter(PizzaArrayList, getApplicationContext());
        mPizzaAdapter.setPizzaQuantityListener(new PizzaAdapter.PizzaQuantityListener(){
            @Override
            public void onPizzaQuantityChange(Context context, PizzaData.Pizza changedPizza, boolean isIncreased){

                String temp;
                if (isIncreased){
                    temp = changedPizza.getName() + " -" + getResources().getString(R.string.txt_desc_added) + ".";
                }
                else{
                    temp = changedPizza.getName() + " -" + getResources().getString(R.string.txt_desc_removed) + ".";
                }

                Toast.makeText(context, temp, Toast.LENGTH_SHORT).show();
            }
        });
        pizzaListView.setAdapter(mPizzaAdapter);

        etxt_SearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing at the moment.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing at the moment.
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchPizzaList(s.toString());
            }
            });

        if (savedInstanceState != null){
            // TODO: Load customer's order list.
            // TODO: Load search field properties.
        }
    }


    public void toCheckout(View v){
        // TODO: Load checkout activity.

        Intent checkout = new Intent(this, checkoutActivity.class);
        Gson gson = new Gson();

        String jsonPizzaArrayList = gson.toJson(PizzaArrayList);

        checkout.putExtra("pizza_list_json", jsonPizzaArrayList);
        startActivity(checkout);
    }


    /*
        Filters out pizzas by comparing the CharSequence input with every pizza's toppings.
     */
    private void searchPizzaList(CharSequence input){

        input = input.toString().replace(' ', ',');
        String[] input_words = input.toString().split(",");


        if (mPizzaAdapter != null){
            if (input.length() > 0){
                mPizzaAdapter.clear();
                for (int i = 0; i <  PizzaData.pizzas.length; i++){

                    // Always keep special pizza with random ingredients.
                    if (PizzaData.pizzas[i].getToppingsID()[0] == 9) {
                        mPizzaAdapter.add(PizzaData.pizzas[i]);
                    }


                    else{
                        // For each input word, search.
                        int match = 0; // Used to count matches.
                        for (String word : input_words){
                            // If pizza contains the given input word in toppings, increment match by 1.
                            if (PizzaData.pizzas[i].getToppings().contains(word)){
                                // Add pizza into the list
                                match++;
                            }

                            // If there were as many matches as there are keywords, add Pizza to list.
                            if (match == input_words.length){
                                mPizzaAdapter.add(PizzaData.pizzas[i]);
                            }
                        }
                    }
                }

                // Sort the new list of pizzas.
                mPizzaAdapter.sort(PizzaData.Pizza.Comparators.PizzaPrice_PizzaName);
                mPizzaAdapter.notifyDataSetChanged();
            }
            else{
                // Update the adapter with the new normal view.
                fillPizzaArrayList(true);
            }
        }
    }

    /*
        Initializes the PizzaArrayList with the mockup pizza data.
        If doSort is true, the list will be sorted by the default comparator method.
     */
    private void fillPizzaArrayList(boolean doSort){

        // Initialize pizza ArrayList
        if (PizzaArrayList != null)
            PizzaArrayList.clear();
        else
            PizzaArrayList = new ArrayList<>();

        // Add pizzas into ArrayList
        for (int i = 0; i < PizzaData.pizzas.length; i++){
            PizzaArrayList.add(PizzaData.pizzas[i]);
        }

        // At the end, sort the new list if required.
        if(doSort)
            Collections.sort(PizzaArrayList, PizzaData.Pizza.Comparators.PizzaPrice_PizzaName);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

    }


    // This callback is called only when there is a saved instance previously saved using
    // onSaveInstanceState(). We restore some state in onCreate() while we can optionally restore
    // other state here, possibly usable after onStart() has completed.
    // The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO: Load customer's order list.
        // TODO: Load search field properties.
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO: Save customer's order list.
        // TODO: Save search field properties.

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}
