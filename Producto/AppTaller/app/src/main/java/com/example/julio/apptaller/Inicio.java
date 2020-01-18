package com.example.julio.apptaller;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julio.apptaller.Firebase.Tablas;

import com.example.julio.apptaller.Fragment.FragmentMap;
import com.example.julio.apptaller.Fragment.PerfilFragment;
import com.example.julio.apptaller.Fragment.TalleresFragment;
import com.example.julio.apptaller.Fragment.VehiculoFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;


import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Inicio extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    TextView nombreC;
    String nombre;

    CircleImageView imageAvatar;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);
        nombreC = (TextView) navHeader.findViewById(R.id.NombreC);
        imageAvatar = (CircleImageView)navHeader.findViewById(R.id.imageAvatar);
        Bundle datos = this.getIntent().getExtras();
        nombre = datos.getString("Nombre");
        nombreC.setText(nombre);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        if (Tablas.currentUser.getImageName() != null && !TextUtils.isEmpty(Tablas.currentUser.getImageName())) {
            Picasso.get()
                   .load(Tablas.currentUser.getImageName())
                   .into(imageAvatar);

        }
        FragmentMap fragmentMap = new FragmentMap();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmento, fragmentMap);
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (id == R.id.mecanicos) {
            transaction.replace(R.id.fragmento, new FragmentMap());
            transaction.commit();
        } else if (id == R.id.talleres) {

            transaction.replace(R.id.fragmento, new TalleresFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        } else if (id == R.id.perfil) {

            transaction.replace(R.id.fragmento, new PerfilFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        }  else if (id == R.id.vehiculo) {

            transaction.replace(R.id.fragmento, new VehiculoFragment());
            transaction.addToBackStack(null);
            transaction.commit();

        } else if (id == R.id.cambiarPassword) {

            showDialogChangePwd();

        }else if (id == R.id.salir) {
            signOut();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogChangePwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Inicio.this);
        alertDialog.setTitle("Cambiar Contraseña");
        alertDialog.setMessage("Por favor complete toda la informacion");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_password,null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Inicio.this);
                waitingDialog.show();
                if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString())){
                    String email = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                       if (task.isSuccessful()){
                                                           Map<String,Object> password = new HashMap<>();
                                                           password.put("Password", edtRepeatPassword.getText().toString());
                                                           DatabaseReference clienteInformation = FirebaseDatabase.getInstance().getReference(Tablas.user_conductores_tbl);
                                                           clienteInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                   .updateChildren(password)
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful())
                                                                               Toast.makeText(Inicio.this, "Las contraseña fue modificada",Toast.LENGTH_SHORT).show();
                                                                           else
                                                                               Toast.makeText(Inicio.this, "Las contraseña fue modificada perd no se actualizo en la Base de Datos",Toast.LENGTH_SHORT).show();
                                                                       waitingDialog.dismiss();
                                                                       }
                                                                   });
                                                       }else{
                                                           Toast.makeText(Inicio.this, "Las contraseña no fue modificada",Toast.LENGTH_SHORT).show();
                                                       }
                                                    }
                                                });
                                    }else{
                                        waitingDialog.dismiss();
                                        Toast.makeText(Inicio.this, "La contraseña antigua es incorrecta",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else{
                    waitingDialog.dismiss();
                    Toast.makeText(Inicio.this, "Las contraseñas no coinciden",Toast.LENGTH_SHORT).show();
                }
            }
        });


        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void changeNavHeaderData(){
        nombreC.setText(Tablas.currentUser.getName());
        if (Tablas.currentUser.getImageName() != null && !TextUtils.isEmpty(Tablas.currentUser.getImageName())) {
            Picasso.get()
                    .load(Tablas.currentUser.getImageName())
                    .into(imageAvatar);
        }
    }

    private void signOut() {

        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Inicio.this, Principal.class);
        startActivity(intent);
        finish();
    }


}
