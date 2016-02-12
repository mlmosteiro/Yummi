package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Adapter para las listas de platos de {@code DetailActivityFragment}
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 10/02/2016.
 */
public class AdapterPlatos extends BaseAdapter {
    private int mNumPrimeros;
    private int mNumSegundos;

    private long mApertura;
    private long mCierre;
    private long mIni;
    private long mFin;
    private String mContacto;
    private String mDir;
    private Context mContext;
    private Cursor mCursor;
    private int mRowIDColumn;

    private static final int TYPE_INFO = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_PLATO = 2;

    public AdapterPlatos(Context context) {
        mContext = context;
        mRowIDColumn = -1;
        mNumPrimeros = mNumSegundos = 0;
    }

    /**
     * Cache con los View hijos para un elemento de una lista de platos
     */
    public static class ViewHolderPlatos {
        public final TextView platoView;
        public final TextView descripcionView;

        public ViewHolderPlatos(View view){
            platoView = (TextView) view.findViewById(R.id.plato_textView);
            descripcionView = (TextView) view.findViewById(R.id.descripcionPlato_textView);
        }
    }
    public static class ViewHolderHeader {
        public final TextView headerView;

        public ViewHolderHeader(View view){
            headerView = (TextView) view.findViewById(R.id.cabecera_text_view);
        }
    }
    public static class ViewHolderInfo {
        public TextView mViewHoraApertura;
        public TextView mViewHoraComida;
        public TextView mViewContacto;
        public TextView mViewUbicacion;

        public ViewHolderInfo(View view) {
            this.mViewHoraApertura = (TextView) view.findViewById(R.id.hora_apertura_textview);
            this.mViewHoraComida =  (TextView) view.findViewById(R.id.hora_comidas_textview);
            this.mViewContacto =  (TextView) view.findViewById(R.id.contacto_textview);
            this.mViewUbicacion = (TextView) view.findViewById(R.id.ubicacion_link);
        }
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (newCursor.moveToFirst()) {
                while (!newCursor.isAfterLast()) {
                    int tipo = Integer.parseInt(
                            newCursor.getString(DetailActivityFragment.COL_PLATO_TIPO)
                                    .substring(0, 1));
                    switch (tipo) {
                        case 0:
                            mNumPrimeros++;
                            break;
                        case 1:
                            mNumSegundos++;
                    }
                    newCursor.moveToNext();
                }
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            // notify the observers about the new cursor
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            // notify the observers about the lack of a data set
            notifyDataSetInvalidated();
        }
    }

    public void setInfoComedor(Cursor data) {
        if(data.moveToFirst()) {
            mApertura = data.getLong(DetailActivityFragment.COL_COMEDOR_APERTURA);
            mCierre = data.getLong(DetailActivityFragment.COL_COMEDOR_CIERRE);
            mIni = data.getLong(DetailActivityFragment.COL_COMEDOR_HORA_INI);
            mFin = data.getLong(DetailActivityFragment.COL_COMEDOR_HORA_FIN);
            String contacto = data.getString(DetailActivityFragment.COL_COMEDOR_NOMBRE_CONTACTO);
            mContacto = data.getString(DetailActivityFragment.COL_COMEDOR_TLFN) +
                    (!contacto.equals("null") ? " (" + contacto + ")" : "");
            mDir = data.getString(DetailActivityFragment.COL_COMEDOR_DIR);
            notifyDataSetInvalidated();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) return TYPE_INFO;
        else if(position == 1 || position == mNumPrimeros + 2 ||
                position == mNumPrimeros+mNumSegundos+3)
            return TYPE_HEADER;
        else
            return TYPE_PLATO;
    }

    @Override
    public int getCount() {
        if(mCursor != null) {
            return mCursor.getCount() + 4;
        } else {
            return 4;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(getItemViewType(position) == TYPE_PLATO) {
            int posicionEnCursor = position-2;
            if (posicionEnCursor >= mNumPrimeros) posicionEnCursor--;
            if (posicionEnCursor >= mNumSegundos+mNumPrimeros) posicionEnCursor--;
            if (!mCursor.moveToPosition(posicionEnCursor)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
        }

        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent, position);
        } else {
            switch(getItemViewType(position)) {
                case TYPE_HEADER:
                    if(convertView.getId() == R.id.cabecera_text_view)
                        v = convertView;
                    else
                        v = newView(mContext, mCursor, parent, position);
                    break;
                case TYPE_PLATO:
                    if(convertView.getId() == R.id.list_item_platos)
                        v = convertView;
                    else
                        v = newView(mContext, mCursor, parent, position);
                    break;
                default:
                    if(convertView.getId() == R.id.content_detalles)
                        v = convertView;
                    else
                        v = newView(mContext, mCursor, parent, position);
            }
        }
        bindView(v, mContext, mCursor, position);
        return v;
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent, int position) {
        View view = null;
        switch(getItemViewType(position)) {
            default: {
                view = LayoutInflater.from(context).inflate(R.layout.list_item_platos, parent, false);

                ViewHolderPlatos viewHolderPlatos = new ViewHolderPlatos(view);
                view.setTag(viewHolderPlatos);
                break;
            }
            case TYPE_HEADER: {
                view = LayoutInflater.from(context).inflate(R.layout.content_cabecera, parent, false);

                ViewHolderHeader viewHolderHeader = new ViewHolderHeader(view);
                view.setTag(viewHolderHeader);
                break;
            }
            case TYPE_INFO: {
                view = LayoutInflater.from(context).inflate(R.layout.content_detalles, parent, false);

                ViewHolderInfo viewHolderInfo = new ViewHolderInfo(view);
                view.setTag(viewHolderInfo);
                break;
            }
        }
        return view;
    }

    private String getHeader(int position) {
        if(position == 1) {
            return mContext.getString(R.string.header_primeros);
        } else if(position == mNumPrimeros + 2) {
            return mContext.getString(R.string.header_segundos);
        } else if(position == mNumPrimeros+mNumSegundos+3) {
            return mContext.getString(R.string.header_postres);
        }
        return "No sé";
    }

    public void bindView(View view, Context context, Cursor cursor, int position) {
        switch(getItemViewType(position)) {
            case TYPE_PLATO: {
                ViewHolderPlatos vH = (ViewHolderPlatos) view.getTag();

                vH.platoView.setText(cursor.getString(DetailActivityFragment.COL_PLATO_NOMBRE));
                vH.descripcionView.setText(cursor.getString(DetailActivityFragment.COL_PLATO_DESCRIPCION));
                break;
            }
            case TYPE_HEADER: {
                ViewHolderHeader vH = (ViewHolderHeader) view.getTag();
                vH.headerView.setText(getHeader(position));
                break;
            }
            case TYPE_INFO: {
                ViewHolderInfo vH = (ViewHolderInfo) view.getTag();
                vH.mViewHoraApertura.setText(
                            mContext.getString(R.string.formato_horario_apertura,
                                    Utility.denormalizarHora(mApertura),
                                    Utility.denormalizarHora(mCierre)));
                vH.mViewHoraComida.setText(
                            mContext.getString(R.string.formato_horario_apertura,
                                    Utility.denormalizarHora(mIni),
                                    Utility.denormalizarHora(mFin)));
                vH.mViewContacto.setText(mContacto);
                vH.mViewUbicacion.setText(mDir);
            }
        }
    }
}
