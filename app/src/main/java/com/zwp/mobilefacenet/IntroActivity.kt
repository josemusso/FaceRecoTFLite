package com.zwp.mobilefacenet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.intro_activity.*
import kotlin.math.log

public class IntroActivity : AppCompatActivity() {

    // para el boton hacia atras
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.intro_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Datos de Ingreso"
        supportActionBar!!.subtitle = "Control de Acceso"

        btAccept.setOnClickListener {

            val intent = Intent(this,
                    MainActivity::class.java)
            startActivity(intent)

        }

    }


}
