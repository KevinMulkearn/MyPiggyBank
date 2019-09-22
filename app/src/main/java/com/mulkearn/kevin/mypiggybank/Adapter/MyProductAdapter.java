package com.mulkearn.kevin.mypiggybank.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.SkuDetails;
import com.mulkearn.kevin.mypiggybank.Interface.IProductClickListener;
import com.mulkearn.kevin.mypiggybank.MainActivity;
import com.mulkearn.kevin.mypiggybank.R;

import java.util.List;

public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.MyViewHolder> {

    MainActivity mainActivity;
    List<SkuDetails> skuDetailsList;
    BillingClient billingClient;

    public MyProductAdapter(MainActivity mainActivity, List<SkuDetails> skuDetailsList, BillingClient billingClient) {
        this.mainActivity = mainActivity;
        this.skuDetailsList = skuDetailsList;
        this.billingClient = billingClient;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(mainActivity.getBaseContext())
                .inflate(R.layout.layout_product_item,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        myViewHolder.txt_product.setText(skuDetailsList.get(i).getTitle());

        // Product Click
        myViewHolder.setiProductClickListener(new IProductClickListener() {
            @Override
            public void onProductClickListener(View view, int position) {
                // Launch Billing Flow
                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                        .setSkuDetails(skuDetailsList.get(i))
                        .build();
                // TODO: add some response code checks to launchBillingFlow
                billingClient.launchBillingFlow(mainActivity, billingFlowParams);
            }
        });
    }

    @Override
    public int getItemCount() {
        return skuDetailsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_product;

        IProductClickListener iProductClickListener;

        public void setiProductClickListener(IProductClickListener iProductClickListener) {
            this.iProductClickListener = iProductClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_product = (TextView) itemView.findViewById(R.id.txt_product_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iProductClickListener.onProductClickListener(view,getAdapterPosition());
        }
    }
}
