package com.apcs.mofs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ActivityProfile extends AppCompatActivity {
    private TextView tvUsername = null;
    private TextView tvEmail = null;
    private String TAG = "RRRRRRRRRRRRRR";
    private InfoUser infoUser = new InfoUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initComponents();
    }

    private void initComponents() {
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUserInfo();
        tvUsername = (TextView)findViewById(R.id.username);
        tvEmail = (TextView)findViewById(R.id.email);
        tvUsername.setText(infoUser.getName());
        tvEmail.setText(infoUser.getEmail());
        new ActivityProfile.RetrieveBitmapTask().execute(infoUser.getUri().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        infoUser.setUsername(getIntent().getStringExtra("username"));
        infoUser.setName(getIntent().getStringExtra("name"));
        infoUser.setEmail(getIntent().getStringExtra("email"));
        infoUser.setUri(Uri.parse(getIntent().getStringExtra("photoProfile")));
    }
}