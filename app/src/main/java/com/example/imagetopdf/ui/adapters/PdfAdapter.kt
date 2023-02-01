package com.example.imagetopdf.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.imagetopdf.Methods
import com.example.imagetopdf.R
import com.example.imagetopdf.RvListenerPdf
import com.example.imagetopdf.data.PdfModel
import java.util.concurrent.Executors

class PdfAdapter(
    private val context: Context,
    private val pdfArrayList: ArrayList<PdfModel>,
    private val rvListenerPdf: RvListenerPdf
) : RecyclerView.Adapter<PdfAdapter.PdfHolder>() {

    companion object {
        private const val TAG = "ADAPTER_PDF_TAG"
    }

    inner class PdfHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbnailIv: ImageView = itemView.findViewById(R.id.thumbnailIv)
        var nameTv: TextView = itemView.findViewById(R.id.nameTv)
        var pagesTv: TextView = itemView.findViewById(R.id.pagesTv)
        var sizeTv: TextView = itemView.findViewById(R.id.sizeTv)
        var moreBtn: ImageButton = itemView.findViewById(R.id.moreBtn)
        var dateTv: TextView = itemView.findViewById(R.id.dateTv)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_pdf, parent, false)
        return PdfHolder(view)
    }

    override fun onBindViewHolder(holder: PdfHolder, position: Int) {
        val pdfModel = pdfArrayList[position]
        val name = pdfModel.file.name
        val timestamp = pdfModel.file.lastModified()
        val formattedDate: String = Methods.formatTimestamp(timestamp)

        loadThumbnailFromPdf(pdfModel, holder)
        loadFileSize(pdfModel, holder)

        //set data to ui views
        holder.nameTv.text = name
        holder.dateTv.text = formattedDate

        holder.itemView.setOnClickListener {
            rvListenerPdf.onPdfClick(pdfModel, position)
        }

        holder.moreBtn.setOnClickListener{
            rvListenerPdf.onPdfMoreClick(pdfModel, position, holder)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    private fun loadFileSize(pdfModel: PdfModel, holder: PdfHolder) {
        val bytes: Double = pdfModel.file.length().toDouble()
        val kb = bytes / 1024
        val mb = kb / 1024
        var size = ""

        if (mb >= 1) {
            size = String.format("%.2f", mb) + " MB"
        } else if (kb >= 1) {
            size = String.format("%.2f", kb) + " KB"
        } else {
            size = String.format("%.2f", bytes) + " bytes"
        }

        Log.d(TAG, "loadFileSize: Size: $size")
        holder.sizeTv.text = size
    }

    @SuppressLint("SetTextI18n")
    private fun loadThumbnailFromPdf(pdfModel: PdfModel, holder: PdfHolder) {
        //init executorService for bg processing
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        var pageCount = 0
        executorService.execute {
            var thumbnailBitmap: Bitmap? = null
            try {

                val parcelFileDescriptor =
                    ParcelFileDescriptor.open(pdfModel.file, ParcelFileDescriptor.MODE_READ_ONLY)
                //pdf renderer used to render pdf
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                pageCount = pdfRenderer.pageCount

                if (pageCount <= 0) {
                    //no pgs in pdf
                    Log.d(TAG, "loadThumbnailFromPdf: No pages")

                } else {
                    val currentPage = pdfRenderer.openPage(0)
                    thumbnailBitmap = Bitmap.createBitmap(
                        currentPage.width,
                        currentPage.height,
                        Bitmap.Config.ARGB_8888
                    )
                    currentPage.render(
                        thumbnailBitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                }
            }
            catch (e: java.lang.Exception) {
                Log.e(TAG, "loadThumbnailFromPdf: ", e)
            }

            handler.post {
                Glide.with(context)
                    .load(thumbnailBitmap)
                    .fitCenter()
                    .placeholder(R.drawable.ic_pdf_gray)
                    .into(holder.thumbnailIv)
                holder.pagesTv.text = "$pageCount Pages"
            }
        }
    }

}