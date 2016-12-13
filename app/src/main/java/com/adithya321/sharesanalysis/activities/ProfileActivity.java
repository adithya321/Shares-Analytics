/*
 * Shares Analysis
 * Copyright (C) 2016  Adithya J
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.adithya321.sharesanalysis.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adithya321.sharesanalysis.R;

public class ProfileActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText activityHelloName, activityHelloTarget;
    private Button activityHelloButtonStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        activityHelloName = (EditText) findViewById(R.id.activity_hello_name);
        activityHelloTarget = (EditText) findViewById(R.id.activity_hello_target);
        activityHelloButtonStart = (Button) findViewById(R.id.activity_hello_button_start);

        sharedPreferences = getSharedPreferences("prefs", 0);
        if (!sharedPreferences.getBoolean("first", true)) {
            activityHelloName.setText(sharedPreferences.getString("name", ""));
            activityHelloTarget.setText(String.valueOf(sharedPreferences.getFloat("target", 0)));
        }

        activityHelloButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activityHelloName.getText().toString().trim().equals("")
                        && !activityHelloTarget.getText().toString().trim().equals("")) {
                    editor = sharedPreferences.edit();
                    editor.putString("name", activityHelloName.getText().toString());
                    editor.putFloat("target", Float.parseFloat(activityHelloTarget.getText().toString()));
                    editor.putBoolean("first", false);
                    editor.apply();

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(ProfileActivity.this, "Enter all details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
