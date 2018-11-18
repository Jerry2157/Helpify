package mx.itesm.agbdc.helpify;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription, PostCentro, DonarView;
    private Button DeletePostButton, DonarPostButton;
    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;
    private FirebaseDatabase Donacion;
    private DatabaseReference donacionRef;
    private String clave;
    private ProgressDialog loadingBar;
    private String institution;
    private String PostKey, currentUserID, databaseUserID, description, image;
    private String uid;
    private EditText claveBox;
    private boolean donarExecuted;
    private boolean registroBool;
    private String iName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);

        String[] myStrings = getIntent().getStringArrayExtra("PostKey");
        PostKey = myStrings[0];
        uid = myStrings[2];
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Post").child(PostKey);
        DatabaseReference inst = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        institution = myStrings[1];
        iName =myStrings[3];
        donarExecuted = false;
        PostImage = (ImageView)findViewById(R.id.click_post_image);
        PostDescription = (TextView)findViewById(R.id.click_post_description);
        DeletePostButton =(Button)findViewById(R.id.delete_post_button);
        DonarPostButton = (Button)findViewById(R.id.donar_post_button);
        PostCentro = (TextView)findViewById(R.id.nombre);
        DonarView = (TextView)findViewById(R.id.numeroDonar);
        Donacion = FirebaseDatabase.getInstance();
        claveBox = (EditText)findViewById(R.id.claveText);
        //PostDescription.setMovementMethod(new ScrollingMovementMethod());
        DeletePostButton.setVisibility(View.INVISIBLE);
        capturarDatos(PostKey);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.with(ClickPostActivity.this).load(image).into(PostImage);

                    if(currentUserID.equals(databaseUserID)){
                        DeletePostButton.setVisibility(View.VISIBLE);
                        DonarPostButton.setVisibility(View.VISIBLE);
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentPost();
            }
        });
        Log.i("institution", institution);
        if(!institution.equals("null") && uid.equals(currentUserID))
        {
            DeletePostButton.setVisibility(View.VISIBLE);
            DonarPostButton.setVisibility(View.VISIBLE);
            DonarPostButton.setText("Registrar");
            claveBox.setVisibility(View.VISIBLE);
            registroBool = true;
        }
        else if(institution.equals("null"))
        {
            DeletePostButton.setVisibility(View.INVISIBLE);
            DonarPostButton.setVisibility(View.VISIBLE);
            claveBox.setVisibility(View.INVISIBLE);
            registroBool = false;
        }
        else
        {
            DeletePostButton.setVisibility(View.INVISIBLE);
            DonarPostButton.setVisibility(View.INVISIBLE);
            claveBox.setVisibility(View.INVISIBLE);
        }
        DonarView.setVisibility(View.INVISIBLE);
        DonarPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registroBool && !claveBox.getText().toString().equals(""))
                {
                    Registrar();
                }
                else
                {
                    Donar();
                }

            }
        });

    }

    private void Registrar()
    {
        final String donaClave = claveBox.getText().toString();
        donacionRef = Donacion.getReference().child("Donaciones");

        donacionRef.addListenerForSingleValueEvent (new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                String saveCurrentDate = currentDate.format(new Date());
                boolean registroDone = false;
                if(!registroDone)
                {
                    if(dataSnapshot.hasChild(donaClave))
                    {
                        String childPostKey = dataSnapshot.child(donaClave).child("postKey").getValue().toString();
                        //Log.i("childPostKey - postkey", childPostKey + "\t" + PostKey);
                        if(childPostKey.equals(PostKey))
                        {
                            int num = Integer.parseInt(dataSnapshot.child(donaClave).child("Numero").getValue().toString());
                            donacionRef.child(donaClave).child("Fecha_realizdo").setValue(saveCurrentDate);
                            donacionRef.child(donaClave).child("Status").setValue("Realizado");
                            num++;
                            donacionRef.child(donaClave).child("Numero").setValue(String.valueOf(num));

                            Toast.makeText(ClickPostActivity.this, "Donación exitosa", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(ClickPostActivity.this, "Donación no encontrada", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(ClickPostActivity.this, "Donación no encontrada", Toast.LENGTH_LONG).show();
                        Log.i("rechazado postkey",  PostKey);
                    }

                }
                registroDone = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void capturarDatos(final String key)
    {
        FirebaseDatabase.getInstance().getReference().child("Posts").child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            HashMap a = (HashMap) dataSnapshot.getValue();
                            Log.i("key", key);

                            PostDescription.setText(a.get("description").toString());
                            PostCentro.setText(a.get("fullname").toString());

                            Picasso.with(getApplicationContext()).load(a.get("postimage").toString()).into(PostImage);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void Donar()
    {
        donacionRef = Donacion.getReference().child("Donaciones");

        donacionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                boolean existe = false;
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren())
                {
                    Log.i("donacion:", childSnapshot.getValue().toString());
                    if(childSnapshot.child("userID").getValue().toString().equals(currentUserID) && childSnapshot.child("postKey").getValue().toString().equals(PostKey))
                    {
                        Toast.makeText(ClickPostActivity.this, "Ya has donado.", Toast.LENGTH_LONG).show();
                        DonarView.setVisibility(View.VISIBLE);
                        DonarView.setText("Clave donacion: " + childSnapshot.getKey());
                        existe = true;
                        break;
                    }
                }
                 if(!donarExecuted && !existe)
                    {
                        donarExecuted = true;
                        makeDonacion();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void makeDonacion()
    {
        loadingBar.setTitle("Donando");
        loadingBar.setMessage("Porfavor espera, estamos registrando tu donación..");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        clave = (new randomStringGenerator()).generateString();
        donacionRef = Donacion.getReference().child("Donaciones").child(clave);
        HashMap donarMap = new HashMap();
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        String saveCurrentDate = currentDate.format(new Date());
        donarMap.put("userID", currentUserID.toString());
        donarMap.put("postKey", PostKey.toString());
        donarMap.put("Fecha_solicitud", saveCurrentDate.toString());
        donarMap.put("Fecha_realizdo", "null");
        donarMap.put("Status", "pendiente");
        donarMap.put("InsitutionID", uid.toString());
        donarMap.put("Numero", "0");
        donarMap.put("InstitutionName", iName.toString());

        donacionRef.updateChildren(donarMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    //SendUserToMainActivity();
                    DonarView.setVisibility(View.VISIBLE);
                    DonarView.setText(clave);
                    Toast.makeText(ClickPostActivity.this, "Tu código de donación es: " + clave, Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }
                else
                {
                    String message =  task.getException().getMessage();
                    Toast.makeText(ClickPostActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
    public class randomStringGenerator {
        public  void main(String[] args) {
            //System.out.println(generateString());
        }

        public  String generateString() {
            String uuid = UUID.randomUUID().toString();
            uuid.replace("-", "");
            return uuid.substring(0, 8);
        }
    }


    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post: ");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "post has been eliminated...",Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost() {
        ClickPostRef.removeValue();
        SendUserToMainActivity();
        //Toast.makeText(this, "post has been eliminated...",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
