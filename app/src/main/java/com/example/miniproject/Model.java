package com.example.miniproject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Model extends Thread {

    private Context context;

    public Model(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        AssetManager assetManager = context.getAssets();
        File file = new File(context.getCacheDir()+"/eye_eyebrows_22.dat");
        try {

            InputStream inputStream = assetManager.open("eye_eyebrows_22.dat");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(buffer);
            fileOutputStream.close();

            Native.loadModel(file.getPath());
            Log.e("Model","Loaded");
            return;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Model","Error in loading file");
        }
    }
}
