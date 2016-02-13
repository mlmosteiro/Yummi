package com.example.android.yummi;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Adapter para las listas de platos de {@code DetailActivityFragment}
 * Created by David Campos Rodríguez <a href='mailto:david.campos@rai.usc.es'>david.campos@rai.usc.es</a> on 10/02/2016.
 */
public class AdapterPlatos extends  RecyclerView.Adapter{
    private int mNumPrimeros;
    private int mNumSegundos;

    private long mApertura;
    private long mCierre;
    private long mIni;
    private long mFin;
    private double mLat;
    private double mLon;
    private String mContacto;
    private String mDir;
    private String mNombre;
    private Context mContext;
    private AbridorLocalizacion mAbridor;
    private Cursor mCursor;

    private static final int TYPE_INFO = 0;
    private static final int TYPE_HEADER = 1;
    private static final int TYPE_PLATO = 2;

    public AdapterPlatos(Context context, AbridorLocalizacion abridor) {
        mContext = context;
        mAbridor = abridor;
        mNumPrimeros = mNumSegundos = 0;
    }

    /**
     * Cache con los View hijos para un elemento de una lista de platos
     */

    public static class ViewHolderHeader extends RecyclerView.ViewHolder  {
        public final TextView headerView;

        public ViewHolderHeader(View view){
            super(view);
            headerView = (TextView) view.findViewById(R.id.cabecera_text_view);
        }
    }

    public static class ViewHolderInfo extends RecyclerView.ViewHolder {
        public TextView mViewHoraApertura;
        public TextView mViewHoraComida;
        public TextView mViewContacto;
        public TextView mViewUbicacion;

        public ViewHolderInfo(View view) {
            super(view);
            this.mViewHoraApertura = (TextView) view.findViewById(R.id.hora_apertura_textview);
            this.mViewHoraComida =  (TextView) view.findViewById(R.id.hora_comidas_textview);
            this.mViewContacto =  (TextView) view.findViewById(R.id.contacto_textview);
            this.mViewUbicacion = (TextView) view.findViewById(R.id.ubicacion_link);
        }
    }

    public static class ViewHolderPlatos extends RecyclerView.ViewHolder {
        public final TextView platoView;
        public final TextView descripcionView;

        public ViewHolderPlatos(View view) {
            super(view);
            platoView = (TextView) view.findViewById(R.id.plato_textView);
            descripcionView = (TextView) view.findViewById(R.id.descripcionPlato_textView);
        }
    }

    public void swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return;
        }
        mCursor = newCursor;
        if (newCursor != null) {
            mNumPrimeros = mNumSegundos = 0;
            if (newCursor.moveToFirst()) {
                while (!newCursor.isAfterLast()) {
                    Log.d("TIPO", newCursor.getString(DetailActivityFragment.COL_PLATO_TIPO));
                    int tipo = Integer.parseInt(
                            newCursor.getString(DetailActivityFragment.COL_PLATO_TIPO)
                                    .substring(0, 1));
                    switch (tipo) {
                        case 0:
                            mNumPrimeros++;
                            break;
                        case 1:
                            mNumSegundos++;
                            break;
                        case 2: break;
                        default:
                            throw new IllegalStateException("Qué cojones? " + tipo);
                    }
                    newCursor.moveToNext();
                }
            }
            Log.d("SWAP", mNumPrimeros + " - " + mNumSegundos);
            Log.d("NUM", "Num " + newCursor.getCount());
            // notify the observers about the new cursor
            notifyDataSetChanged();
        }
    }

    public void setInfoComedor(Cursor data) {
        if(data.moveToFirst()) {
            mApertura = data.getLong(DetailActivityFragment.COL_COMEDOR_APERTURA);
            mCierre = data.getLong(DetailActivityFragment.COL_COMEDOR_CIERRE);
            mIni = data.getLong(DetailActivityFragment.COL_COMEDOR_HORA_INI);
            mFin = data.getLong(DetailActivityFragment.COL_COMEDOR_HORA_FIN);
            mLat = data.getDouble(DetailActivityFragment.COL_COMEDOR_LAT);
            mLon = data.getDouble(DetailActivityFragment.COL_COMEDOR_LONG);
            String contacto = data.getString(DetailActivityFragment.COL_COMEDOR_NOMBRE_CONTACTO);
            mContacto = data.getString(DetailActivityFragment.COL_COMEDOR_TLFN) +
                    (!contacto.equals("null") ? " (" + contacto + ")" : "");
            mDir = data.getString(DetailActivityFragment.COL_COMEDOR_DIR);
            mNombre = data.getString(DetailActivityFragment.COL_COMEDOR_NOMBRE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
     View view;
        switch(viewType) {
            case TYPE_HEADER: {
                view = LayoutInflater.from(mContext).inflate(R.layout.content_cabecera, viewGroup, false);
                return new ViewHolderHeader(view);
            }
            case TYPE_INFO: {
                view = LayoutInflater.from(mContext).inflate(R.layout.content_detalles, viewGroup, false);
                return new ViewHolderInfo(view);
            }
            default: {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_platos, viewGroup, false);
                return new ViewHolderPlatos(view);
            }
        }
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
    public int getItemCount() {
        if(mCursor != null) {
            return mCursor.getCount() + 4;
        } else {
            return 4;
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position   ) {
        if(holder!=null) {
            int type = getItemViewType(position);
            switch (type) {
                case TYPE_PLATO: {
                    ViewHolderPlatos vH = (ViewHolderPlatos)holder;
                    int posicionEnCursor = position-2;
                    if (posicionEnCursor >= mNumPrimeros) posicionEnCursor--;
                    if (posicionEnCursor >= mNumSegundos+mNumPrimeros) posicionEnCursor--;
                    if (!mCursor.moveToPosition(posicionEnCursor)) {
                        throw new IllegalStateException("couldn't move cursor to position " + position + "(" + posicionEnCursor + " relamente)");
                    }
                    vH.platoView.setText(mCursor.getString(DetailActivityFragment.COL_PLATO_NOMBRE));
                    vH.descripcionView.setText(mCursor.getString(DetailActivityFragment.COL_PLATO_DESCRIPCION));
                    break;
                }
                case TYPE_HEADER: {
                    ViewHolderHeader vH = (ViewHolderHeader) holder;
                    vH.headerView.setText(getHeader(position));
                    break;
                }
                case TYPE_INFO: {
                    ViewHolderInfo vH = (ViewHolderInfo)holder;
                    vH.mViewHoraApertura.setText(
                            mContext.getString(R.string.formato_horario_apertura,
                                    Utility.denormalizarHora(mApertura),
                                    Utility.denormalizarHora(mCierre)));
                    vH.mViewHoraComida.setText(
                            mContext.getString(R.string.formato_horario_apertura,
                                    Utility.denormalizarHora(mIni),
                                    Utility.denormalizarHora(mFin)));
                    vH.mViewContacto.setText(mContacto);
                    vH.mViewUbicacion.setText(Html.fromHtml("<u>"+mDir+"</u>"));
                    vH.mViewUbicacion.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAbridor.openLocation(mLat, mLon, mNombre);
                        }
                    });
                }
            }
        }

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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public interface AbridorLocalizacion {
        void openLocation (double latitud, double longitud, String label);
    }
}
