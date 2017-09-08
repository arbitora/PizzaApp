package com.example.pizzaapp.pizzaapp;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by AlienNest on 6.9.2017.
 */

public class PizzaData {

    public static class Pizza{
        private String name; // Stores name of the pizza.
        private int toppings_id[]; // Stores the ID's of the toppings.
        private ArrayList<String> toppings; // Stores the name of the toppings
        private double price; // Stores price
        private boolean recommended; // Is the pizza recommended NOT USED CURRENTLY
        private int quantity; // Stores the quantity of the pizza.

        private String total_price;

        // Default constructor
        public Pizza(){
            name = "Null";
            price = 0.00;
            recommended = false;
            quantity = 0;

            toppings = new ArrayList<>();
            total_price = "0.00";
        }

        // Parameter contructor
        public Pizza(String name_, int toppings_id_[], double price_, boolean recommended_){
            name = name_;
            toppings_id = toppings_id_;
            price = price_;
            recommended = recommended_;
            quantity = 0;

            double temp = price * (quantity + 1);
            total_price = new DecimalFormat("#.00").format(temp);
            toppings = new ArrayList<>();
        }

        // Copy constructor
        public Pizza (String name_, int toppings_id_[],  ArrayList<String> toppings_, double price_, boolean recommended_, int quantity_){
            name = name_;
            toppings_id = toppings_id_;
            toppings = toppings_;
            price = price_;
            recommended = recommended_;
            quantity = quantity_;

            double temp = price * (quantity + 1);
            total_price = new DecimalFormat("#.00").format(temp);
        }

        // Returns the name of the pizza.
        public String getName(){
            return name;
        }

        // Returns the toppings of the pizza in a string with each ingredient seperated by commas.
        public String getToppings(){
            String allToppings = ""; // Temporary string, where toppings will be stored.

            // Go through the string array.
            for (int i = 0; i < toppings.size(); i++){
                allToppings += toppings.get(i);

                // If it is not the last topping, add a comma and space.
                if (i != toppings.size() - 1){
                    allToppings += ", ";
                }

            }
            return allToppings;
        }

        // Returns the toppings of the pizza in a string listarray.
        public ArrayList<String> getArrayToppings(){
            return toppings;
        }

        public int[] getToppingsID(){
            return toppings_id;
        }

        // Returns the total price of the pizzas in a string with 2 decimal format.
        public String getTotalPrice(){
            return total_price;
        }

        // Returns the total price of the pizzas in double.
        public double getTotalPriceDouble(){return price * quantity;}

        // Returns the price of the pizza in a string with 2 decimal format.
        public String getPrice(){
            return  new DecimalFormat("#.00").format(price);
        }

        // Returns the price of the pizza in a double format.
        public double getPriceDouble(){
            return price;
        }

        // Return the quantity of the selected pizza.
        public int getQuantity(){
            return quantity;
        }

        // Adds 1 to quantity
        public void addPizza(){
            quantity++;
            double temp = price * (quantity + 1);
            total_price = new DecimalFormat("#.00").format(temp);
        }

        // Reduces 1 quantity
        public void removePizza(){
            quantity--;
            if (quantity < 0)
                quantity = 0;

            double temp = price * (quantity + 1);
            total_price = new DecimalFormat("#.00").format(temp);
        }

        // Puts new int value into pizza's quantity property.
        public void setQuantity(int new_quantity){
            quantity = new_quantity;
        }

        // Custom sorting options
        public static class Comparators
        {
            // Sorts pizzas in the list by their price and name.
            public static Comparator<Pizza> PizzaPrice_PizzaName = new Comparator<Pizza>(){
                @Override
                public int compare(Pizza x, Pizza y){
                    int i = Double.valueOf(x.price).compareTo(Double.valueOf(y.price));
                    if (i == 0){
                        i = x.name.compareTo(y.name);
                    }

                    return i;
                }
            };

            // Sorts pizzas by their quantity in the order, afterwards price and name.
            public static Comparator<Pizza> PizzaQuantity = new Comparator<Pizza>(){
                @Override
                public int compare(Pizza x, Pizza y){
                    int i = Integer.valueOf(x.quantity).compareTo(Integer.valueOf(y.quantity));
                    if (i == 0){
                        i = Double.valueOf(x.price).compareTo(Double.valueOf(y.price));
                        if (i == 0){
                            i = x.name.compareTo(y.name);
                        }
                    }

                    return i;
                }
            };
        }



    }


    // Mock up pizza data.
    public static Pizza pizzas[] = {
            new Pizza ("Sinivalkoinen suomi-pitsa", new int[] {0 , 1, 2}, 6.95, false),
            new Pizza ("Torinon tulinen", new int[] {3, 4, 5, 6}, 8, false),
            new Pizza ("Talon perinteinen", new int[] {7, 1}, 5.5, false),
            new Pizza ("Opera", new int[] {1, 2}, 5.5, false),
            new Pizza ("Frutti di mare", new int[] {1, 8, 6}, 7, false),
            new Pizza ("Bosses Surprise", new int[] {9}, 6.5, false)
    };

    /*
        This gives the toppings for each pizza with the given language.
     */
    public static void setToppings(String[] toppings){
        /*
            0 = Ham
            1 = Shrimp
            2 = Pepperoni
            3 = Salami
            4 = Jalapeno
            5 = Mincemeat
            6 = Olive
            7 = Pineapple
            8 = Tuna
            8 = Anything!
         */

        // For each pizza in the list pizzas...
        for (int i = 0; i < pizzas.length; i++){

            pizzas[i].toppings.clear(); // Clear the current toppings before adding.

            // For each pizza's toppings_ID, add that topping into pizza's string array.
            for (int topping_id : pizzas[i].toppings_id){
                pizzas[i].toppings.add(toppings[topping_id]);
            }
        }
    }
}
