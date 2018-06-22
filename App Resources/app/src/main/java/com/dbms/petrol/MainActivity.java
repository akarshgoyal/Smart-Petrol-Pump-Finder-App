package com.dbms.petrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import at.markushi.ui.CircleButton;


public class MainActivity extends ActionBarActivity {

    FrameLayout fr;
    TextView tvSteps;
    ImageView ivEmpty, ivTank;
    Switch toggle;

    boolean stopWorker = false;
    int bytesAvailable;
    private char DELIMITER = '#';

    private List<String> mMessages = new ArrayList<String>();


     final int handlerState = 0;

    InputStream mmInputStream = null;
    OutputStream mmOutputStream = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    boolean foundDevice;

    LocationManager locationManager;
    TextToSpeech tts;

    CircleButton  btnNet, btnGPS;

    View line1, line2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        initialise();






        final Button btnStats = (Button) findViewById(R.id.btnStats);
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StatsActivity.class));
            }
        });

        toggle = (Switch) findViewById(R.id.toggle);

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fr.setVisibility(View.VISIBLE);
                    ivEmpty.setVisibility(View.GONE);
                    btnStats.setVisibility(View.GONE);
                    startProc();
                } else {
                    fr.setVisibility(View.GONE);
                    ivEmpty.setVisibility(View.VISIBLE);
                    btnStats.setVisibility(View.VISIBLE);


                }
            }
        });

        CircleButton dbactivity = (CircleButton) findViewById(R.id.btnGPS);
        dbactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DatabaseActivity.class));
            }
        });
    }

    private void initialise() {

        ivTank = (ImageView) findViewById(R.id.ivTank);
        ivEmpty = (ImageView) findViewById(R.id.ivEmpty);
        tvSteps = (TextView) findViewById(R.id.tvSteps);

        ivTank.setImageLevel(2);
        ivTank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int random = (int) (Math.random() * 5);
                ivTank.setImageLevel(random);
                String toSpeak = null;
                if (random == 1) {
                    toSpeak = "Petrol Level is low";
                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    tvSteps.setText("Petrol level Low !");
                }
            }
        });

        fr = (FrameLayout) findViewById(R.id.frMain);


        btnNet = (CircleButton) findViewById(R.id.btnNet);
        btnGPS = (CircleButton) findViewById(R.id.btnGPS);





        line1 = findViewById(R.id.line1);




    }

    private void startProc() {
        tvSteps.setText("1. Enable Internet");

        btnNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isInternetOn()) {
                    startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                } else {
                    tvSteps.setText("2. Enable GPS");
                    System.out.println("Internet is ON");
                    btnNet.setEnabled(false);
                    btnGPS.setEnabled(true);
                    btnNet.setColor(getResources().getColor(R.color.green));
                    line1.setBackgroundColor(getResources().getColor(R.color.green));
                }
            }
        });


        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                } else {
                    System.out.println("GPS is ON");
                    getCurrentLoc();
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    public void getCurrentLoc() {
        GPSTracker gps = new GPSTracker(MainActivity.this);
        Double currentLat = gps.getLatitude();
        Double currentLong = gps.getLongitude();
        String currentCoord = String.valueOf(currentLat.toString() + "," + currentLong.toString());
        System.out.println("Your Location is - " + currentCoord);

        String zero = "0.0,0.0";
        if (currentCoord.equals(zero)) {
            System.out.println("Not Working");
        } else {
            Log.d("ABC", "GPS OK");
            btnGPS.setColor(getResources().getColor(R.color.green));
            btnGPS.setEnabled(false);

            tvSteps.setText("3. Enable Bluetooth");

            Toast.makeText(getApplicationContext(), "Your Location is - " + currentCoord, Toast.LENGTH_LONG).show();

        }
    }





        public void write(byte[] bytes) {
            try {
                mmOutputStream.write(bytes);
            } catch (IOException e) {
            }
        }






    public final boolean isInternetOn() {
        ConnectivityManager connec = (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED
                || connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

            // if connected with internet
            return true;

        } else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED
                || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {

            // if not connected to the internet
            return false;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "Paused");
        if (tts != null) {
            tts.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "Resumed");
        if (toggle.isChecked()) {
            if (!isInternetOn() && btnNet.isEnabled()) {
                System.out.println("Turn on the Net");
            } else if (isInternetOn() && btnNet.isEnabled()) {
                tvSteps.setText("2. Enable GPS");
                System.out.println("Internet is ON");
                btnNet.setEnabled(false);
                btnNet.setColor(getResources().getColor(R.color.green));
                line1.setBackgroundColor(getResources().getColor(R.color.green));
                btnGPS.setEnabled(true);
            } else if (btnGPS.isEnabled()) {
                getCurrentLoc();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this,NearestActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}