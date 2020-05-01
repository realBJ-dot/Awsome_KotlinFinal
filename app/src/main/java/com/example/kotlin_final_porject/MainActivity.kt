package com.example.kotlin_final_porject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.kotlin_final_project.R
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val mCameraView: SurfaceView = findViewById(R.id.surfaceView)
    private val mTextView: TextView = findViewById(R.id.text_view)
    private val TAG = "MainActivity"
    private val requestPermissionID = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startCameraSource()

    }
    private fun startCameraSource() {

        //Create the TextRecognizer
        val textRecognizer = TextRecognizer.Builder(applicationContext).build()
        if (!textRecognizer.isOperational) {
            Log.w(TAG, "Detector dependencies not loaded yet")
        } else {

            //Initialize camerasource to use high resolution and set Autofocus on.
            val mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build()
            /**
             * Add call back to SurfaceView and check if camera permission is granted.
             * If permission is granted we can start our cameraSource and pass it to surfaceView
             */
            mCameraView.getHolder().addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.CAMERA),
                                requestPermissionID
                            )
                            return
                        }
                        mCameraSource.start(mCameraView.getHolder())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                /**
                 * Release resources for cameraSource
                 */
                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    mCameraSource.stop()
                }
            })

            //Set the TextRecognizer's Processor.
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                /**
                 * Detect all the text from camera using TextBlock and the values into a stringBuilder
                 * which will then be set to the textView.
                 */
                override fun receiveDetections(detections: Detections<TextBlock>) {
                    val items = detections.detectedItems
                    if (items.size() != 0) {
                        mTextView.post(Runnable {
                            val stringBuilder =
                                StringBuilder()
                            for (i in 0 until items.size()) {
                                val item = items.valueAt(i)
                                stringBuilder.append(item.value)
                                stringBuilder.append("\n")
                            }
                            mTextView.setText(stringBuilder.toString())
                        })
                    }
                }
            })
        }
    }


}
