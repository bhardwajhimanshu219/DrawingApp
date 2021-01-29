    package com.example.drawingproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brush_type.*
import kotlinx.android.synthetic.main.brushsize.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        drawing_view.setSizeForBrush(20.toFloat())
        id_brush_thickness.setOnClickListener {
            showSeekBarChooserDialog("Brush Size")
        }
        id_opacity.setOnClickListener {
            showSeekBarChooserDialog("Opacity")
        }
        undo_button.setOnClickListener {
                drawing_view.onClickUndo()
            }
        id_color_pallete_button.setOnClickListener {
            dialog()
        }
        save_button.setOnClickListener {
            if (isReadStorageAllowed()) {
                BitmapAsyncTask(getBitmapFromView(drawing_view),"save").execute()
            } else {
                requestStoragePermission()
            }
        }
        id_brush_type.setOnClickListener {
            showBrushTypeChooserDialog()
        }
    share_button.setOnClickListener {
        if (isReadStorageAllowed()) {
            BitmapAsyncTask(getBitmapFromView(drawing_view),"share").execute()
        } else {
            requestStoragePermission()
        }

}

        zoom_control.setOnZoomInClickListener (View.OnClickListener {
                val x:Float = drawing_view.scaleX
                val y:Float = drawing_view.scaleY
                 drawing_view.scaleX = x+1
                drawing_view.scaleY = y+1

        })
        zoom_control.setOnZoomOutClickListener (View.OnClickListener {
            val x:Float = drawing_view.scaleX
            val y:Float = drawing_view.scaleY
            if (x>1f && y>1f){
                drawing_view.scaleX = x-1
                drawing_view.scaleY = y-1
            }
        })

    }

    private fun showBrushTypeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.brush_type)
        val smallBtn = brushDialog.ib_brush1
        smallBtn.setOnClickListener(View.OnClickListener {
//            drawing_view.setErase(false)
            drawing_view.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        })
        val mediumBtn = brushDialog.ib_brush2
        mediumBtn.setOnClickListener(View.OnClickListener {
//            drawing_view.setErase(false)
            drawing_view.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        })

        val largeBtn = brushDialog.ib_brush3
        largeBtn.setOnClickListener(View.OnClickListener {
//            drawing_view.setErase(false)
            drawing_view.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        })

        val giantBtn = brushDialog.ib_brush4
        giantBtn.setOnClickListener(View.OnClickListener {
//            drawing_view.setErase(false)
            drawing_view.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        })

        val eraser = brushDialog.eraser
        eraser.setOnClickListener(View.OnClickListener {
           drawing_view.drawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            brushDialog.dismiss()
            drawing_view.setColor(Color.WHITE)
        })
        brushDialog.show()
    }
    private fun showSeekBarChooserDialog(value:String) {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.brushsize)
        val seekBar = brushDialog.seekbar
        val textView = brushDialog.dialog_title
        textView.text = "Choose " + value
        seekBar?.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
            var progressChanged:Int = 0
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                progressChanged = progress
            }
            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }
            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                Toast.makeText(this@MainActivity,
                        value +" is: " + seek.progress + "%",
                        Toast.LENGTH_SHORT).show()
                if (value=="Brush Size")
                drawing_view.setSizeForBrush(progressChanged.toFloat())
                else
                    drawing_view.setPaintAlpha(progressChanged)
            }
        })
        brushDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@MainActivity,
                    "Permission granted now you can read the storage files.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Oops you just denied the permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }
    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {

            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }
    @SuppressLint("StaticFieldLeak")
    private inner class BitmapAsyncTask(val mBitmap: Bitmap? , val value: String) : AsyncTask<Any, Void, String>() {
        @Suppress("DEPRECATION")
        private var mDialog: ProgressDialog? = null
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any): String {
            var result = ""
            if (mBitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                            externalCacheDir!!.absoluteFile.toString()
                                    + File.separator + "KidDrawingApp_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )
                    val fo =
                            FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (result.isNotEmpty()) {
                Toast.makeText(
                        this@MainActivity,
                        "File saved successfully :$result",
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                        this@MainActivity,
                        "Something went wrong while saving the file.",
                        Toast.LENGTH_SHORT
                ).show()
            }
            if (value == "share") {
                MediaScannerConnection.scanFile(
                        this@MainActivity, arrayOf(result), null
                ) { path, uri ->
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(
                            Intent.EXTRA_STREAM,
                            uri)
                    shareIntent.type =
                            "image/jpeg"
                    startActivity(
                            Intent.createChooser(
                                    shareIntent,
                                    "Share"
                            )
                    )
                }
            }
        }
        private fun showProgressDialog() {
            @Suppress("DEPRECATION")
            mDialog = ProgressDialog.show(
                this@MainActivity,
                "",
                "Saving your image..."
            )
        }
        private fun cancelProgressDialog() {
            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {

        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun dialog() {
        val builder = ColorPickerDialog.Builder(this)
            .setTitle("ColorPicker Dialog")
            .setPreferenceName("Test")
            .setPositiveButton(
                "confirm",
                ColorEnvelopeListener { envelope: ColorEnvelope?, fromUser: Boolean ->
                    val hexColor = java.lang.String.format("#%06X", 0xFFFFFF and envelope?.color!!)
                    drawing_view.setColor(envelope.color)
                    drawing_view.setPaintAlpha(envelope.color)
                }
            )
            .setNegativeButton(
                "cancel"
            ) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        builder.colorPickerView.flagView = BubbleFlag(this)
        builder.show()
    }
}













