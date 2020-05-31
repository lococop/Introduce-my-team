package com.example.weatherforecastbygwanwoo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "imagesearchexample";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private String API_KEY = "&APPID=5fd2f2cde90c1533efb95b19c048a528";
    private String MODE = "&mode=json";
    private String UNITS = "&units=metric";
    private String CNT = "&cnt=14";
    private String SEARCH_CITY = "q=";
    private ProgressDialog progressDialog = null;
    private SimpleAdapter adapter = null;
    private List<HashMap<String,String>> photoinfoList = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listviewPhtoList = (ListView)findViewById(R.id.listview_main_list);

        photoinfoList = new ArrayList<HashMap<String,String>>();

        String[] from = new String[]{"dt", "min", "max", "main"};
        int[] to = new int[] {R.id.textview_main_listviewdata1, R.id.textview_main_listviewdata2,
                R.id.textview_main_listviewdata3, R.id.textview_main_listviewdata4};
        adapter = new SimpleAdapter(this, photoinfoList, R.layout.listview_items, from, to);
        listviewPhtoList.setAdapter(adapter);

        SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
        String cityname = sharedPreferences.getString("CityName", "");

        if(cityname != ""){
            SharedPreferences JsharedPreferences = getSharedPreferences(Jshared, 0);
            String jstring = JsharedPreferences.getString("jsonstring", "");
            jsonParser(jstring);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    String shared = "file";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if(id == R.id.refresh){
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait.....");
            progressDialog.show();

            Intent intent = getIntent();
            String keyword = intent.getStringExtra("str");

            SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("CityName", keyword);
            getJSON(keyword);
            editor.commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final MyHandler mHandler = new MyHandler(this);


    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainactivity) {
            weakReference = new WeakReference<MainActivity>(mainactivity);
        }

        @Override
        public void handleMessage(Message msg) {

            MainActivity mainActivity = weakReference.get();

            if (mainActivity != null) {
                switch (msg.what) {

                    case LOAD_SUCCESS:
                        mainActivity.progressDialog.dismiss();
                        mainActivity.adapter.notifyDataSetChanged();
                        break;
                }
            }
        }
    }

    public void  getJSON(final String keyword) {

        if ( keyword == null) return;

        Thread thread = new Thread(new Runnable() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {

                String result;

                try {

                    Log.d(TAG, SEARCH_URL+SEARCH_CITY+keyword+MODE+UNITS+CNT+API_KEY);
                    URL url = new URL(SEARCH_URL+SEARCH_CITY+keyword+MODE+UNITS+CNT+API_KEY);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.connect();


                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();

                    }


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;


                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    result = sb.toString().trim();


                } catch (Exception e) {
                    result = e.toString();
                }



                if (jsonParser(result)){

                    Message message = mHandler.obtainMessage(LOAD_SUCCESS);
                    mHandler.sendMessage(message);
                }
            }

        });
        thread.start();
    }

    String Jshared = "filed";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean jsonParser(String jsonString){

        if (jsonString == null ) return false;

        TextView tx_name = findViewById(R.id.area_name);

        SharedPreferences JsharedPreferences = getSharedPreferences(Jshared, 0);
        SharedPreferences.Editor Jeditor = JsharedPreferences.edit();
        Jeditor.putString("jsonstring", jsonString);
        Jeditor.commit();

        try {
            JSONObject jsonobject = new JSONObject(jsonString);
            JSONObject CityName = jsonobject.getJSONObject("city");
            String name = CityName.getString("name");
            tx_name.setText(name);

            JSONArray list = jsonobject.getJSONArray("list");

            photoinfoList.clear();

            for (int i = 0; i < list.length(); i++) {
                JSONObject listInfo = list.getJSONObject(i);
                JSONObject Wmain = listInfo.getJSONObject("temp");
                JSONArray weather = listInfo.getJSONArray("weather");
                JSONObject weatherInfo = weather.getJSONObject(0);

                String dt = listInfo.getString("dt");
                String min = Wmain.getString("min");
                String max = Wmain.getString("max");
                String main = weatherInfo.getString("main");

                final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE MM월 dd일");
                long time = Long.parseLong(dt);
                final String date = Instant.ofEpochSecond(time).atZone(ZoneId.of("GMT+9")).format(dateTimeFormatter);

                double min_int = Double.parseDouble(min);
                double centi = Math.round(min_int);
                int m = (int)centi;
                double max_int = Double.parseDouble(max);
                double centiM = Math.round(max_int);
                int M = (int)centiM;

                HashMap<String, String> photoinfoMap = new HashMap<String, String>();
                photoinfoMap.put("dt", date);
                photoinfoMap.put("min", String.valueOf(m));
                photoinfoMap.put("max", String.valueOf(M));
                photoinfoMap.put("main", main);

                photoinfoList.add(photoinfoMap);

            }

            return true;
        } catch (JSONException e) {

            Log.d(TAG, e.toString() );
        }

        return false;
    }
}