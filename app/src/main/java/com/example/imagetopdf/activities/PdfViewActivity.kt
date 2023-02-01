package com.example.imagetopdf.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.imagetopdf.data.PdfViewModel
import com.example.imagetopdf.databinding.ActivityPdfViewBinding
import com.example.imagetopdf.ui.adapters.PdfViewAdapter
import java.io.File
import java.util.concurrent.Executors

class PdfViewActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "PDF_VIEW_TAG"
    }
    private lateinit var binding: ActivityPdfViewBinding

    private var pdfUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.title = "PDF View"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        pdfUri = intent.getStringExtra("pdfUri").toString()
        Log.d(TAG, "onCreate: pdfUri: $pdfUri")
        loadPdfPages()
    }

    private var currPage: PdfRenderer.Page? = null

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPdfPages() {
        Log.d(TAG, "loadPdfPages: ")
        val pdfViewArrayList: ArrayList<PdfViewModel> = ArrayList()
        val pdfViewAdapter = PdfViewAdapter(this, pdfViewArrayList)

        binding.pdfViewRv.adapter = pdfViewAdapter

        val file = Uri.parse(pdfUri).path?.let { File(it) }
        try{
            //set file name as subtitle of action bar
            supportActionBar?.setSubtitle(file?.name)
        } catch (e: Exception){
            //failed to set file name as subtitle
            Log.e(TAG, "loadPdfPages: ", e)
        }

        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            try{
                val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                val pageCount = pdfRenderer.pageCount

                if(pageCount <=0){
                    Log.d(TAG, "loadPdfPages: No pages in PDf")

                } else{
                    Log.d(TAG, "loadPdfPages: Pages are: $pageCount")
                    for(i in 0 until pageCount){
                        if(currPage != null){
                            currPage?.close()
                        }

                        currPage = pdfRenderer.openPage(i)
                        val bitmap = Bitmap.createBitmap(
                            currPage!!.width,
                            currPage!!.height,
                            Bitmap.Config.ARGB_8888
                        )

                        currPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        val pdfModel = PdfViewModel(Uri.parse(pdfUri), (i+1), pageCount, bitmap)
                        pdfViewArrayList.add(pdfModel)
                    }
                }

            }catch (e: Exception){
                Log.e(TAG, "loadPdfPages: ", e )
            }

            handler.post{

                Log.d(TAG, "loadPdfPages: UI thread..")
                pdfViewAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}