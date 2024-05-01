

    package com.example.myapplication8

    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import androidx.appcompat.app.AppCompatActivity

    class firstPage  : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.firstpage)
        }

        // Method to start the MainActivity when the button is clicked
        fun startGame(view: View) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
