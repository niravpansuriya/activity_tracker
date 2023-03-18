package com.example.activitytracker

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.annotation.RequiresApi
import org.w3c.dom.Text
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    lateinit var greetingView: TextView;
    lateinit var dateView: TextView;
    lateinit var timeView: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        greetingView = findViewById(R.id.greetingView)
        dateView = findViewById(R.id.dateView)
        timeView = findViewById(R.id.timeView)

        greetingView.setText("Good ${getTimeofDay()}")
        dateView.setText(getCurrentDate())

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                timeView.setText(getCurrentTime())
                //Call your function here
                handler.postDelayed(this, 1000)//1 sec delay
            }
        }, 0)
    }

    fun getTimeofDay(): String{
        val hour = getHour()

        if(hour >= 6 && hour < 12) {
            return "monning"
        }
        else if(hour >= 12 && hour < 16) {
            return "afternoon"
        }
        else if(hour >= 16 && hour < 20){
            return "evening"
        }
        else {
            return "night"
        }
    }

    fun getCurrentDate(): String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        return formatted
    }

    fun getCurrentTime(): String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatted = current.format(formatter)
        return formatted
    }

    fun getHour(): Int{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH")
        val formatted = current.format(formatter)

        return formatted.toInt()
    }
}