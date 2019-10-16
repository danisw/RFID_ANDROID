package com.acs.btdemo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.List;

public abstract class Adapter <DataClass, ViewHolder extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<ViewHolder>{
    private int mLayout;
    private Class<ViewHolder> viewHolderClass;
    private List<DataClass> data;
    private Callback callback;

    Adapter(int mLayout, Class<ViewHolder> viewHolderClass, List<DataClass> data) {
        this.mLayout = mLayout;
        this.viewHolderClass = viewHolderClass;
        this.data = data;
        // this.callback=callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayout,parent,false);
        try{
            Constructor <ViewHolder> constructor = viewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);

        }catch(Exception error){
            throw  new RuntimeException(error);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataClass model = getItem(position);
        bindView(holder,model,position);


    }
    // A method to set a callback from activity/fragment.
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    protected abstract void bindView(ViewHolder holder, DataClass model, int position);

    protected  DataClass getItem(int position){
        return data.get(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface Callback {
        void onImageClick(int position);
        void onRemoveItem(int position);
    }
}