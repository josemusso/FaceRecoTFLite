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
import kotlinx.android.synthetic.main.activity_mode.*
import kotlinx.android.synthetic.main.intro_activity.*
import kotlin.math.log

public class ModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mode)

        enrol_btn.setOnClickListener {

            val intent = Intent(this,
                    IntroActivity::class.java)
            startActivity(intent)

        }

        reco_btn.setOnClickListener {

            val intent = Intent(this,
                    RecoActivity::class.java)
            startActivity(intent)

        }

    }


}