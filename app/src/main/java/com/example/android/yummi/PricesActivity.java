package com.example.android.yummi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class PricesActivity extends AppCompatActivity {


    public static final String PROMO_COMEDOR = "promo";
    public static final String ID_COMEDOR = "ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) { //
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        String comedorPromo = (String)  getIntent().getExtras().get(PROMO_COMEDOR);
        Long comedorID = (Long)  getIntent().getExtras().get(ID_COMEDOR);
        PricesActivityFragment pricesActivityFragment = new PricesActivityFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(ID_COMEDOR, comedorID);
        bundle.putString(PROMO_COMEDOR, comedorPromo);

        pricesActivityFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(
                R.id.prices_container, pricesActivityFragment).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
