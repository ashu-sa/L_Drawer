package com.example.ldrawer

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialouge_brushsize.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var ibCurrentPaint: ImageButton?=null
    val openGalleryLauncher:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode== RESULT_OK && result.data != null){
            image_fl.setImageURI(result.data?.data)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawing_view?.setSizeBrush(20.toFloat())
        ibCurrentPaint=ll_colours[1] as ImageButton
        ibCurrentPaint?.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_pressed))

        gallery_button.setOnClickListener{
            erase_btn.visibility= View.GONE
            if (isReadPermissonAllowed() && isWritePermissonAllowed()){
           val pickPhotoIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            }else{
                requestPermisson()
            }
        }
        brush_button.setOnClickListener {
            setBrushDialog()
        }
        undo_button.setOnClickListener {
            drawing_view?.onUndoClick()
        }
        save_button.setOnClickListener {
            if (isReadPermissonAllowed() && isWritePermissonAllowed()){
                lifecycleScope.launch(){
                saveBitmapFile(getBitmapFromView(fl_view))
            }

            }


        }
    }

    //Setting Brush and color methods
    private fun setBrushDialog(){
        var brushDialog= Dialog(this)
        brushDialog.setContentView(R.layout.dialouge_brushsize)
        brushDialog.setTitle("Brush Size")

        val smallBtn = brushDialog.small_circle
        smallBtn.setOnClickListener{
            drawing_view.setSizeBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.medium_circle
        mediumBtn.setOnClickListener{
            drawing_view.setSizeBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn = brushDialog.large_circle
        largeBtn.setOnClickListener{
            drawing_view.setSizeBrush(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view !== ibCurrentPaint){   //if is used to make sure view is clicked only imagebutton
            val imageButton = view as ImageButton    //Set that view only be a image Button
            val colorTag = imageButton.tag.toString()
            drawing_view?.setPaintColors(colorTag)
            imageButton.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_pressed))
            ibCurrentPaint?.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallete_normal))
            ibCurrentPaint = view   // if we dont do this then each btn at end will be pressed

        }
    }

    //Permisson Methods

    private fun requestPermisson(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                   Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this, "Permisson Needed", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSON_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== STORAGE_PERMISSON_CODE){
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                val pickIntent= Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)
            } else{
                Toast.makeText(this, "oops! you denied permisson", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun isWritePermissonAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
    private fun isReadPermissonAllowed():Boolean{
        val result=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    //Save Bitmap methods
    private fun getBitmapFromView(view: View):Bitmap{
        val return_bitmap= Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas =Canvas(return_bitmap)
        val bgDrawable = view.background
        if (bgDrawable!=null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        return return_bitmap
    }

    private suspend fun saveBitmapFile(bitmap: Bitmap?):String{
          var result= ""
        withContext(Dispatchers.IO){
            if (bitmap!= null){
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f = File(externalCacheDir?.absoluteFile.toString()+File.separator+"Ldrawer"+System.currentTimeMillis()/1000+".png")
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread{
                        //What should run in Ui thread
                        if (result.isNotEmpty()){
                            Toast.makeText(this@MainActivity, "File saved in : $result", Toast.LENGTH_SHORT).show()
                        }
                    }
                }catch (e:Exception){
                    result =""
                    e.printStackTrace()

                }
            }

        }
        return result
    }



    companion object {
        private const val STORAGE_PERMISSON_CODE = 1
    }
}