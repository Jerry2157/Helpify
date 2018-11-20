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
    private Query query, queryPost;
    private String [] results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        setSupportActionBar(toolbar);
        fotoPerfil = findViewById(R.id.foto);

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
        results = intent.getStringArrayExtra("User");
        coordenadas = new String[results.length - 2];

        currentUserID = results[0];
        institutionID = results[1];
        Log.i("currentuser profile", currentUserID);
        Log.i("institution profile", institutionID);

        query = FirebaseDatabase.getInstance().getReference().child("Donaciones").
                orderByChild("userID").equalTo(currentUserID);
        queryPost = FirebaseDatabase.getInstance().getReference().child("Posts").
                orderByChild("uid").equalTo(currentUserID);

        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("fullname")) {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                        TextView nombre = findViewById(R.id.Name);
                        nombre.setText(fullname);
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

        GiveInstitutionRights();
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
                                myString[1] = model.getUid();
                                myString[2] = model.getUid();
                                myString[3] = model.getFullname();

                                Intent clickPostIntent = new Intent(ProfileActivity.this,ClickPostActivity.class);

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
            DisplayAllDonaciones();
        }else{
            navigationView.getMenu().findItem(R.id.nav_post).setVisible(true);
            DisplayAllUsersPosts();
            TextView title = findViewById(R.id.DonaTitle);
            title.setText("Posts de esta institución:");
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
        sendUserToMapa.putExtra("Users", results);
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
            Intent mapIntent = new Intent(ProfileActivity.this, Mapa.class);
            mapIntent.putExtra("User", results);
            startActivity(mapIntent);
            Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
        }
        if (id == R.id.nav_nosotros)
        {
            Intent mapIntent = new Intent(ProfileActivity.this, NosotrosActivity.class);
            mapIntent.putExtra("User", results);
            startActivity(mapIntent);
            Toast.makeText(this, "Nosotros", Toast.LENGTH_SHORT).show();
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
