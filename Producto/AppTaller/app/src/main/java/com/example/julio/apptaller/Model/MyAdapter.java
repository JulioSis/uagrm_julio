package com.example.julio.apptaller.Model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.julio.apptaller.R;
import com.example.julio.apptaller.Fragment.TallerDetailFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Julio on 11/10/2019.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Taller> talleres;

    public MyAdapter(Context c, ArrayList<Taller> t){
        context = c;
        talleres = t;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_layout,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final Taller taller = talleres.get(i);
        myViewHolder.nombre.setText(taller.getNombre());
        myViewHolder.direccion.setText(taller.getDireccion());
        myViewHolder.horario.setText(taller.getHorario());
        Picasso.get().load(taller.getImagen()).into(myViewHolder.image);
        myViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(context, TallerDetailFragment.class);
                TallerDetailFragment detailFragment = new TallerDetailFragment();
                Bundle intent = new Bundle();
                intent.putString("Nombre",taller.getNombre());
                intent.putString("Direccion",taller.getDireccion());
                intent.putString("Horario",taller.getHorario());
                intent.putString("Imagen",taller.getImagen());
                intent.putString("Latitud",taller.getLatitud());
                intent.putString("Longitud",taller.getLongitud());
                intent.putString("Telefono",taller.getTelefono());
                detailFragment.setArguments(intent);
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragmento, detailFragment).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return talleres.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView nombre, direccion ,horario;
        ImageView image;
        RelativeLayout relativeLayout;
        public  MyViewHolder(View itemView){
            super(itemView);
            nombre = (TextView) itemView.findViewById(R.id.textNombre);
            direccion = (TextView) itemView.findViewById(R.id.textDireccion);
            horario = (TextView) itemView.findViewById(R.id.textHorario);
            image = (ImageView) itemView.findViewById(R.id.imageTaller);
            relativeLayout = itemView.findViewById(R.id.relative);
        }
    }
}
