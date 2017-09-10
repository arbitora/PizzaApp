package com.example.pizzaapp.pizzaapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pizzaapp.pizzaapp.PizzaAdapter.PizzaQuantityListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class checkoutActivity extends AppCompatActivity {

    private ArrayList<PizzaData.Pizza> PizzaArrayList; // Has the full list of all the pizzas
    private PizzaAdapter mPizzaAdapter;
    private boolean activityStarted = false; // False by default, true when started via Intent.

    private boolean data_sent = false; // TODO: set true when POST sent successfully.
    Resources res; // To load string resources for messages.
    private boolean nothingOrdered = true; // Avoid doing unnecessary internet connections.

    private ListView pizzaListView; // Stores the pizzas.
    private Button btn_PlaceOrder; // CheckOut button for enabling and disabling it.
    private TextView txt_total_price; // Shows total price.

    private Intent resultIntent; // Intent data is stored here for activity result.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_activity);

        res = getResources();

        // Initialize PizzaArrayList
        PizzaArrayList = new ArrayList<>();

        // Bind components to variables.
        pizzaListView = (ListView) findViewById(R.id.listPizzaView);
        btn_PlaceOrder = (Button) findViewById(R.id.BTN_Order);
        txt_total_price = (TextView) findViewById(R.id.txtView_Total);


        if (savedInstanceState != null)
            activityStarted = savedInstanceState.getBoolean("activityStarted");


        // Get data from intent only on initial run.
        // if ActivityStarted is true, do not load intent data.
        if (!activityStarted && getIntent().hasExtra("pizza_list_json")){
            activityStarted = true;
            ArrayList<PizzaData.Pizza> deleteList = new ArrayList<>(); // Lists Pizza objects we do not want in the main list.

            // Load the string
            String jsonPizzaArrayList = getIntent().getStringExtra("pizza_list_json");

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PizzaData.Pizza>>(){}.getType();

            PizzaArrayList = gson.fromJson(jsonPizzaArrayList, type);
            for (PizzaData.Pizza pizza : PizzaArrayList){

                // Find each pizza that has quantity of 0 and add it to deleteList
                if (pizza.getQuantity() == 0)
                    deleteList.add(pizza);
            }

            // Iterate through deleteList and delete each pizza from the actual PizzaArrayList.
            for (PizzaData.Pizza pizza : deleteList){
                PizzaArrayList.remove(pizza);
            }

            // Call function to show the total price.  No change in quantity was made, thus pass in null.
            onPriceChange(null, false);
        }

        // Bind ArrayList to adapter, implement listener and set the adapter to ListView.
        if (mPizzaAdapter == null){
            mPizzaAdapter = new PizzaAdapter(PizzaArrayList, getApplicationContext());

            mPizzaAdapter.setPizzaQuantityListener(new PizzaQuantityListener(){
                @Override
                public void onPizzaQuantityChange(PizzaData.Pizza changedPizza, boolean isIncreased){
                    // Text message to see the changes.
                    Log.d("STRING", "Message: " + PizzaData.pizzas);

                    // Call function to show the total price.
                    onPriceChange(changedPizza, isIncreased);

                    // Invoke every time a change in pizza quantities is detected.
                    constructResultIntent();
                }
            });
        }

        pizzaListView.setAdapter(mPizzaAdapter);

        if (resultIntent == null)
            resultIntent = new Intent();

        // Construct first time result.
        constructResultIntent();
    }

    /*
        Sends a JSON list of the ordered pizzas to internet. via POST.
        Set Intent result to OK if POST was successfully sent, otherwise RESULT_CANCEL
     */
    public void sendOrder(View v){

        // Disable button to avoid accidentally sending multiple orders.
        btn_PlaceOrder.setEnabled(false);

       // Resources res = getResources();
        ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
        for (PizzaData.Pizza pizza : PizzaArrayList) {
            if (pizza.getQuantity() > 0) {
                orderedPizzas.add(pizza);
            }
        }

        // Nothing to order.
        if (orderedPizzas.size() < 1){
            nothingOrdered = true;
        }
        else{
            nothingOrdered = false;
        }

        if (isNetworkAvailable(this) && !nothingOrdered){

            RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

            String url = "https://httpbin.org/post";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>(){
                @Override
                public void onResponse(String response){
                    //This code is executed if the server responds, whether or not the response contains data.
                    //The String 'response' contains the server's response.

                    // Order has been placed successfully.
                    setResult(RESULT_OK, resultIntent);

                    //Log.d("POST", "Response: " + response);
                    dialogMessage(res.getString(R.string.txt_error_POSTtitle), response, true);

                    // Enable button for placing new order.
                    btn_PlaceOrder.setEnabled(true);
                }
            }, new Response.ErrorListener(){ //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error){
                    // Code to be executed if there is an error.
                    //Log.d("POST", "Error: " + error);
                    setResult(RESULT_CANCELED, resultIntent);

                    if (nothingOrdered)
                        dialogMessage(res.getString(R.string.txt_error_errorTitle), res.getString(R.string.txt_error_noOrder), false);
                    else
                        dialogMessage(res.getString(R.string.txt_error_errorTitle), res.getString(R.string.txt_error_connError), false);

                    // Enable button for placing new order.
                    btn_PlaceOrder.setEnabled(true);
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();

                    // Create temporary Gson object to handle json creation.
                    Gson gson = new Gson();

                    ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
                    for (PizzaData.Pizza pizza : PizzaArrayList) {
                        if (pizza.getQuantity() > 0) {
                            orderedPizzas.add(pizza);
                        }
                    }

                        // Turn the temporary ArrayList into json
                        String jsonPizzaArrayList = gson.toJson(orderedPizzas);

                        MyData.put(jsonPizzaArrayList, "OrderList");

                        //Log.d("POST", "MyData: " + MyData);
                        return MyData;
                }
            };

            MyRequestQueue.add(MyStringRequest);
        } // End of network available
        else if (nothingOrdered){
            dialogMessage(res.getString(R.string.txt_error_errorTitle), res.getString(R.string.txt_error_noOrder), false);

            // Enable button for placing new order.
            btn_PlaceOrder.setEnabled(true);
        }
        else{
            setResult(RESULT_CANCELED, resultIntent);
            dialogMessage(res.getString(R.string.txt_error_errorTitle), res.getString(R.string.txt_error_connError), false);

            // Enable button for placing new order.
            btn_PlaceOrder.setEnabled(true);
        }

        // Enable button for placing new order.

    }

    /*
        Displays a dialog with the message and OK button which closes the dialog.
        If endActivity is true, end's this activity.
     */
    private void dialogMessage(String title, String message, final boolean endActivity){
        //Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        AlertDialog alertDialog = new AlertDialog.Builder(checkoutActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int x){
                        dialog.dismiss();
                        if (endActivity)
                            finish();
                    }
                });

        alertDialog.show();
    }


    // Returns true if network connection is available, false if there is no connection.
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = false;
        if (connectivity != null)
        {
            /*
            Network[] networks = connectivity.getAllNetworks();
            NetworkInfo infoNet;
            for (Network tempNetwork : networks)
            {
                infoNet = connectivity.getNetworkInfo(tempNetwork);
                if (infoNet.getState() == NetworkInfo.State.CONNECTED)
                {
                    isConnected = true;
                }
            }*/

            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        isConnected= true;
                    }

        }else{
            isConnected= false;
        }

        return isConnected;
    }

    /*
        Calculates new total price and sets it in TextView.
        If no pizza's quantity was changed, changedPizza is null.
     */
    private void onPriceChange(PizzaData.Pizza changedPizza, boolean isIncreased){

        double total_price = 0; // Temporary double variable to calculate the total price in.
        // Go through PizzaArrayList and calculate all the prices and put it in TextView.

        if (PizzaArrayList != null){
            for (PizzaData.Pizza pizza : PizzaArrayList){
                total_price += pizza.getTotalPriceDouble();
            }
        }

        // Set new total price text.
        if (txt_total_price != null)
            txt_total_price.setText(getResources().getString(R.string.txt_checkout_total) + " " + new DecimalFormat("0.00").format(total_price) + " €");

        // Create toast message if changedPizza is not null (a change in pizza was detected).
        /*
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
        */

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

            // Load boolean to detect if data has been sent already.
            data_sent = savedInstanceState.getBoolean("data_sent");

            // Load the string
            String jsonPizzaArrayList = savedInstanceState.getString("pizza_list_json");

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PizzaData.Pizza>>(){}.getType();

            PizzaArrayList = gson.fromJson(jsonPizzaArrayList, type);


            if (mPizzaAdapter != null){
                mPizzaAdapter.clear();
                mPizzaAdapter.addAll(PizzaArrayList);
            }


            // Call function to show the total price.  No change in quantity was made, thus pass in null.
            onPriceChange(null, false);
        }

        // Reconstruct ResultIntent();
        constructResultIntent();
    }

    // Invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Create temporary Gson object to handle json creation.
        Gson gson = new Gson();

        // Get the current selected pizzas from PizzData.pizzas, where all pizzas are stored.
        // Save them in a temporary ArrayList.
        ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
        for (PizzaData.Pizza pizza : PizzaArrayList){
                orderedPizzas.add(pizza);
        }

        // Turn the temporary ArrayList into json
        String jsonPizzaArrayList = gson.toJson(orderedPizzas);

        // Save json string in outState.
        outState.putString("pizza_list_json", jsonPizzaArrayList);

        outState.putBoolean("activityStarted", activityStarted);
        outState.putBoolean("data_Sent", data_sent);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    // Creates new JSON string into intentResult, so it can be passed at anytime.
    public void constructResultIntent(){
        // Create temporary Gson object to handle json creation.
        Gson gson = new Gson();

        // Get the current selected pizzas from PizzData.pizzas, where all pizzas are stored.
        // Save them in a temporary ArrayList.
        ArrayList<PizzaData.Pizza> orderedPizzas = new ArrayList<>();
        for (PizzaData.Pizza pizza : PizzaArrayList){
            if (pizza.getQuantity() > 0){
                orderedPizzas.add(pizza);
            }
        }

        // Turn the temporary ArrayList into json
        String jsonPizzaArrayList = gson.toJson(orderedPizzas);

        // Save json string in resultIntent.
        resultIntent.putExtra("pizza_list_json", jsonPizzaArrayList);

        // By default, RESULT_CANCELED. Only set RESULT_OK if order was sent.
        setResult(RESULT_CANCELED, resultIntent);
    }
}
