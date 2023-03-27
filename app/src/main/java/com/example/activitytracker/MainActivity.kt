package com.example.activitytracker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
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
    lateinit var imageView: ImageView;
    lateinit var layout: RelativeLayout;

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        greetingView = findViewById(R.id.greetingView)
        dateView = findViewById(R.id.dateView)
        timeView = findViewById(R.id.timeView)
        imageView = findViewById(R.id.imageView)
        layout = findViewById(R.id.welcomeBackground)

        // it changes the ui based on the time of the day
        changeUIBasedOnTime();

        // update the text based on time like Good morning, Good afternoon etc.
        greetingView.setText("Good ${getTimeofDay()}")

        // show current date and time
        dateView.setText("Today is ${getCurrentDate()}")
        timeView.setText(getCurrentTime())

        // update the time at every 5 seconds
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                timeView.setText(getCurrentTime())
                //Call your function here
                handler.postDelayed(this, 5000)//5 sec delay
            }
        }, 0)

        // after 2 seconds, go to the Activity activity
        handler.postDelayed({
           startActivity(Intent(this, Activity::class.java))
        }, 2000)

    }

    // it returns the time of the day based on the hours
    // like morning, afternoon etc.
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

    // it changes the appearance of the the welcome screen
    // based on the current time of the day
    fun changeUIBasedOnTime(){
        val time = getTimeofDay()
        val backgroundColor = when{
            time == "morning" -> "#8FB5AA"
            time == "afternoon" -> "#afa493"
            time == "evening" -> "#FFC857"
            else -> "#2C3E50"
        }
        val fontColor = when{
            time == "morning"->"#FFFFFF"
            time == "afternoon" -> "#3D3D3D"
            time == "evening" -> "#FFF2E6"
            else -> "#F5F5F5"
        }

        val resourceId = resources.getIdentifier(time, "drawable", packageName)
        imageView.setImageResource(resourceId)
        greetingView.setTextColor(Color.parseColor(fontColor));
        dateView.setTextColor(Color.parseColor(fontColor));
        timeView.setTextColor(Color.parseColor(fontColor));
        layout.setBackgroundColor(Color.parseColor(backgroundColor));
    }

    // return the current date to show on the welcome screen
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

    // returns the hour of the time
    fun getHour(): Int{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH")
        val formatted = current.format(formatter)

        return formatted.toInt()
    }
}