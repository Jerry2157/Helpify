package mx.itesm.agbdc.helpify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{
    private EditText UserName, FullName, CountryName, InstitutionID;
    private Button SaveInformationbuttion,SendToMapaButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private Switch SwitchButton;
    private String IsInstitution;
    private boolean SwitchInstitutionOn;

    private boolean perfil;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    private String LatitudToFirebase;
    private String LongitudToFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        LatitudToFirebase = "null";
        LongitudToFirebase = "null";
        perfil = false;
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        SendToMapaButton = (Button)findViewById(R.id.setup_mapa);
        SendToMapaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToMapaUbicacion();
            }
        });


        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationbuttion = (Button) findViewById(R.id.setup_information_button2);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        InstitutionID = (EditText)findViewById(R.id.setup_institutionID);
        SwitchButton = (Switch)findViewById(R.id.switch_Institution);
        SwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b == true){
                    //IsInstitution = "true";
                    SwitchInstitutionOn = true;
                    InstitutionID.setVisibility(View.VISIBLE);
                    SendToMapaButton.setVisibility(View.VISIBLE);
                }else{
                    //IsInstitution = "false";
                    SwitchInstitutionOn = false;
                    InstitutionID.setVisibility(View.INVISIBLE);
                    SendToMapaButton.setVisibility(View.INVISIBLE);
                }
            }
        });


        SaveInformationbuttion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SaveAccountSetupInformation();
            }
        });


        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        perfil = true;
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMapaUbicacion()
    {
        Intent i = new Intent(this, MapUbication.class);
        Log.i("requestcode", Integer.toString(24));
        startActivityForResult(i, 24);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("requestcode", Integer.toString(requestCode));
        if (requestCode == 24 && data!=null) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                Log.i("resultado", result);
                String [] parts = result.split(",");
                LatitudToFirebase = parts[0];
                LongitudToFirebase = parts[1];
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {

                            Toast.makeText(SetupActivity.this, "Profile Image stored successfully to Firebase storage...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                perfil = true;
                                                Log.i("perfilstatus", Boolean.toString(perfil));
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }



    private void SaveAccountSetupInformation()
    {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();
        String institutionId = InstitutionID.getText().toString();
        Log.i("Perfil final:", Boolean.toString(perfil));

        if(!perfil)
        {
            Toast.makeText(this, "Please select an image for your profile...", Toast.LENGTH_SHORT).show();
        }
        else if((LongitudToFirebase.equals("null") || LongitudToFirebase.equals("null")) && SwitchInstitutionOn)
        {
            Toast.makeText(this, "Please select your ubication...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(fullname))
        {
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        }
        else if(SwitchInstitutionOn && TextUtils.isEmpty(institutionId)){

            Toast.makeText(this, "If you are an Institution please write your ID...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(institutionId.equals("null")){
                Toast.makeText(this, "InstitutionID cannot be null...", Toast.LENGTH_SHORT).show();
            }else{
                loadingBar.setTitle("Saving Information");
                loadingBar.setMessage("Please wait, while we are creating your new Account...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                HashMap userMap = new HashMap();
                userMap.put("username", username);
                userMap.put("fullname", fullname);
                userMap.put("country", country);
                userMap.put("status", "Hey there, i am using Helpify, developed by an amazing team at ITESM CEM.");
                if(SwitchInstitutionOn == true){
                    userMap.put("InstitutionID", institutionId);
                    userMap.put("instLatitud",LatitudToFirebase);
                    userMap.put("instLongitud",LongitudToFirebase);
                }else{
                    userMap.put("InstitutionID", "null");
                    userMap.put("instLatitud","null");
                    userMap.put("instLongitud","null");
                }
                userMap.put("dob", "none");
                userMap.put("relationshipstatus", "none");
                UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if(task.isSuccessful())
                        {
                            SendUserToMainActivity();
                            Toast.makeText(SetupActivity.this, "your Account is created Successfully.", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                        else
                        {
                            String message =  task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }



    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
