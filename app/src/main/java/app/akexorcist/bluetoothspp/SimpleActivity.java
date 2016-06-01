/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.akexorcist.bluetoothspp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class SimpleActivity extends Activity {
    public static final String WALLPAPER = "wallpaper";
    BluetoothSPP bt;
    private Button btnChangeBackground;
    private CheckBox chkCheckConnection;
    private Button btnChangeDefault;
    private ImagePicker imagePicker;
    private ImageView background;
    private DisplayMetrics metrics;
    private String preference = "BT";
    private SharedPreferences sharedpreferences;
    private View relativeAfterConnection;
    private Button connect;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        bt = new BluetoothSPP(this);

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(SimpleActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = (Button) findViewById(R.id.btnScan);
        btnConnect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });

//        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        btnChangeBackground = (Button) findViewById(R.id.btnChangeBackground);
        chkCheckConnection = (CheckBox) findViewById(R.id.chkCloseArduinoIfConnectionLost);
        btnChangeDefault = (Button) findViewById(R.id.btnChangeDefaultPassword);
        btnChangeDefault.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLangDialog();
            }
        });
        connect = (Button) findViewById(R.id.btnConnect);
        connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeAfterConnection.setVisibility(View.VISIBLE);
            }
        });

        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        btnChangeBackground.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                imagePicker = new ImagePicker(SimpleActivity.this);
                imagePicker.setImagePickerCallback(new ImagePickerCallback() {
                                                       @Override
                                                       public void onImagesChosen(List<ChosenImage> images) {
                                                           Log.e("HI", "HI");
                                                           String originalPath = images.get(0).getOriginalPath();
                                                           setWallpaper(originalPath);
                                                           SharedPreferences.Editor edit = sharedpreferences.edit();
                                                           edit.putString(WALLPAPER, originalPath);
                                                           edit.commit();
                                                       }

                                                       @Override
                                                       public void onError(String message) {
                                                           // Do error handling
                                                           Log.e("Error", "Error");
                                                       }
                                                   }
                );

                imagePicker.pickImage();
            }
        });

        background = (ImageView) findViewById(R.id.imgBackground);
        background.getLayoutParams().height = metrics.heightPixels;
        background.getLayoutParams().width = metrics.widthPixels;
        background.requestLayout();

        sharedpreferences = getSharedPreferences(preference, Context.MODE_PRIVATE);
        String wallpaper = sharedpreferences.getString(WALLPAPER, "");
        if(!wallpaper.isEmpty()){
            setWallpaper(wallpaper);
        }

        relativeAfterConnection = findViewById(R.id.relativeAfterConnection);
        relativeAfterConnection.setVisibility(View.INVISIBLE);
    }

    private void setWallpaper(String originalPath) {
        Bitmap myBitmap = BitmapFactory
                .decodeFile(originalPath);

        Bitmap resized = Bitmap
                .createScaledBitmap(myBitmap,
                        metrics.heightPixels,
                        (int) (metrics.heightPixels * myBitmap
                                .getHeight()
                                / myBitmap
                                .getWidth()),
                        true);

        background.setImageBitmap(resized);
    }

    public void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_default_pwd, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.editPassword);

        dialogBuilder.setTitle("Set default password");
        dialogBuilder.setMessage("Enter text below");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Toast.makeText(getApplicationContext(), edt.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }

    public void setup() {
        Button btnSend = (Button) findViewById(R.id.btnConnect);
        btnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                bt.send("Text", true);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == Picker.PICK_IMAGE_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                imagePicker.submit(data);
            }
        }

    }
}
