package com.data.cloner.phone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.PowerManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class VideoFragment extends Fragment {

    ArrayList<VideoData> videoDataArrayList = new ArrayList<>();
    RecyclerView videoRecyclerView;
    private PowerManager powerManager;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private SelectVideoRecyclerAdapter adapter;
    Context context;

    @SuppressLint({"NewApi"})
    private class FetchVideoAsyncTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;

        private FetchVideoAsyncTask() {
            this.progressDialog = null;
        }


        public void onPreExecute() {
            this.progressDialog = new ProgressDialog(VideoFragment.this.getActivity());
            this.progressDialog.setMessage("Loading...");
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }


        public Boolean doInBackground(Void... voidArr) {
            return Boolean.valueOf(VideoFragment.this.b());
        }


        public void onPostExecute(Boolean bool) {
            this.progressDialog.dismiss();
            if (bool.booleanValue()) {

                adapter = new SelectVideoRecyclerAdapter(videoDataArrayList, context);
                videoRecyclerView.setAdapter(adapter);
            }
        }
    }



    public VideoFragment() {
        // Required empty public constructor
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_video, container, false);
        this.powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        videoRecyclerView = inflate.findViewById(R.id.videoRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);

        manager.setStackFromEnd(true);
        videoRecyclerView.setLayoutManager(manager);
        videoRecyclerView.setHasFixedSize(true);
        videoRecyclerView.setItemViewCacheSize(20);
        videoRecyclerView.setDrawingCacheEnabled(true);
        videoRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        GridLayoutManager mLayoutManager = new GridLayoutManager(context, 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getItemViewType(position)) {
                    case 1:
                        return 2;
                    case 2:
                        return 1;
                    default:
                        return 2;
                }
            }
        });
        videoRecyclerView.setLayoutManager(mLayoutManager);
        return inflate;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            new FetchVideoAsyncTask().execute(new Void[0]);
        } else {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new FetchVideoAsyncTask().execute(new Void[0]);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }


    @SuppressLint({"NewApi"})
    public boolean b() {
        Cursor managedQuery = getActivity().managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "_id", "_display_name", "duration"}, null, null, " _id DESC");
        int count = managedQuery.getCount();
        if (count <= 0) {
            return false;
        }
        managedQuery.moveToFirst();
        for (int i = 0; i < count; i++) {
            Uri withAppendedPath = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ContentUtill.getLong(managedQuery));
            this.videoDataArrayList.add(new VideoData(managedQuery.getString(managedQuery.getColumnIndexOrThrow("_display_name")), withAppendedPath, managedQuery.getString(managedQuery.getColumnIndex("_data")), ContentUtill.getTime(managedQuery, "duration")));
            managedQuery.moveToNext();
        }
        return true;
    }
}