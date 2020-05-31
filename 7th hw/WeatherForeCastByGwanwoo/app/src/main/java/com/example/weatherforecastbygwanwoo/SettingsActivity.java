package com.example.weatherforecastbygwanwoo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    private Button btn;
    private EditText searchKeyword = null;
    String shared = "file";
    private String str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        searchKeyword = (EditText)findViewById(R.id.edittext_main_searchkeyword);
        SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
        String value = sharedPreferences.getString("Area", "");
        searchKeyword.setText(value);

        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                str = searchKeyword.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("str", str);
                startActivity(intent);
                SharedPreferences sharedPreferences = getSharedPreferences(shared, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String value = searchKeyword.getText().toString();
                editor.putString("Area", value);
                editor.commit();
            }
        });

    }

}
