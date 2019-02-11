/*
 * @Author: CodeOfSH
 * @Github: https://github.com/CodeOfSH
 * @Date: 2019-02-09 12:50:22
 * @LastEditors: CodeOfSH
 * @LastEditTime: 2019-02-10 15:52:48
 * @Description: Main Activity file for the project
 */
package com.ub.codesh.controlvibrator;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = { "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    public final static int FILE_SELECT_CODE = 1;
    private static final String TAG = "ChooseFile";

    private Vibrator vibrator;
    private long[] vibrateControl = { 0 };
    private boolean hasChooseFile;
    private boolean isVibrating;
    private Button buttonSelect;
    private Button buttonOnce;
    private Button buttonRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        verifyStoragePermissions(this);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        buttonSelect = (Button) findViewById(R.id.btn_select);
        buttonOnce = (Button) findViewById(R.id.btn_once);
        buttonRepeat = (Button) findViewById(R.id.btn_repeat);
        hasChooseFile = false;
        isVibrating = false;
    }

    /**
     * @description: verify the permission for access storage
     * @param {type} Activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            // check if have the permission
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // if no permission, ask for permission
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @description: function for the select button
     */
    public void selectFile(View V) {
        System.out.print("In selecting file");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Cannot open file manager", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @description: function for the vibrate once button
     */
    public void vibrateOnce(View V) {
        if (hasChooseFile) {
            Toast.makeText(this, "Start Vibrating", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(vibrateControl, -1);// no repeat
        }
    }

    /**
     * @description: function for the repeat virbate button
     */
    public void vibrateRepeat(View V) {
        if (hasChooseFile) {
            if (!isVibrating) {
                Toast.makeText(this, "Start Vibrating", Toast.LENGTH_SHORT).show();
                buttonSelect.setEnabled(false);
                buttonOnce.setEnabled(false);
                buttonRepeat.setText(R.string.btn_stop);
                vibrator.vibrate(vibrateControl, 0);// repeat from index 0
                isVibrating = true;
            } else {
                Toast.makeText(this, "Stop Vibrating", Toast.LENGTH_SHORT).show();
                buttonSelect.setEnabled(true);
                buttonOnce.setEnabled(true);
                buttonRepeat.setText(R.string.btn_repeat);
                vibrator.cancel();
                isVibrating = false;
            }
        }
    }

    /**
     * @description: answer for activity, mainly for the file select activity
     * @param {type} int requestCode, int resultCode, Intent data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case FILE_SELECT_CODE:
            if (resultCode == RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                Log.d(TAG, "File Uri: " + uri.toString());
                // Extract the path from Uri
                String path = getPath(this, uri);
                Log.d(TAG, "File Path: " + path);
                // Read the file
                try {
                    File file = new File(path);
                    ArrayList<Long> tempList = new ArrayList<>();
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = br.readLine()) != null) {
                        tempList.add(Long.parseLong(line));
                    }
                    // Convert List to array
                    vibrateControl = new long[tempList.size()];
                    for (int i = 0; i < tempList.size(); i++) {
                        vibrateControl[i] = tempList.get(i);
                    }
                    hasChooseFile = true;
                } catch (Exception e) {
                    Toast.makeText(this, "Error in Opening the File", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @description: to extract path from uri
     * @param {type} Context context, Uri uri
     * @return: string of the path
     */
    public static String getPath(Context context, Uri uri) {
        // check if the Uri is provided by 'content' or 'file'
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Here is not a good way to get the string
            try {
                String string = uri.toString();
                String a[];
                // check if the file is in SD card
                if (string.indexOf(String.valueOf(Environment.getExternalStorageDirectory())) != -1) {
                    // spilt the Uri string
                    a = string.split(String.valueOf(Environment.getExternalStorageDirectory()));
                    // return the path of the file
                    return Environment.getExternalStorageDirectory() + a[1];
                } else if (string.indexOf(String.valueOf(Environment.getDataDirectory())) != -1) { // 判断文件是否在手机内存中
                    // spilt the Uri string
                    a = string.split(String.valueOf(Environment.getDataDirectory()));
                    // return the path of the file
                    return Environment.getDataDirectory() + a[1];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}
