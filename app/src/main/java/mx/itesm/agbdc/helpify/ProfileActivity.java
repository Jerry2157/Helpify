package mx.itesm.agbdc.helpify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private DatabaseReference UsersRef, DonaRef;
    private String currentUserID;
    private String institutionID;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String [] coordenadas;
    private FirebaseAuth mAuth;
    private ImageView fotoPerfil;
    private RecyclerView donaList;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);
        fotoPerfil = findViewById(R.id.foto);
        /*toolbar = findViewById(R.id.profile_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Perfil");*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawable_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        DonaRef = FirebaseDatabase.getInstance().getReference().child("Donaciones");
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        donaList = (RecyclerView) findViewById(R.id.Donaciones);
        donaList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        donaList.setLayoutManager(linearLayoutManager);



        Intent intent = getIntent();
        String [] results = intent.getStringArrayExtra("User");
        coordenadas = new String[results.length - 2];
        for(int i = 2; i < results.length; i++)
        {
            coordenadas[i - 2] = results[i];
        }

        currentUserID = results[0];
        institutionID = results[1];
        GiveInstitutionRights();

        query = FirebaseDatabase.getInstance().getReference().child("Donaciones").
                orderByChild("userID").equalTo(currentUserID);
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
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                        Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.profile).into(fotoPerfil);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DisplayAllDonaciones();
    }


    private void DisplayAllDonaciones()
    {
        FirebaseRecyclerAdapter<Donations, DonaViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Donations, DonaViewHolder>(
                        Donations.class,
                        R.layout.donaciones_list,
                        DonaViewHolder.class,
                        query
                ) {
                    @Override
                    protected void populateViewHolder(DonaViewHolder viewHolder, final Donations model, int position) {
                        final String DonaKey = getRef(position).getKey();
                        viewHolder.setDate(model.getFecha_realizdo());
                        viewHolder.setFullname(model.getInstitutionName());
                        viewHolder.setNumeber("Numero donaciones: " + model.getNumero());
                        viewHolder.setID(DonaKey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String[] myString = new String[]{model.getPostKey(), "null",
                                        model.getUserID(), model.getInstitutionName()};
                                Intent clickPostIntent = new Intent(ProfileActivity.this,ClickPostActivity.class);

                                clickPostIntent.putExtra("PostKey", myString);

                                startActivity(clickPostIntent);
                            }
                        });
                    }
                };

        donaList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class DonaViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public DonaViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_post_name);
            username.setText(fullname);
        }


        public void setDate(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.dona_date);
            if(time.equals("null"))
            {
                PostTime.setText("No realizada");
            }
            else
            {
                PostTime.setText("Fecha de ultima donacion \t\t"  + time);
            }
        }

        public void setNumeber(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.donaNum);
            PostDate.setText(date);


        }
        public void setID(String id)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.idDona);
            PostDate.setText("Clave Donacion: " + id);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void GiveInstitutionRights() {
        if(institutionID.equals("null")){
            navigationView.getMenu().findItem(R.id.nav_post).setVisible(false);
        }else{
            navigationView.getMenu().findItem(R.id.nav_post).setVisible(true);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawable_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
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
    }*/

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToMapa()
    {
        Intent sendUserToMapa = new Intent(ProfileActivity.this, Mapa.class);
        sendUserToMapa.putExtra("Coordenadas", coordenadas);
        startActivity(sendUserToMapa);
        finish();
    }

    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(ProfileActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
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
            SendUserToMapa();
        }
        if(id == R.id.nav_Logout)
        {
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawable_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
