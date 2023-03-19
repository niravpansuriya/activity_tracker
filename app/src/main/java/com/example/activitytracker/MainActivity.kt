package com.example.activitytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.RelativeLayout
import android.widget.TextView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : AppCompatActivity() {
    lateinit var greetingView: TextView;
    lateinit var dateView: TextView;
    lateinit var timeView: TextView;
    lateinit var layout: RelativeLayout;

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        greetingView = findViewById(R.id.greetingView)
        dateView = findViewById(R.id.dateView)
        timeView = findViewById(R.id.timeView)
        layout = findViewById(R.id.welcomeBackground)

        changeUIBasedOnTime();

        greetingView.setText("Good ${getTimeofDay()}")
        dateView.setText("Today is ${getCurrentDate()}")
        timeView.setText(getCurrentTime())

        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                timeView.setText(getCurrentTime())
                //Call your function here
                handler.postDelayed(this, 5000)//1 sec delay
            }
        }, 0)

        handler.postDelayed({
           startActivity(Intent(this, sensor::class.java))
        }, 2000)

    }

    fun getTimeofDay(): String{
        val hour = getHour()

        if(hour >= 6 && hour < 12) {
            return "morning"
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

    fun changeUIBasedOnTime(){
        val time = getTimeofDay()
        val backgroundColor = when{
            time == "morning" -> "#FEEFB3"
            time == "afternoon" -> "#FFD7B5"
            else -> "#2C3E50"
        }
        val fontColor = when{
            time == "morning"->"#434343"
            time == "afternoon" -> "#434343"
            else -> "#F5F5F5"
        }

        greetingView.setTextColor(Color.parseColor(fontColor));
        dateView.setTextColor(Color.parseColor(fontColor));
        timeView.setTextColor(Color.parseColor(fontColor));
        layout.setBackgroundColor(Color.parseColor(backgroundColor));
    }

    fun getCurrentDate(): String{
        val current = LocalDateTime.now()
        val monthName = current.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val dayOfWeekName = current.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val dayOfMonth = current.dayOfMonth
        return "$dayOfWeekName, $monthName $dayOfMonth"
    }

    fun getCurrentTime(): String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
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