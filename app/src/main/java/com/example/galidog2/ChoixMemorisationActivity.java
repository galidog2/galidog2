package com.example.galidog2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChoixMemorisationActivity extends GenericActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choix_memorisation);

        Button mapB = (Button)findViewById(R.id.map);
        mapB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChoixMemorisationActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
    }
}
