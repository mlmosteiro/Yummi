package com.example.android.yummi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.android.yummi.services.ComedoresService;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    public static final String ID_COMEDOR = "id";
    public static final String NOMBRE_COMEDOR = "nombre";
    public static final String PROMO_COMEDOR = "promo";
    public static final String IMAGENES_PATH = "imagenes";
    public static final String DETAILACTIVITYFRAGMENT_TAG = "DAFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) { // Habilitar up button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        CollapsingToolbarLayout collapser = (CollapsingToolbarLayout) findViewById(R.id.collapser);
        final Long comedorId = (Long) getIntent().getExtras().get(ID_COMEDOR);
        String comedorNombre = (String) getIntent().getExtras().get(NOMBRE_COMEDOR);
        final String comedorPromo = (String) getIntent().getExtras().get(PROMO_COMEDOR);

        if ( comedorId != null){
            DetailActivityFragment detailFragment = new DetailActivityFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(DetailActivityFragment.COMEDOR_ID, comedorId);
            bundle.putString(DetailActivityFragment.COMEDOR_NOMBRE, comedorNombre);
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(
                    R.id.detail_container, detailFragment,
                    DETAILACTIVITYFRAGMENT_TAG).commit();
            Uri uri = Uri.parse(ComedoresService.API_DIR).buildUpon()
                    .appendPath(IMAGENES_PATH)
                    .appendQueryParameter("id", Long.toString(comedorId))
                    .build();

            Picasso.with(this)
                    .load(uri)
                    .placeholder(R.drawable.comedor_placeholder)
                    .into((ImageView)findViewById(R.id.image_paralax));
        } else{
            NotSelectedFragment notSelectedFragment = new NotSelectedFragment();
            getSupportFragmentManager().beginTransaction().add(
                    R.id.detail_container, notSelectedFragment,
                    NotSelectedFragment.NOTSELECTED_TAG).commit();
        }

        if( comedorNombre != null){
            collapser.setTitle( comedorNombre );
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(DetailActivity.this, PricesActivity.class);
                        intent.putExtra(PricesActivity.ID_COMEDOR, comedorId);
                        intent.putExtra(PricesActivity.PROMO_COMEDOR, comedorPromo);
                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_aboutUs) {
            startActivity(new Intent(this, AboutUsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}







