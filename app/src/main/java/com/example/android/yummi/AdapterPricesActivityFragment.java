package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class AdapterPricesActivityFragment extends RecyclerView.Adapter{
    private static final int TYPE_TABLE_HEADER = 0;
    private static final int TYPE_MENU = 1;
    private static final int TYPE_PROMO = 2;

    private Context mContext;
    private ArrayList<Menu> mMenus;
    private String mPromo;

    private static class Menu {
        public String nombre;
        public int id;
        public float precio;
        public ArrayList<String> elementos;

        public Menu(int id, String nombre, float precio) {
            this.nombre = nombre;
            this.precio = precio;
            this.id = id;
            elementos = new ArrayList();
        }
    }

    public AdapterPricesActivityFragment(Context context, String promo) {
        mContext = context;
        mPromo = promo;
        mMenus = new ArrayList();
    }

    public static class ViewHolderPromo extends RecyclerView.ViewHolder {
        public TextView mViewPromo;

        public ViewHolderPromo(View view){
            super(view);
            mViewPromo = (TextView) view.findViewById(R.id.promo_textView);
        }
    }

    public static class ViewHolderMenuItem extends RecyclerView.ViewHolder {
        public TextView mViewMenuNombre;
        public  TextView mViewMenuPrecio;
        public  TextView mViewMenuElementos;
        public View mGeneralView;

        public ViewHolderMenuItem(View view){
            super(view);
            mViewMenuNombre = (TextView) view.findViewById(R.id.menu_name);
            mViewMenuPrecio = (TextView) view.findViewById(R.id.menu_price);
            mViewMenuElementos = (TextView) view.findViewById(R.id.menu_elements);
            mGeneralView = view;
        }
    }

    public void cargarCursor(Cursor cursor) {
        // Let's go
        if (cursor != null) {
            // Limpiamos el arrayList de menus
            mMenus.clear();

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    int id = cursor.getInt(PricesActivityFragment.COL_MENU_ID);
                    Menu men = null;

                    // Comprobamos que no tenemos ya ese id, recorremos en orden inverso
                    // porque de estar repetido es probable que el último sea la repetición
                    for(int i=mMenus.size()-1; i >= 0; i--) {
                        if(mMenus.get(i).id == id) {
                            men = mMenus.get(i);
                            break;
                        }
                    }

                    // Si es nuevo, lo creamos y añadimos al array
                    if( men == null ) {
                        String nombre = cursor.getString(PricesActivityFragment.COL_MENU_NOMBRE);
                        float precio = cursor.getFloat(PricesActivityFragment.COL_MENU_PRECIO);
                        men = new Menu(id, nombre, precio);
                        mMenus.add(men);
                    }

                    // Añadimos el elemento correspondiente
                    String elemento = cursor.getString(PricesActivityFragment.COL_ELEM_NOMBRE);
                    men.elementos.add(elemento);

                    // NEXT!
                    cursor.moveToNext();
                }
            }
            // Notificamos a los observadores
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        switch(viewType) {
            case TYPE_MENU: {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item_menus, viewGroup, false);
                return new ViewHolderMenuItem(view);
            }
            case  TYPE_TABLE_HEADER: {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_item_menus,viewGroup, false);
                return new ViewHolderMenuItem(view);
            }
            case TYPE_PROMO:{
                view = LayoutInflater.from(mContext).inflate(R.layout.content_promo, viewGroup, false);
                return  new ViewHolderPromo(view);
            }

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder != null) {
            int type = getItemViewType(position);
            switch (type) {
                case TYPE_MENU: {
                    ViewHolderMenuItem vH = (ViewHolderMenuItem) holder;

                    // El primer hueco corresponde a la cabecera de la tabla
                    Menu m = mMenus.get(position-1);

                    // Ponemos texto de nombre del menu
                    vH.mViewMenuNombre.setText(m.nombre);
                    // Ponemos los elementos del menu
                    String elems = TextUtils.join(", ", m.elementos);
                    vH.mViewMenuElementos.setText(Character.toUpperCase(elems.charAt(0)) + elems.substring(1));
                    // Ponemos el precio del menu
                    vH.mViewMenuPrecio.setText(
                            mContext.getString(R.string.formato_dinero, m.precio));

                    // Dos colorsitos de fondo
                    // TODO: mover estos colores a colors.xml
                    if(position%2 == 0) {
                        vH.mGeneralView.setBackgroundColor(Color.parseColor("#e5e4e5"));
                    } else{
                        vH.mGeneralView.setBackgroundColor(Color.parseColor("#c1bfc1"));
                    }

                    break;
                }
                case TYPE_TABLE_HEADER: {
                    ViewHolderMenuItem vH = (ViewHolderMenuItem) holder;
                    vH.mViewMenuNombre.setText(R.string.menu_label);
                    vH.mViewMenuPrecio.setText(R.string.precio_label);
                    vH.mViewMenuPrecio.setTextSize(20);
                    vH.mViewMenuNombre.setTextSize(20);
                    vH.mViewMenuNombre.setPadding(0,0,0,0);
                    vH.mViewMenuPrecio.setPadding(0,0,0,0);
                    vH.mViewMenuPrecio.setTypeface(null, Typeface.BOLD);
                    vH.mViewMenuNombre.setTextSize(20);

                    break;
                }
                case TYPE_PROMO: {
                    ViewHolderPromo vH = (ViewHolderPromo) holder;
                    vH.mViewPromo.setText(mPromo);
                    break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return TYPE_TABLE_HEADER;
        } else if (position <= mMenus.size())
            return TYPE_MENU;
        else
            return TYPE_PROMO;
    }

    @Override
    public int getItemCount() {
        return mMenus.size() + 2;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0)
            return -1;
        else if (position <= mMenus.size() )
            return mMenus.get(position-1).id;
        else
            return -1;
    }
}


