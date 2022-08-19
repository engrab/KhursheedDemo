package com.data.cloner.phone;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class SelectVideoRecyclerAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<VideoData> copyVideo = new ArrayList<>();
    public final Context context;

    public SelectVideoRecyclerAdapter(ArrayList<VideoData> copyVideo, Context context) {
        this.copyVideo.addAll(copyVideo);
        this.context = context;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);
            return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == 1)
            return;

        if (holder instanceof MyViewHolder) {
            MyViewHolder holder1 = (MyViewHolder) holder;

            Glide.with(context).load(copyVideo.get(position).videouri).into(holder1.image1);


        }

    }

    public long getItemId(int i) {
        return (long) i;
    }

    @Override
    public int getItemCount() {
        if (copyVideo != null) {
            return copyVideo.size();
        } else {
            return 0;
        }
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image1;

        public MyViewHolder(View itemView) {
            super(itemView);


            image1 = itemView.findViewById(R.id.imagePreview);


        }

    }

    @Override
    public int getItemViewType(int position) {

        if (copyVideo.get(position).videoPath.equals("admob")) {
            return 1;
        } else
            return 2;

    }




    public boolean CheckMedia(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public boolean isDownloads(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean CheckUriExternal(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public void MakeFileCopy(File fromFile, File toFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel fileChannelInput = null;
        FileChannel fileChannelOutput = null;
        try {
            fileInputStream = new FileInputStream(fromFile);
            fileOutputStream = new FileOutputStream(toFile);
            fileChannelInput = fileInputStream.getChannel();
            fileChannelOutput = fileOutputStream.getChannel();
            fileChannelInput.transferTo(0, fileChannelInput.size(), fileChannelOutput);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
                if (fileChannelInput != null)
                    fileChannelInput.close();
                if (fileOutputStream != null)
                    fileOutputStream.close();
                if (fileChannelOutput != null)
                    fileChannelOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String FetchFilePath(final Context context, final Uri uri) {

        try {


            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (CheckUriExternal(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloads(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                    return FetchData(context, contentUri, null, null);
                } else if (CheckMedia(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    return FetchData(context, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return FetchData(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public String FetchData(Context context, Uri uri, String selection, String[] selectionArgs) {

        try {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return selection;
    }
}
