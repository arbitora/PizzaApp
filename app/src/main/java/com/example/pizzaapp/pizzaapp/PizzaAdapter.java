package com.example.pizzaapp.pizzaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by AlienNest on 6.9.2017.
 */

public class PizzaAdapter extends ArrayAdapter<PizzaData.Pizza> {

    private ArrayList<PizzaData.Pizza> listSet;
    Context mContext;

    public interface PizzaQuantityListener {
        public void onPizzaQuantityChange(Context context, PizzaData.Pizza changed, boolean isIncreased);
    }

    private PizzaQuantityListener listener;

    // Adapter creation.
    public PizzaAdapter(ArrayList<PizzaData.Pizza> inputList, Context context){
        super(context, R.layout.pizza_list_item, inputList);
        this.listSet = inputList;
        this.mContext = context;
        this.listener = null;
    }

    public void setPizzaQuantityListener(PizzaQuantityListener listener){
        this.listener = listener;
    }

    // ViewHolder class for caching, avoids finding components multiple times.
    private static class ViewHolder {
        TextView txt_Name;
        TextView txt_Toppings;
        TextView txt_Price;
        TextView txt_Quantity;

        Button btn_Order;
        Button btn_Remove;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PizzaData.Pizza selectedPizza = getItem(position);
        //*

        ViewHolder viewHolder; // View lookup cache stored in tag.

        // Declare viewHolder for first run.
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.pizza_list_item, parent, false);

            // Bind components into viewHolder class.
            viewHolder = new ViewHolder();
            viewHolder.txt_Name = (TextView) convertView.findViewById(R.id.txtView_PizzaName);
            viewHolder.txt_Toppings = (TextView) convertView.findViewById(R.id.txtView_Toppings);
            viewHolder.txt_Price = (TextView) convertView.findViewById(R.id.txtView_Price);
            viewHolder.txt_Quantity = (TextView) convertView.findViewById(R.id.txtView_Quantity);
            viewHolder.btn_Order = (Button) convertView.findViewById(R.id.BTN_Checkout);
            viewHolder.btn_Remove = (Button) convertView.findViewById(R.id.BTN_Remove);



            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }



        // If selected Pizza has not been added to order, disable remove button.
        if (selectedPizza.getQuantity() == 0)
            viewHolder.btn_Remove.setEnabled(false);
        else
            viewHolder.btn_Remove.setEnabled(true);
        /*
            Button onClick event handlers are declared here.
            They need to be declared here in order for them to being bound properly
            for each listItem, otherwise they might bind to incorrect rows.
         */


        // Add pizza to customer's order.
        viewHolder.btn_Order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listSet.get(position).addPizza();
                notifyDataSetChanged(); // Refresh ListView

                // Enable remove button when quantity changes to 1.
                // Check only at 1 to avoid calling the functions multiple times.
                if (listSet.get(position).getQuantity() == 1){
                    // Find the Remove button from the view.
                    LinearLayout vwParentLayout = (LinearLayout)v.getParent();
                    // Remove Button is the 2nd child (object) in the parent view.
                    Button btn_child_remove = (Button)vwParentLayout.getChildAt(1);
                    if (btn_child_remove != null)
                        btn_child_remove.setEnabled(true);
                }

                listener.onPizzaQuantityChange(mContext, listSet.get(position), true);
            }
        });

        // Remove pizza from customer's order.
        viewHolder.btn_Remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listSet.get(position).removePizza();
                notifyDataSetChanged(); // Refresh ListView

                // Disable Remove button incase quantity goes to 0.
                if (listSet.get(position).getQuantity() < 1){

                    // Find the Remove button from the view.
                    LinearLayout vwParentLayout = (LinearLayout)v.getParent();
                    // Remove Button is the 2nd child (object) in the parent view.
                    Button btn_child_remove = (Button)vwParentLayout.getChildAt(1);
                    if (btn_child_remove != null)
                        btn_child_remove.setEnabled(false);
                }

                listener.onPizzaQuantityChange(mContext, listSet.get(position), false);
            }
        });

        viewHolder.txt_Name.setText(selectedPizza.getName());
        viewHolder.txt_Toppings.setText(selectedPizza.getToppings());
        viewHolder.txt_Quantity.setText(mContext.getResources().getString(R.string.txt_desc_quantity) + " - " + selectedPizza.getQuantity());
        viewHolder.txt_Price.setText(getContext().getResources().getString(R.string.txt_desc_price) + " " + selectedPizza.getPrice() + " €");


        // Return the completed view to render on screen
        return convertView;
    }
}
