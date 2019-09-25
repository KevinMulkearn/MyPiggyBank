package com.mulkearn.kevin.mypiggybank;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.mulkearn.kevin.mypiggybank.Adapter.MyProductAdapter;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    BillingClient billingClient;
    SharedPreferences sharedPref;
    DialogInterface.OnClickListener dialogClickListener;

    Button loadProduct;
    TextView balanceDisplay;
    RecyclerView recyclerProduct;

    static int balance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBillingClient();

        // Set balance amount
        sharedPref = getSharedPreferences("balance_sum", MODE_PRIVATE);
        balance = sharedPref.getInt("sum", 0);

        // View setup
        loadProduct = (Button)findViewById(R.id.btn_load_product);
        balanceDisplay = (TextView)findViewById(R.id.balance_display);
        recyclerProduct = (RecyclerView)findViewById(R.id.recycler_product);
        recyclerProduct.setHasFixedSize(true);
        recyclerProduct.setLayoutManager(new LinearLayoutManager(this));
        balanceDisplay.setText("Balance: €" + balance);

        // Button click listener
        loadProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(billingClient.isReady()){
                    SkuDetailsParams params = SkuDetailsParams.newBuilder()
                            .setSkusList(Arrays.asList("one_euro","ten_euro"))
                            .setType(BillingClient.SkuType.INAPP) // If we add from managed product, this is in app, else is SUB
                    .build();

                    billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                            if(responseCode == BillingClient.BillingResponse.OK){
                                loadProductToRecyclerView(skuDetailsList);
                            } else {
                                Toast.makeText(MainActivity.this, "Cannot Query Product", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Billing Client Not Ready", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Dialog pop-up click listener
        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked, reset balance to zero
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.clear();
                        editor.apply();
                        balance = 0;
                        balanceDisplay.setText("Balance: €" + balance);
                        Toast.makeText(MainActivity.this, "Reset", Toast.LENGTH_SHORT).show();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked, do nothing
                        break;
                }
            }
        };
    }

    private void loadProductToRecyclerView(List<SkuDetails> skuDetailsList) {
        MyProductAdapter adapter = new MyProductAdapter(this, skuDetailsList, billingClient);
        recyclerProduct.setAdapter(adapter);
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this).setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if(responseCode == BillingClient.BillingResponse.OK){
                    Toast.makeText(MainActivity.this, "Success to connect to Billing", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, ""+responseCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Toast.makeText(MainActivity.this, "You are disconnected from Billing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // After launchBillingFlow(), called by Google Play
    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        // Here, if user click to TAP-BUY, we will retrieve data here
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                // Purchase must be consumed to repurchase
                Toast.makeText(MainActivity.this, "Purchased: " + purchase.getSku(), Toast.LENGTH_SHORT).show();
                if (purchase.getSku().equals("one_euro")){
                    balance += 1;
                } else if (purchase.getSku().equals("ten_euro")) {
                    balance += 10;
                }
                billingClient.consumeAsync(purchase.getPurchaseToken(), new ConsumeResponseListener() {
                    @Override
                    public void onConsumeResponse(int responseCode, String purchaseToken) {
                        Toast.makeText(MainActivity.this, "Clearing Token: " + purchaseToken, Toast.LENGTH_SHORT).show();
                    }
                });
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("sum", balance);
                editor.apply();
                balanceDisplay.setText("Balance: €" + balance);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
        } else {
            // Handle any other error codes.
            Toast.makeText(MainActivity.this, "Error Code: " + responseCode, Toast.LENGTH_SHORT).show();
        }

    }

    public void ResetBalance(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Reset Balance?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }
}
