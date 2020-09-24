package com.apcs.mofs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class TaskUpdateListViewWithFirebaseData {
    private String type;
    private DatabaseReference mRefToRetrieveList;
    private ArrayList<String> listToRetrieve = new ArrayList<>();
    private DatabaseReference mRefToUpdateListView;
    private ListView listView;
    private static String TAG = "RRRRRRRRRRRRRRRRR";

    //AdapterFriends
    private static ArrayList<InfoUser> listToUpdateListViewFriends = new ArrayList<InfoUser>();
    private static AdapterFriends adapterFriends;

    public void updateListViewFriends() {
        retrieveList(new CallbackRetrieveList() {
            @Override
            public void onCallback(ArrayList<String> list) {
                for (int i = 0; i < list.size(); ++i) {
                    DatabaseReference mRefFriend = mRefToUpdateListView.child(list.get(i));
                    mRefFriend.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            InfoUser infoUser = new InfoUser();
                            for (DataSnapshot ds: dataSnapshot.getChildren()) {
                                switch (ds.getKey()) {
                                    case "profilePhoto":
                                        infoUser.setUri(Uri.parse(ds.getValue(String.class)));
                                        break;
                                    case "name":
                                        infoUser.setName(ds.getValue(String.class));
                                        break;
                                    case "email":
                                        infoUser.setEmail(ds.getValue(String.class));
                                        break;
                                }
                            }
                            infoUser.setUsername(dataSnapshot.getKey());
                            new TaskUpdateListViewWithFirebaseData.UpdateInfoUsersListView().execute(infoUser);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "Failed to read value.", databaseError.toException());
                        }
                    });
                }
            }
        });
    }

    private interface CallbackRetrieveList {
        void onCallback(ArrayList<String> listToRetrieve);
    }

    private void retrieveList(CallbackRetrieveList callbackRetrieveList) {
        mRefToRetrieveList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    //AdapterFriends
//                    if (type.equals("Friends"))
                    listToRetrieve.add(ds.getKey());
                }
                callbackRetrieveList.onCallback(listToRetrieve);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    private static class InfoUserParam {
        InfoUser infoUser;
        InfoUserParam(InfoUser infoUser) {
            this.infoUser = infoUser;
        }
    }

    private static class UpdateInfoUsersListView extends AsyncTask<InfoUser, Void, InfoUserParam> {
        protected InfoUserParam doInBackground(InfoUser... infoUsers) {
            Bitmap bm = null;
            try {
                URL url = new URL(infoUsers[0].getUri().toString());
                URLConnection conn = url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Error getting bitmap", e);
            }
            infoUsers[0].setBitmap(bm);
            return new InfoUserParam(infoUsers[0]);
        }
        protected void onPostExecute(InfoUserParam infoUserParam) {
            listToUpdateListViewFriends.add(infoUserParam.infoUser);
            adapterFriends.notifyDataSetChanged();
        }
    }

    public TaskUpdateListViewWithFirebaseData(String type, DatabaseReference mRefToRetrieveList, DatabaseReference mRefToUpdateListView, ListView listView, Context context) {
        this.type = type;
        this.mRefToRetrieveList = mRefToRetrieveList;
        this.mRefToUpdateListView = mRefToUpdateListView;
        this.listView = listView;
        listToUpdateListViewFriends = new ArrayList<InfoUser>();
        this.adapterFriends = new AdapterFriends(context, 0, listToUpdateListViewFriends);
        this.listView.setAdapter(adapterFriends);
    }

    public TaskUpdateListViewWithFirebaseData(String type, DatabaseReference mRefToRetrieveList, DatabaseReference mRefToUpdateListView, ListView listView, int resource, String key, String flag, Context context) {
        this.type = type;
        this.mRefToRetrieveList = mRefToRetrieveList;
        this.mRefToUpdateListView = mRefToUpdateListView;
        this.listView = listView;
        listToUpdateListViewFriends = new ArrayList<InfoUser>();
        this.adapterFriends = new AdapterFriends(context, resource, listToUpdateListViewFriends, key, flag);
        this.listView.setAdapter(adapterFriends);
    }

    public ArrayList<InfoUser> getListToUpdateListViewFriends() {
        return listToUpdateListViewFriends;
    }

    public AdapterFriends getAdapterFriends() {
        return adapterFriends;
    }

    public DatabaseReference getmRefToRetrieveList() {
        return mRefToRetrieveList;
    }

    public void setmRefToRetrieveList(DatabaseReference mRefToRetrieveList) {
        this.mRefToRetrieveList = mRefToRetrieveList;
    }

    public DatabaseReference getmRefToUpdateListView() {
        return mRefToUpdateListView;
    }

    public void setmRefToUpdateListView(DatabaseReference mRefToUpdateListView) {
        this.mRefToUpdateListView = mRefToUpdateListView;
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }
}
