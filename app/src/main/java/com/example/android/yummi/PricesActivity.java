package com.example.android.yummi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_aboutUs) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
