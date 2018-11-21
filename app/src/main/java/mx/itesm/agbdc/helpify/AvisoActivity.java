package mx.itesm.agbdc.helpify;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AvisoActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private DatabaseReference UsersRef;
    private String currentUserID;
    private ImageView fotoPerfil;
    private String [] results;
    private FirebaseAuth mAuth;
    private String institutionID;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aviso);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);
        fotoPerfil = findViewById(R.id.foto);

        Intent intent = getIntent();
        results = intent.getStringArrayExtra("User");

        currentUserID = results[0];
        institutionID = results[1];

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(AvisoActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                        //Picasso.with(NosotrosActivity.this).load(image).placeholder(R.drawable.profile).into(fotoPerfil);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        GiveInstitutionRights();
    }

    private void GiveInstitutionRights() {
        if(institutionID.equals("null")){
            navigationView.getMenu().findItem(R.id.nav_post).setVisible(false);
        }else{
            navigationView.getMenu().findItem(R.id.nav_post).setVisible(true);
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(AvisoActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(AvisoActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(AvisoActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.nav_post)
        {
            SendUserToPostActivity();
        }
        if (id == R.id.nav_home) {
            SendUserToMainActivity();
        }
        if (id == R.id.nav_find_friends)
        {
            Intent mapIntent = new Intent(AvisoActivity.this, Mapa.class);
            mapIntent.putExtra("User", results);
            startActivity(mapIntent);
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.nav_Logout)
        {
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (id == R.id.nav_profile)
        {
            results[2] = "";
            Intent addNewProfileIntent = new Intent(AvisoActivity.this, ProfileActivity.class);

            addNewProfileIntent.putExtra("User", results);
            startActivity(addNewProfileIntent);
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.nav_nosotros)
        {
            Intent avisoIntent = new Intent(AvisoActivity.this, NosotrosActivity.class);
            avisoIntent.putExtra("User", results);
            startActivity(avisoIntent);
            Toast.makeText(this, "Sobre Nosotros", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
