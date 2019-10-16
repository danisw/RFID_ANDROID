package com.acs.btdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    TextView judul;
    TextView uid;
    TextView qty;
    public ItemViewHolder(View itemView) {
        super(itemView);
        judul=itemView.findViewById(R.id.kode_item_wht);
        uid=itemView.findViewById(R.id.uid_picked_wht);
        qty=itemView.findViewById(R.id.qty_picked_wht);

    }
    public void onBind(final Context context, final ItemWhtModel model) {

        judul.setText(model.getTitle());
        uid.setText(model.getUid());
        qty.setText(model.getQty());
    }

}
