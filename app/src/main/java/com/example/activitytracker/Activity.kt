package com.example.activitytracker

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Activity : AppCompatActivity(), SensorEventListener {

    private lateinit var activityImageView: ImageView;
    private lateinit var activityTypeView: TextView;
    private lateinit var motivationalQuoteView: TextView;
    private lateinit var analyticsButton: ImageButton;
    private lateinit var mapButton: ImageButton;

    private lateinit var sensorManager: SensorManager
    private lateinit var accelSensor: Sensor


    // this stores activity details like start time of the activity, date, type etc
    /**
     * start time
     * date
     * activityType
     * datetime object
     */
    private val activityMap: MutableMap<String, Any> = mutableMapOf();

    private var accelValues: FloatArray = floatArrayOf(0f, 0f, 0f)
    private var accelMagnitude = 0f
    private var pastActivity: String = "";
    private var pastValues: MutableList<Float> =  mutableListOf<Float>();
    private var currAvg: Float = 0f;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        // init variables
        activityImageView = findViewById(R.id.activityImage);
        activityTypeView = findViewById(R.id.activityType)
        motivationalQuoteView = findViewById(R.id.motivationQuote)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        analyticsButton = findViewById(R.id.analyticsButton);
        analyticsButton.setOnClickListener {
            startActivity(Intent(this, Analytics::class.java));
        }

        mapButton = findViewById(R.id.mapButton)
        mapButton.setOnClickListener {
            startActivity(Intent(this,  Map::class.java));
        }
    }

    // show toast when activity changes to showcase the duration of the
    // past activity
    fun showToast(message: String){
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        val view = toast.view
        view?.setBackgroundColor(ContextCompat.getColor(this, R.color.toast_background_color))
        val text = view?.findViewById<TextView>(android.R.id.message)
        text?.setTextColor(ContextCompat.getColor(this, R.color.toast_text_color))
        text?.typeface = ResourcesCompat.getFont(this, R.font.goldplay_semi_bold)
        toast.show()
    }

    // it converts the duration seconds to readable format
    // ex. 130 seconds -> 2 minutes 10 seconds
    fun convertDurationInReadableFormat(duration: Int):String{
        var minutes = 0;
        var hours = 0;
        var seconds = duration;

        if(duration >= 60){
            minutes = duration/60;
            seconds = duration%60;
        }

        if(minutes >= 60){
            hours = minutes/60;
            minutes = minutes%60;
        }

        var ans = "";

        if(hours > 0){
            ans += "$hours hours "
        }
        if(minutes > 0){
            ans += "$minutes minutes "
        }

        ans += "$seconds seconds"

        println("ans " + ans)

        return ans;
    }

    // update the overall average based on the current sensor reading
    fun updateAverage(avg: Float){
        var size = pastValues.size;

        if(size == 200){
            // remove the first element of the list
            var ele = pastValues[0];

            currAvg = (currAvg * pastValues.size - ele + avg)/ pastValues.size

            pastValues.removeAt(0);
            pastValues.add(avg);
        }
        else{
            currAvg = (currAvg * pastValues.size + avg) / (pastValues.size + 1)
            pastValues.add(avg);

        }
    }

    // update the acitvity image based on the current activity
    fun updateActivityImage(activityType:String){
        var quote: String = when (activityType) {
            "standing" -> ""
            "walking" -> "Take the first step towards running and leave your doubts behind!"
            "running" -> "Every step you take brings you closer to your goal."
            else -> "Drive safe, arrive happy."
        }
        motivationalQuoteView.text = quote;
    }

    // update the quote on the screen based on the current activity
    fun updateMotivationQuote(activityType:String){
        var imageId: Int = when (activityType) {
            "standing" -> R.drawable.still_person
            "walking" -> R.drawable.walking_person
            "running" -> R.drawable.running_person
            else -> R.drawable.driving_person
        }
        activityImageView.setImageResource(imageId)
    }

    fun addDataInDatabase(){

        // get db helper object, which contains functions for sqlite db
        val dbHelper = DatabaseHelper(this);

        // get duration of the past activity
        val durationSeconds: Int = Duration.between( activityMap["datetimeObject"] as LocalDateTime, LocalDateTime.now()).toMillis().toInt() / 1000

        // if duration is less than 3 seconds, we will avoid it as it might be
        // because of the wrong reading
        if(durationSeconds <= 3)    return;

        // get duration in minutes
        val durationMinutes: Int =  durationSeconds / 60 ;

        // add data in database
        dbHelper.addDataInDatabase(activityMap["activityType"] as String,
            activityMap["date"] as String,
            activityMap["startTime"] as String,
            durationMinutes);

        // make toast
        if(activityMap["activityType"] != "standing"){

            var toast: String = "You have "
            if(activityMap["activityType"] == "walking")    toast += "walked "
            else if(activityMap["activityType"] == "running") toast += "ran "
            else toast += "drove a vehicle "
            toast += "for ${convertDurationInReadableFormat(durationSeconds)}"
            showToast(toast)
        }

    }

    // this is to update the activity and add data in sqlite database
    fun updateActivityMap(activityType:String){
        if(activityMap.size != 0){

            // add data in database
            addDataInDatabase();
        }

        // update new acitivity data
        activityMap["startTime"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        activityMap["date"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/YYYY"));
        println("date"+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/YYYY")));
        activityMap["activityType"] = activityType;
        activityMap["datetimeObject"] = LocalDateTime.now();
    }


    // we are using linear acceleration sensor
    // when sensor data will be changed, this function will be called
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {

            // Get linear acceleration values
            accelValues = event.values.clone()

            // Calculate acceleration magnitude
            accelMagnitude = Math.sqrt((accelValues[0] * accelValues[0] + accelValues[1] * accelValues[1] + accelValues[2] * accelValues[2]).toDouble()).toFloat()

            // update the average acceleration magnitude
            // we are taking past 200 readings
            // this to avoid the small changes in the sensor reading
            // and we can recognize the activity based on whole pattern, not on single reading
            updateAverage(accelMagnitude);


            // Determine user activity
            if (currAvg < 1.5f) {
                if(pastActivity == "running"){
                    stopAudio();
                }
                if(pastActivity != "standing"){
                    // update map
                    updateActivityMap("standing");
                }
                pastActivity = "standing";
                activityTypeView.text = "Still";
                updateActivityImage("standing");
                updateMotivationQuote("standing");
            } else if (currAvg > 1.5f && currAvg < 3f) {
                if(pastActivity == "running"){
                    stopAudio();
                }
                if(pastActivity != "walking"){
                    // update map
                    updateActivityMap("walking");
                }
                pastActivity = "walking";
                activityTypeView.text = "Walking";
                updateActivityImage("walking");
                updateMotivationQuote("walking");

            } else if(currAvg > 1.5f && currAvg < 15f) {
                if(pastActivity != "running"){
                    playAudio(this);
                }
                if(pastActivity != "running"){
                    // update map
                    updateActivityMap("running");
                }
                pastActivity = "running"
                activityTypeView.text = "Running";
                updateActivityImage("running");
                updateMotivationQuote("running");

            }else{
                if(pastActivity != "running"){
                    stopAudio();
                }
                if(pastActivity != "driving"){
                    // update map
                    updateActivityMap("driving");
                }
                pastActivity = "driving"
                activityTypeView.text = "Driving";
                updateActivityImage("driving");
                updateMotivationQuote("driving");

            }
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this,accelSensor,
            SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this)
    }

}