package com.example.julio.apptaller_taller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julio.apptaller_taller.Firebase.Tablas;
import com.example.julio.apptaller_taller.Model.Taller;
import com.example.julio.apptaller_taller.Model.Token;
import com.example.julio.apptaller_taller.Model.User_Taller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
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


public class PerfilFragment extends Fragment {
    TextView nombre, email, telefono;
    ImageView foto;
    Button update;
    FirebaseDatabase db;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.profile, container, false);
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        nombre = (TextView) vista.findViewById(R.id.Nombre);
        email = (TextView) vista.findViewById(R.id.Email);
        telefono = (TextView) vista.findViewById(R.id.Telefono);
        foto = (ImageView) vista.findViewById(R.id.Image);
        db = FirebaseDatabase.getInstance();
        nombre.setText(Tablas.currentUser.getNombre());
        email.setText(Tablas.currentUser.getEmail());
        telefono.setText(Tablas.currentUser.getTelefono());
        if (Tablas.currentUser.getImageName() != null && !TextUtils.isEmpty(Tablas.currentUser.getImageName())) {
            Picasso.get()
                    .load(Tablas.currentUser.getImageName())
                    .into(foto);
        }
        update = (Button) vista.findViewById(R.id.updateDatos);
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
        alertDialog.setMessage("Por favor edite su informacion");
        LayoutInflater inflater = this.getLayoutInflater();
        View layout_update = inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName = (MaterialEditText)layout_update.findViewById(R.id.edtName);
        final MaterialEditText edtTelefono = (MaterialEditText)layout_update.findViewById(R.id.edtTelefono);
        final ImageView image_upload = (ImageView) layout_update.findViewById(R.id.image_upload);
        edtName.setText(Tablas.currentUser.getNombre());
        edtTelefono.setText(Tablas.currentUser.getTelefono());
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

                String name = edtName.getText().toString();
                String phone = edtTelefono.getText().toString();

                Map<String, Object> updateInfo = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    updateInfo.put("name",name);
                if (!TextUtils.isEmpty(phone))
                    updateInfo.put("telefono",phone);

                DatabaseReference userInformation = FirebaseDatabase.getInstance().getReference(Tablas.user_mecanicos_tbl);
                userInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                mDialog.setMessage("Subiendo...");
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
                                        DatabaseReference userInformation = FirebaseDatabase.getInstance().getReference(Tablas.user_mecanicos_tbl);
                                        userInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(nameImage)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
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
