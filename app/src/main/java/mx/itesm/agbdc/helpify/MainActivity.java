package mx.itesm.agbdc.helpify;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;
    private ArrayList<String> coordenadas;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;
    private String InstitutionID;

    String currentUserID;
    private Query query;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        query = PostsRef.orderByChild("date");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Inicio");


        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);


        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);


        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        coordenadas = new ArrayList<>();

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
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    if (dataSnapshot.hasChild("InstitutionID")) {
                         InstitutionID = dataSnapshot.child("InstitutionID").getValue().toString();
                        GiveInstitutionRights(InstitutionID);
                    } else {
                        CheckUserExistence();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        capturarDatos();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                UserMenuSelector(item);
                return false;
            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts();
    }

    private void GiveInstitutionRights(String InstitutionID) {
        if(InstitutionID.equals("null")){
            AddNewPostButton.setVisibility(View.INVISIBLE);

            navigationView.getMenu().findItem(R.id.nav_post).setVisible(false);
        }else{
            AddNewPostButton.setVisibility(View.VISIBLE);
        }
    }

    private void DisplayAllUsersPosts()
    {
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                query
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, final Posts model, int position)
                    {

                        final String PostKey = getRef(position).getKey();

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                        viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String[] myString = new String[4];
                                myString[0] = PostKey;
                                myString[1] = InstitutionID;
                                myString[2] = model.getUid();
                                myString[3] = model.getFullname();

                                Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);

                                clickPostIntent.putExtra("PostKey", myString);

                                startActivity(clickPostIntent);
                            }
                        });
                    }
                };
        postList.setAdapter(firebaseRecyclerAdapter);
    }



    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx1).load(postimage).into(PostImage);
        }
    }

    private void capturarDatos()
    {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            HashMap a = (HashMap) snapshot.getValue();
                            /*for(Object k: a.keySet())
                            {
                                //Log.i("kMap", k.toString());
                            }*/
                            String latdb;
                            String londb;
                            String fName;
                            try {
                                String ins = a.get("InstitutionID").toString();
                                if (!ins.equals("null")) {
                                    latdb = (a.get("instLatitud").toString());
                                    londb = (a.get("instLongitud").toString());
                                    fName = (a.get("fullname").toString());
                                    if (!latdb.equals("null") && !londb.equals("null")) {
                                        coordenadas.add(latdb);
                                        coordenadas.add(londb);
                                        coordenadas.add(fName);
                                        coordenadas.add(snapshot.getKey());
                                    }

                                }
                            }catch (Exception e)
                            {
                                Log.i("erased user captura", snapshot.getKey());
                                snapshot.getRef().setValue(null);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }


    private void SendUserToPostActivity()
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendUserToMapa()
    {
        String latlng [] = new String[coordenadas.size()];
        int k = 0;
        for(String cc: coordenadas)
        {
            latlng[k] = cc;
            //Log.i("double", latlng[k]);
            k++;
        }
        Intent sendUserToMapa = new Intent(MainActivity.this, Mapa.class);
        sendUserToMapa.putExtra("Coordenadas", latlng);
        startActivity(sendUserToMapa);
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence()
    {
        try{
            final String current_user_id = mAuth.getCurrentUser().getUid();

            UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(!dataSnapshot.hasChild(current_user_id))
                    {
                        SendUserToSetupActivity();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } catch (Exception e)
        {
            Log.i("failed user", mAuth.getCurrentUser().toString());
        }

    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void UserMenuSelector(MenuItem item)
    {
        String[] myString = new String[coordenadas.size() + 3];
        myString[0] = currentUserID;
        myString[1] = InstitutionID;
        myString[2] = "";
        int k = 3;
        for(String cc: coordenadas)
        {
            myString[k] = cc;
            k++;
        }
        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                Intent addNewProfileIntent = new Intent(MainActivity.this, ProfileActivity.class);

                addNewProfileIntent.putExtra("User", myString);
                startActivity(addNewProfileIntent);
                Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Inicio", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                Intent mapIntent = new Intent(MainActivity.this, Mapa.class);
                String[] myString1 = new String[coordenadas.size() + 3];
                myString1[0] = currentUserID;
                myString1[1] = InstitutionID;
                myString1[2] = "";
                Log.i("currenuser main", myString1[0]);
                Log.i("currenuser ins", myString1[1]);

                int kk = 3;
                for(String cc: coordenadas)
                {
                    myString1[kk] = cc;
                    kk++;
                }
                mapIntent.putExtra("User", myString1);
                startActivity(mapIntent);
                Toast.makeText(this, "Ubicaciones", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_nosotros:
                Intent nosotrosIntent = new Intent(MainActivity.this, NosotrosActivity.class);
                String[] myString2 = new String[coordenadas.size() + 3];
                myString2[0] = currentUserID;
                myString2[1] = InstitutionID;
                myString2[2] = "";
                Log.i("currenuser main", myString2[0]);
                Log.i("currenuser ins", myString2[1]);

                int kkk = 3;
                for(String cc: coordenadas)
                {
                    myString2[kkk] = cc;
                    kkk++;
                }
                nosotrosIntent.putExtra("User", myString2);
                startActivity(nosotrosIntent);
                Toast.makeText(this, "Sobre Nosotros", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_aviso:
                Intent avisoIntent = new Intent(MainActivity.this, AvisoActivity.class);
                avisoIntent.putExtra("User", myString);
                startActivity(avisoIntent);
                Toast.makeText(this, "Aviso de privacidad", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_Logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }
}