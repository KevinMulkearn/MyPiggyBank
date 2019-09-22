package com.mulkearn.kevin.mypiggybank;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.mulkearn.kevin.mypiggybank.Adapter.MyProductAdapter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    BillingClient billingClient;

    Button loadProduct;
    RecyclerView recyclerProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupBillingClient();

        // View
        loadProduct = (Button)findViewById(R.id.btn_load_product);
        recyclerProduct = (RecyclerView)findViewById(R.id.recycler_product);
        recyclerProduct.setHasFixedSize(true);
        recyclerProduct.setLayoutManager(new LinearLayoutManager(this));

        // Event
        loadProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(billingClient.isReady()){
                    SkuDetailsParams params = SkuDetailsParams.newBuilder()
                            .setSkusList(Arrays.asList("deposit_one_euro","deposit_ten_euro"))
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
            Toast.makeText(MainActivity.this, "Purchase item: "+purchases.size(), Toast.LENGTH_SHORT).show();
//            for (Purchase purchase : purchases) {
//                handlePurchase(purchase);
//            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(MainActivity.this, "User Canceled", Toast.LENGTH_SHORT).show();
        } else {
            // Handle any other error codes.
            Toast.makeText(MainActivity.this, "Error Code: " +responseCode, Toast.LENGTH_SHORT).show();
        }

    }
}
