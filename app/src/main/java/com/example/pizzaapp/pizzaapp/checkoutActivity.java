package com.example.pizzaapp.pizzaapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pizzaapp.pizzaapp.PizzaAdapter.PizzaQuantityListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class checkoutActivity extends AppCompatActivity {

    private ArrayList<PizzaData.Pizza> PizzaArrayList; // Has the full list of all the pizzas
    private static PizzaAdapter mPizzaAdapter;

    private ListView pizzaListView;
    private Button btn_PlaceOrder;
    private TextView txt_total_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        PizzaArrayList = new ArrayList<>();

        // Get data from intent.
        if (getIntent() != null){

            ArrayList<PizzaData.Pizza> deleteList = new ArrayList<>(); // Lists Pizza objects we do not want in the main list.

            String jsonPizzaArrayList = getIntent().getStringExtra("pizza_list_json");

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PizzaData.Pizza>>(){}.getType();

            PizzaArrayList = gson.fromJson(jsonPizzaArrayList, type);
            for (PizzaData.Pizza pizza : PizzaArrayList){

                // Find each pizza that has quantity of 0 and add it to deleteList
                if (pizza.getQuantity() == 0)
                    deleteList.add(pizza);
            }

            // Iterate through deleteList and delete each pizza from the actualy PizzaArrayList.
            for (PizzaData.Pizza pizza : deleteList){
                PizzaArrayList.remove(pizza);
            }
        }


        // Bind components to variables.
        pizzaListView = (ListView) findViewById(R.id.listPizzaView);
        btn_PlaceOrder = (Button) findViewById(R.id.BTN_Order);
        txt_total_price = (TextView) findViewById(R.id.txtView_Total);


        // Bind ArrayList to adapter and set the adapter to ListView.
        mPizzaAdapter = new PizzaAdapter(PizzaArrayList, getApplicationContext());
        mPizzaAdapter.setPizzaQuantityListener(new PizzaQuantityListener(){
            @Override
            public void onPizzaQuantityChange(Context context, PizzaData.Pizza changedPizza, boolean isIncreased){
                double total_price = 0;
                for (PizzaData.Pizza pizza : PizzaArrayList){
                    total_price += pizza.getTotalPriceDouble();
                }

                txt_total_price.setText(getResources().getString(R.string.txt_checkout_total) + " " + new DecimalFormat("#.00").format(total_price) + " €");

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
    }

    public void sendOrder(View v){
        // TODO: Send post data.
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


}
