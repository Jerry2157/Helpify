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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowInfo extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private DatabaseReference UsersRef, DonaRef;
    private String ins;
    private String institutionID;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String [] coordenadas;
    private FirebaseAuth mAuth;
    private ImageView fotoPerfil;
    private RecyclerView donaList;
    private Query  queryPost;
    String [] results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        fotoPerfil = findViewById(R.id.foto1);

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

        donaList = (RecyclerView) findViewById(R.id.Donaciones);
        donaList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        donaList.setLayoutManager(linearLayoutManager);


        Intent intent = getIntent();
        results = intent.getStringArrayExtra("User");
        coordenadas = new String[results.length - 2];
        for(int i = 2; i < results.length; i++)
        {
            coordenadas[i - 2] = results[i];
        }

        ins = results[2];
        institutionID = results[1];

        Log.i("currentuser profile", ins);
        Log.i("institution profile", institutionID);
        queryPost = FirebaseDatabase.getInstance().getReference().child("Posts").
                orderByChild("uid").equalTo(ins);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.child(ins).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        TextView nombre = findViewById(R.id.Name1);
                        nombre.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(ShowInfo.this).load(image).placeholder(R.drawable.profile).into(fotoPerfil);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        UsersRef.child(results[0]).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(ShowInfo.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        GiveInstitutionRights();
        DisplayAllUsersPosts();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.post_simple,
                                PostsViewHolder.class,
                                queryPost
                        )
                {

                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, final Posts model, int position)
                    {

                        final String PostKey = getRef(position).getKey();

                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String[] myString = new String[4];
                                myString[0] = PostKey;
                                myString[1] = institutionID;
                                myString[2] = model.getUid();
                                myString[3] = model.getFullname();

                                Intent clickPostIntent = new Intent(ShowInfo.this,ClickPostActivity.class);

                                clickPostIntent.putExtra("PostKey", myString);

                                startActivity(clickPostIntent);
                            }
                        });
                    }
                };
        donaList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }



        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time1);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date1);
            PostDate.setText("    " + date);
        }


        public void setPostimage(Context ctx1, String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image1);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }


    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ShowInfo.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToMapa()
    {
        Intent sendUserToMapa = new Intent(ShowInfo.this, Mapa.class);
        sendUserToMapa.putExtra("Coordenadas", coordenadas);
        startActivity(sendUserToMapa);
        finish();
    }

    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(ShowInfo.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(ShowInfo.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
            results[2] = "";

            Intent mapIntent = new Intent(ShowInfo.this, Mapa.class);
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
            Intent addNewProfileIntent = new Intent(ShowInfo.this, ProfileActivity.class);

            addNewProfileIntent.putExtra("User", results);
            startActivity(addNewProfileIntent);
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.nav_nosotros)
        {
            Intent mapIntent = new Intent(ShowInfo.this, NosotrosActivity.class);
            mapIntent.putExtra("User", results);
            startActivity(mapIntent);
            Toast.makeText(this, "Nosotros", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.nav_aviso)
        {
            Intent avisoIntent = new Intent(ShowInfo.this, AvisoActivity.class);
            avisoIntent.putExtra("User", results);
            startActivity(avisoIntent);
            Toast.makeText(this, "Aviso de Privacidad", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
