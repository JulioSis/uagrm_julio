package com.example.julio.apptaller;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.julio.apptaller.Firebase.Tablas;
import com.example.julio.apptaller.Model.User;
import com.example.julio.apptaller.Model.Vehiculo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Principal extends AppCompatActivity {
    Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users, vehiculos;

    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";

    TextView txt_forgot_pwd;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_principal);

        printKeyHash();

        //Iniciar PaperDB
        Paper.init(this);

        //Iniciar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Tablas.user_conductores_tbl);

        vehiculos = db.getReference(Tablas.user_vehiculo_tbl);

        //Iniciar Vista
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Registro();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Login();
            }
        });

        txt_forgot_pwd = (TextView)findViewById(R.id.txt_forgot_password);
        txt_forgot_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialogForgotPwd();
                return false;
            }
        });
        //Auto Login
        String user = Paper.book().read(user_field);
        String pwd = Paper.book().read(pwd_field);
        if (user != null && pwd !=null)
        {
            if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd))
            {
                autoLogin(user,pwd);
            }
        }
    }

    private void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Principal.this);
        alertDialog.setTitle("Cambiar Contraseña");
        alertDialog.setMessage("Por favor ingrese su Email");

        LayoutInflater inflater = LayoutInflater.from(Principal.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd,null);

        final MaterialEditText edtEmail = (MaterialEditText)forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        alertDialog.setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog(Principal.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout, "Se ha restablecido su contraseña",Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout, "No se pudo restablecer la Contraseña",Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private void printKeyHash() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.julio.apptaller",
                    PackageManager.GET_SIGNATURES);

            for (Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void Login() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("LOGIN ");
        dialog.setMessage("Por favor use su Email y Contraseña para iniciar sesion");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.login,null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        dialog.setPositiveButton("Iniciar Sesion", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                btnSignIn.setEnabled(false);

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor ingrese su email", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor ingrese su Password", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                final android.app.AlertDialog DialogoEspera = new SpotsDialog(Principal.this);
                DialogoEspera.show();

                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                DialogoEspera.dismiss();

                                FirebaseDatabase.getInstance().getReference(Tablas.user_conductores_tbl)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Paper.book().write(user_field,edtEmail.getText().toString());
                                                Paper.book().write(pwd_field,edtPassword.getText().toString());
                                                Tablas.currentUser = dataSnapshot.getValue(User.class);
                                                Intent intancia = new Intent(Principal.this, Inicio.class);
                                                intancia.putExtra("Nombre", Tablas.currentUser.getName());
                                                startActivity(intancia);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DialogoEspera.dismiss();
                        Snackbar.make(rootLayout,"Fallo ",Snackbar.LENGTH_SHORT)
                                .show();

                        btnSignIn.setEnabled(true);
                    }
                });
            }
        });
        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void autoLogin(String user, String pwd) {
        final android.app.AlertDialog waitingdialog = new SpotsDialog(Principal.this);
        waitingdialog.show();
        //Login
        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingdialog.dismiss();
                        FirebaseDatabase.getInstance().getReference(Tablas.user_conductores_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Tablas.currentUser = dataSnapshot.getValue(User.class);
                                        Intent intancia = new Intent(Principal.this, Inicio.class);
                                        intancia.putExtra("Nombre", Tablas.currentUser.getName());
                                        startActivity(intancia);
                                        waitingdialog.dismiss();
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingdialog.dismiss();
                Snackbar.make(rootLayout,"Fallo ",Snackbar.LENGTH_SHORT)
                        .show();

                btnSignIn.setEnabled(true);
            }
        });

    }


    private void Registro() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTRO ");
        dialog.setMessage("Por favor use su email para registrarse");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.registro, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtTelefono = register_layout.findViewById(R.id.edtTelefono);

        dialog.setView(register_layout);


        dialog.setPositiveButton("Registro", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor ingrese su email", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtTelefono.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor ingrese su telefono", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Por favor ingrese su Password", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Contraseña muy corta..!!", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(edtName.getText().toString())) {
                    Snackbar.make(rootLayout, "No ingreso nombre.!!", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                //Registrar un usuario nuevo
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setTelefono(edtTelefono.getText().toString());

                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Vehiculo vehiculo = new Vehiculo();
                                                vehiculo.setPlaca("Sin Dato");
                                                vehiculo.setTipo("Sin Dato");
                                                vehiculo.setMarca("Sin Dato");
                                                vehiculo.setModelo("Sin Dato");
                                                vehiculo.setAnio("Sin Dato");

                                                vehiculos.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .setValue(vehiculo);
                                                Snackbar.make(rootLayout, "Registro Completo", Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout, "Fallo ", Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout, "Fallo ", Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });


            }
        });
        dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();

    }
}
