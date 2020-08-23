package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ProfileUserActivity extends AppCompatActivity {
    private TextView tvUsername = null;
    private TextView tvEmail = null;
    private String TAG = "RRRRRRRRRRRRRR";
    private UserInfo userInfo = new UserInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);
        initComponents();
    }

    private void initComponents() {
        setUserInfo();
        tvUsername = (TextView)findViewById(R.id.username);
        tvEmail = (TextView)findViewById(R.id.email);
        tvUsername.setText(userInfo.getName());
        tvEmail.setText(userInfo.getEmail());
        new ProfileUserActivity.RetrieveBitmapTask().execute(userInfo.getPhoto().toString());
    }

    private class RetrieveBitmapTask extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... urls) {
            Bitmap bm = null;
            try {
                URL aURL = new URL(urls[0]);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Error getting bitmap", e);
            }
            return bm;
        }

        protected void onPostExecute(Bitmap bm) {
            ImageView ivProfile = (ImageView)findViewById(R.id.imageAva);
            ivProfile.setImageBitmap(bm);
        }
    }

    private void setUserInfo() {
        userInfo.setUsername(getIntent().getStringExtra("username"));
        userInfo.setName(getIntent().getStringExtra("name"));
        userInfo.setEmail(getIntent().getStringExtra("email"));
        userInfo.setPhoto(Uri.parse(getIntent().getStringExtra("photoProfile")));
    }
}