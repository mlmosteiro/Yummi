package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AdapterMenu extends RecyclerView.Adapter{
    private static final int TYPE_TABLE_HEADER = 0;
    private static final int TYPE_MENU = 1;
    private static final int TYPE_PROMO = 2;

    private int mNumMenus;
    private Context mContext;
    private Cursor mCursor;
    private String mPromo;
    Map<Long, Cursor> cursoresElementos;


    public AdapterMenu(Context context, String promo) {
        mContext = context;
        mPromo = promo;
        cursoresElementos = new HashMap<>();
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

        public ViewHolderMenuItem(View view){
            super(view);
            mViewMenuNombre = (TextView) view.findViewById(R.id.menu_name);
            mViewMenuPrecio = (TextView) view.findViewById(R.id.menu_price);
            mViewMenuElementos = (TextView) view.findViewById(R.id.menu_elements);
        }
    }

    public void setElementosMenu(long id, Cursor c) {
        cursoresElementos.put(id, c);
        notifyDataSetChanged();
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        mCursor = newCursor;
        if (newCursor != null) {
            mNumMenus = 0;
            if (newCursor.moveToFirst()) {
                while (!newCursor.isAfterLast()) {
                    mNumMenus ++;
                    newCursor.moveToNext();
                }
            }
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
                    int posicionEnCursor = position-1;
                    if (!mCursor.moveToPosition(posicionEnCursor)) {
                        throw new IllegalStateException("couldn't move cursor to position " + position + "(" + posicionEnCursor + " relamente)");
                    }
                    vH.mViewMenuNombre.setText(mCursor.getString(PricesActivityFragment.COL_MENU_NOMBRE));
                    long id = mCursor.getLong(PricesActivityFragment.COL_MENU_ID);
                    Cursor elemCursor = cursoresElementos.get(id);
                    if(elemCursor != null && elemCursor.moveToFirst()) {
                        ArrayList<String> elemList = new ArrayList<>(elemCursor.getCount());
                        while(!elemCursor.isAfterLast()) {
                            elemList.add(elemCursor.getString(PricesActivityFragment.COL_ELEM_NOMBRE));
                            elemCursor.moveToNext();
                        }
                        vH.mViewMenuElementos.setText(TextUtils.join(", ", elemList));
                    }
                    vH.mViewMenuPrecio.setText(
                            mContext.getString(R.string.formato_dinero,
                            mCursor.getString(PricesActivityFragment.COL_MENU_PRECIO)));
                    break;
                }
                case TYPE_TABLE_HEADER: {
                    ViewHolderMenuItem vH = (ViewHolderMenuItem) holder;
                    vH.mViewMenuNombre.setText(R.string.menu_label);
                    vH.mViewMenuPrecio.setText(R.string.precio_label);
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
        } else if (position <= mNumMenus)
            return TYPE_MENU;
        else
            return TYPE_PROMO;
    }

    @Override
    public int getItemCount() {
        if(mCursor != null ) {
            return mCursor.getCount() + 2;
        }else {
            return 2;
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }




}


