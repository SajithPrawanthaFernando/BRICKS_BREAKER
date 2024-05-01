package com.example.myapplication8

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class trophyPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_you_won)

        val goBackButton = findViewById<Button>(R.id.dId)
        goBackButton.setOnClickListener {
            val intent = Intent(this, firstPage::class.java)
            startActivity(intent)
        }
    }
}
