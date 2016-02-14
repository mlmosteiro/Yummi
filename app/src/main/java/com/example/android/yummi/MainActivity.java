package com.example.android.yummi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback {

    private boolean twoPane;
    private static final String DETAILACTIVITYFRAGMENT_TAG = "DAFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.detail_container) != null) {
            twoPane = true;

            if (savedInstanceState == null) {
               ningunComedorSeleccionado();
            }
        } else {
            twoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_log_base) {
            Utility.logearBase(this);
            return true;
        } else if (id == R.id.action_aboutUs) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void comedorSeleccionado(long comedorId, String comedorName, String promo) {
        if (twoPane){
            DetailActivityFragment detailFragment = new DetailActivityFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(DetailActivityFragment.COMEDOR_ID,comedorId );
            bundle.putString(DetailActivityFragment.COMEDOR_NOMBRE, comedorName);
            bundle.putBoolean(DetailActivityFragment.COMEDOR_TWOPANE, twoPane);
            bundle.putString(DetailActivityFragment.COMEDOR_PROMO, promo);
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.detail_container, detailFragment,
                    DETAILACTIVITYFRAGMENT_TAG).commit();
        } else {
            Intent intent = new Intent( this, DetailActivity.class);
            intent.putExtra(DetailActivity.ID_COMEDOR, comedorId);
            intent.putExtra(DetailActivity.NOMBRE_COMEDOR, comedorName);
            intent.putExtra(DetailActivityFragment.COMEDOR_PROMO, promo);
            startActivity(intent);
        }
    }

    @Override
    public void ningunComedorSeleccionado() {
        NotSelectedFragment notSelectedFragment = new NotSelectedFragment();

        getSupportFragmentManager().beginTransaction().add(
                R.id.detail_container,notSelectedFragment , NotSelectedFragment.NOTSELECTED_TAG).commit();

    }
}
