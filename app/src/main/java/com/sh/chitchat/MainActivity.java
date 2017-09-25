package com.sh.chitchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.sh.chitchat.main_page_fragments.SectionsPagerAdapter;
import com.sh.utils.Constants;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        mViewPager = (ViewPager) findViewById(R.id.chat_pager);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        }else{
            mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_USERS).child(mAuth.getCurrentUser().getUid());
            mUserRef.child(Constants.DB_REF_USER_ONLINE).setValue("true");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.main_logout_btn:
                mAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.main_menu_setting:
                Intent settingIntent = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(settingIntent);
                break;
            case R.id.main_menu_all_user:
                Intent userIntent = new Intent(MainActivity.this,UsersActivity.class);
                startActivity(userIntent);
                break;

        }

        return true;
    }

    private void sendToStart(){
        Intent startIntent = new Intent(this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_USERS).child(mAuth.getCurrentUser().getUid());
            mUserRef.child(Constants.DB_REF_USER_ONLINE).setValue(ServerValue.TIMESTAMP);
        }
    }
}
