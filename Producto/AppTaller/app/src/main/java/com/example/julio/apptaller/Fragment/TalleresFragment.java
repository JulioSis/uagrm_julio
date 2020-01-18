package com.example.julio.apptaller.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.julio.apptaller.Firebase.Tablas;
import com.example.julio.apptaller.Model.MyAdapter;
import com.example.julio.apptaller.Model.Taller;
import com.example.julio.apptaller.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class TalleresFragment extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference reference;
    ArrayList<Taller> list;
    MyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_talleres, container, false);
        recyclerView = (RecyclerView)vista.findViewById(R.id.myReciclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        list = new ArrayList<Taller>();

        reference= FirebaseDatabase.getInstance().getReference().child(Tablas.talleres_tbl);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    Taller t = dataSnapshot1.getValue(Taller.class);
                    list.add(t);
                }
                adapter = new MyAdapter(getContext(),list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(),"No hay talleres ",Toast.LENGTH_SHORT).show();
            }
        });
        return vista;
    }

}
