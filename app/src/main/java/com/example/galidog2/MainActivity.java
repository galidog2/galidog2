package com.example.galidog2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On rend les boutons non cliquables (modes non pris en charge par Galidog2)
        findViewById(R.id.navigation).setClickable(false);
        findViewById(R.id.description).setClickable(false);

        Button memorisationB = (Button)findViewById(R.id.mémorisation);
        memorisationB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChoixMemorisation.class);
                startActivity(intent);
            }
        });
    }
}
