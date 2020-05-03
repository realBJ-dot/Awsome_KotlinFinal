package com.example.kotlin_final_porject

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException



class MainActivity : AppCompatActivity() {

    private val mCameraView: SurfaceView = findViewById(R.id.surfaceView)
    private val mTextView: TextView = findViewById(R.id.text_view)
    private val TAG = "MainActivity"
    private val requestPermissionID = 101
    var editText: EditText? = null
    val save: Button = findViewById(R.id.button_save)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.edit_text)
        //var thisText: String = editText.toString()


        val newText: String = mTextView.toString()


        //thisText = newText

        val fileName: String = "example_file"
        save?.setOnClickListener() {
            var fos: FileOutputStream? = null
            try {
                fos = openFileOutput(fileName, Context.MODE_PRIVATE)

                Toast.makeText(
                    this,
                    "it is saved to " + getFilesDir() + "/" + newText,
                    Toast.LENGTH_LONG
                ).show()
                fos.write(newText.toByteArray())
            } catch (e: FileNotFoundException) {
                e.printStackTrace()

            } finally {
                if (fos != null) {
                    fos.close()
                }
            }


        }

        fun startCameraSource() {
            val textRecognizer = TextRecognizer.Builder(applicationContext).build()
            if (!textRecognizer.isOperational) {
                Log.w(TAG, "Detector dependencies not loaded yet")
            } else {

                val mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build()
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
}
