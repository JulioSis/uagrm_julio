package com.example.julio.apptaller.Fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julio.apptaller.Firebase.Tablas;
import com.example.julio.apptaller.Inicio;
import com.example.julio.apptaller.Model.User;
import com.example.julio.apptaller.Model.Vehiculo;
import com.example.julio.apptaller.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;


public class VehiculoFragment extends Fragment {

    TextView nombre, placa, modelo, tipo, marca, año;
    ImageView foto;
    Button update;
    Vehiculo vehiculos;
    User user;
    FirebaseDatabase db;
    DatabaseReference vehiculo;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_vehiculo, container, false);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        nombre = (TextView) vista.findViewById(R.id.nombre);
        placa = (TextView) vista.findViewById(R.id.placa);
        tipo = (TextView) vista.findViewById(R.id.tipo);
        marca = (TextView) vista.findViewById(R.id.marca);
        modelo = (TextView) vista.findViewById(R.id.modelo);
        año = (TextView) vista.findViewById(R.id.anio);
        foto = (ImageView) vista.findViewById(R.id.fotoVehiculo);
        db = FirebaseDatabase.getInstance();
        vehiculo = db.getReference(Tablas.user_vehiculo_tbl);
        vehiculo.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot1) {
                 vehiculos = dataSnapshot1.getValue(Vehiculo.class);
                 nombre.setText(Tablas.currentUser.getName());
                 tipo.setText(vehiculos.getTipo());
                 placa.setText(vehiculos.getPlaca());
                 marca.setText(vehiculos.getMarca());
                 modelo.setText(vehiculos.getModelo());
                 año.setText(vehiculos.getAnio());
                 if (vehiculos.getImageName() != null && !TextUtils.isEmpty(vehiculos.getImageName())) {
                     Picasso.get()
                            .load(vehiculos.getImageName())
                            .into(foto);
                 }
             }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        update = (Button) vista.findViewById(R.id.updateDatosVehiculo);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogUpdateInfo();
            }
        });
        return vista;
    }

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Actualizar Datos");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_update = inflater.inflate(R.layout.layout_update_vehicle,null);

        final MaterialEditText edtPlaca = (MaterialEditText)layout_update.findViewById(R.id.edtPlaca);
        final MaterialEditText edtMarca = (MaterialEditText)layout_update.findViewById(R.id.edtMarca);
        final MaterialEditText edtModelo = (MaterialEditText)layout_update.findViewById(R.id.edtModelo);
        final MaterialEditText edtAño = (MaterialEditText)layout_update.findViewById(R.id.edtAño);
        final ImageView image_upload = (ImageView) layout_update.findViewById(R.id.image_upload);
        edtPlaca.setText(vehiculos.getPlaca());
        edtMarca.setText(vehiculos.getMarca());
        edtModelo.setText(vehiculos.getModelo());
        edtAño.setText(vehiculos.getAnio());
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_update);

        alertDialog.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog(getContext());
                waitingDialog.show();

                String placa = edtPlaca.getText().toString();
                String modelo = edtModelo.getText().toString();
                String marca = edtMarca.getText().toString();
                String año = edtAño.getText().toString();

                Map<String, Object> updateInfo = new HashMap<>();
                if (!TextUtils.isEmpty(placa))
                    updateInfo.put("placa",placa);
                if (!TextUtils.isEmpty(marca))
                    updateInfo.put("marca",marca);
                if (!TextUtils.isEmpty(modelo))
                    updateInfo.put("modelo",modelo);
                if (!TextUtils.isEmpty(año))
                    updateInfo.put("anio",año);

                DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Tablas.user_vehiculo_tbl);
                driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(getContext(),"Informacion actualizada",Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(getContext(),"Error al actualizar la informacion", Toast.LENGTH_SHORT).show();
                                }

                                waitingDialog.dismiss();
                            }
                        });
            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Seleccione imagen"), Tablas.Pick_Image_Request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Tablas.Pick_Image_Request && resultCode == RESULT_OK && data!=null && data.getData()!=null)
        {
            Uri saveUri = data.getData();
            if (saveUri != null)
            {
                final ProgressDialog mDialog = new ProgressDialog(getContext());
                mDialog.setMessage("Subiendo");
                mDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/"+imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mDialog.dismiss();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                        Map<String, Object> nameImage = new HashMap<>();
                                        nameImage.put("imageName",uri.toString());
                                        DatabaseReference userInformation = FirebaseDatabase.getInstance().getReference(Tablas.user_vehiculo_tbl);
                                        userInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(nameImage)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            ((Inicio)getActivity()).changeNavHeaderData();
                                                            Picasso.get()
                                                                    .load(uri.toString())
                                                                    .into(foto);
                                                            Toast.makeText(getContext(),"Subido",Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            Toast.makeText(getContext(),"Error al subir",Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }
                                });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                int progreso = (int) progress;
                                mDialog.setMessage("Subiendo "+progreso+"%");
                            }
                        });
            }
        }
    }


}
