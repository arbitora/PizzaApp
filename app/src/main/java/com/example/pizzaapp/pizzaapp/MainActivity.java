package com.example.pizzaapp.pizzaapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ArrayList<PizzaData.Pizza> PizzaArrayList; // Has the full list of all the pizzas

    private static PizzaAdapter mPizzaAdapter;

    private ListView pizzaListView;
    private EditText etxt_SearchField; // Search field.

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
            public void onPizzaQuantityChange(PizzaData.Pizza changedPizza, boolean isIncreased){


                // Simple toast message, disabled for final build.
                //pizzaQuantityChangeToast(changedPizza, isIncreased);
                // Text message to see the changes.
                Log.d("STRING", "Message: " + PizzaData.pizzas);
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
                // Do filtering search everytime a change is detected.
                searchPizzaList(s.toString());
            }
        });
    }

    /*
        Loads checkoutActivity, the cart view.
     */
    public void toCheckout(View v){

        // Create intent.
        Intent checkout = new Intent(this, checkoutActivity.class);

        // Create temporary Gson object to handle json creation.
        Gson gson = new Gson();

        // Get the current selected pizzas from PizzaData.pizzas, where all pizzas are stored.
        // Save them in a temporary ArrayList.
        ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
        for (PizzaData.Pizza pizza : PizzaArrayList){
            if (pizza.getQuantity() > 0){
                orderedPizzas.add(pizza);
            }
        }

        // Turn the temporary ArrayList into json
        String jsonPizzaArrayList = gson.toJson(orderedPizzas);

        // Put the Json string into intent's extra and start activity.
        checkout.putExtra("pizza_list_json", jsonPizzaArrayList);

        //startActivity(checkout); // Not used as result is required
        // Request code is not used
        startActivityForResult(checkout, 0);
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
        Initializes the PizzaArrayList with the mock up pizza data.
        If doSort is true, the list will be sorted by the default comparator method.
     */
    private void fillPizzaArrayList(boolean doSort){

        // Initialize pizza ArrayList
        if (PizzaArrayList == null)
            PizzaArrayList = new ArrayList<>();

        // Add all pizzas that have larger than 0 quantity into temporary list.
        ArrayList<PizzaData.Pizza> tempList = new ArrayList<>();
        for (PizzaData.Pizza pizza : PizzaArrayList) {
            if (pizza.getQuantity() > 0)
                tempList.add(pizza);
        }

        PizzaArrayList.clear();

        // Populate main PizzaArrayList with all the pizzas.
        for (PizzaData.Pizza pizza : PizzaData.pizzas){
            PizzaArrayList.add(pizza);

        }

        // Clone the pizza quantities from the temporary list.
        for (PizzaData.Pizza pizza : tempList){
            for (PizzaData.Pizza ArrayListPizza : PizzaArrayList){
                if (pizza.getName().equals(ArrayListPizza.getName())){
                    ArrayListPizza.setQuantity(pizza.getQuantity());
                }
            }
        }

        // At the end, sort the new list if required.
        if(doSort)
            Collections.sort(PizzaArrayList, PizzaData.Pizza.Comparators.PizzaPrice_PizzaName);
    }

    /*
        Creates a short duration toast message when quantity of a pizza has been changed.
        changedPizza is the pizza which's quantity was changed.
        isIncreased true = quantity was increased, false = quantity was decreased.
     */
    private void pizzaQuantityChangeToast(PizzaData.Pizza changedPizza, boolean isIncreased){
        if (changedPizza != null){
            String temp;
            if (isIncreased){
                temp = changedPizza.getName() + " -" + getResources().getString(R.string.txt_desc_added) + ".";
            }
            else{
                temp = changedPizza.getName() + " -" + getResources().getString(R.string.txt_desc_removed) + ".";
            }

            Toast.makeText(this, temp, Toast.LENGTH_SHORT).show();
        }
    }

    /*
        Invoked when returning from Checkout view.
        If user created the order, resultCode returns RESULT_OK.
        If user cancelled order and returned to MainActivity, resultCode returns RESULT_CANCELED.
        Intent data has the order contents listed, these will be loaded to show proper quantities.
        JSON string can be found via keyword pizza_list_json
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // TODO: Check ResultCode
        // TODO: Do things depending on ResultCode

        // Checkout was successful, reset everything.
        if (resultCode == RESULT_OK){
            fillPizzaArrayList(true);
            for (PizzaData.Pizza pizza : PizzaArrayList){
                pizza.setQuantity(0); // Reset all quantities.
            }
        }
        // Checkout was cancelled, load the data.
        else if (resultCode == RESULT_CANCELED && data != null){

            // Check that the extra is there before accessing it.
            if(data.hasExtra("pizza_list_json")){

                ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
                String jsonPizzaArrayList = data.getStringExtra("pizza_list_json");
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<PizzaData.Pizza>>(){}.getType();

                orderedPizzas = gson.fromJson(jsonPizzaArrayList, type);

                for (PizzaData.Pizza pizza_from_list : PizzaArrayList){
                    pizza_from_list.setQuantity(0); // Reset all quantities.
                    for (PizzaData.Pizza pizza_from_order : orderedPizzas){
                        if (pizza_from_list.getName().equals(pizza_from_order.getName())){
                            pizza_from_list.setQuantity(pizza_from_order.getQuantity());
                        }
                    }

                }
            }

        }

        if (mPizzaAdapter != null)
            mPizzaAdapter.notifyDataSetChanged();
    }


    // This callback is called only when there is a saved instance previously saved using
    // onSaveInstanceState(). We restore some state in onCreate() while we can optionally restore
    // other state here, possibly usable after onStart() has completed.
    // The savedInstanceState Bundle is same as the one used in onCreate().
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            // Load the string
            String jsonPizzaArrayList = savedInstanceState.getString("pizza_list_json");

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PizzaData.Pizza>>(){}.getType();

            PizzaArrayList = gson.fromJson(jsonPizzaArrayList, type);

            // Load the Search text field text.
            etxt_SearchField.setText(savedInstanceState.getString("search_text"));
        }

    }

    // Invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Create temporary Gson object to handle json creation.
        Gson gson = new Gson();

        // Get the current selected pizzas from PizzData.pizzas, where all pizzas are stored.
        // Save them in a temporary ArrayList.
        ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
        for (PizzaData.Pizza pizza : PizzaData.pizzas){
            if (pizza.getQuantity() > 0){
                orderedPizzas.add(pizza);
            }
        }

        // Turn the temporary ArrayList into json
        String jsonPizzaArrayList = gson.toJson(orderedPizzas);

        // Save json string in outState.
        outState.putString("pizza_list_json", jsonPizzaArrayList);
        // Save current search field text in outState.
        outState.putString("search_text", etxt_SearchField.getText().toString());


        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }
}
