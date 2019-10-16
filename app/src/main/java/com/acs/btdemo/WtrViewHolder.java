package com.acs.btdemo;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WtrViewHolder extends RecyclerView.ViewHolder {
    TextView no_wtr;
    TextView tanggal;
    TextView presentase;
    TextView keterangan;

    LinearLayout LL_presentase;
    CardView cv;
    String epoch;

    public WtrViewHolder(View itemView) {
        super(itemView);
        no_wtr=itemView.findViewById(R.id.kode_item_wtr);
        tanggal=itemView.findViewById(R.id.waktu_wtr);
        cv= itemView.findViewById(R.id.cv_wtr);
        presentase=itemView.findViewById(R.id.presentase);
        keterangan=itemView.findViewById(R.id.ket);
        LL_presentase = itemView.findViewById(R.id.LL_presentase);

    }
    public void onBind(final Context context, final WtrModel model) {

        no_wtr.setText(model.getTitle());
        tanggal.setText(model.getTanggal_wtr());
        epoch = model.getEpoch();
        Integer pres = Integer.parseInt(model.getPresentase());
        if(pres > 30 && pres <=60){
            LL_presentase.setBackgroundColor(Color.YELLOW);
        }else if(pres >60){
            LL_presentase.setBackgroundColor(Color.parseColor("#8ECF47"));
        }else{
            LL_presentase.setBackgroundColor(Color.RED);
        }
        presentase.setText(model.getPresentase()+" %");
        keterangan.setText(model.getApproved_qty()+" Item Terpenuhi dari "+model.getRequest_qty()+" Item.");
       // Log.d("epoch", "onBind: "+epoch);
        if(!epoch.equals("0")){
            cv.setCardBackgroundColor(Color.GRAY);
        }
    }

}
