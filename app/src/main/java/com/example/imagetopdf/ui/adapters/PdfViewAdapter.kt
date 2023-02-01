package com.example.imagetopdf.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.imagetopdf.R
import com.example.imagetopdf.data.PdfViewModel

class PdfViewAdapter(
    private val context: Context,
    private val pdfViewArrayList: ArrayList<PdfViewModel>
   ) :androidx.recyclerview.widget.RecyclerView.Adapter<PdfViewAdapter.PdfViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewAdapter.PdfViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pdf_card, parent, false)
        return PdfViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val pdfViewModel = pdfViewArrayList[position]
        val pageNumber = position+1
        val bitmap = pdfViewModel.bitmap

        //set bitmap to imageView
        Glide.with(context)
            .load(bitmap)
            .placeholder(R.drawable.ic_image_black)
            .into(holder.imageIv)
        holder.pageNumberTv.text = "$pageNumber"
    }

    override fun getItemCount(): Int {
        return pdfViewArrayList.size
    }

    inner class PdfViewHolder(itemView: View) : ViewHolder(itemView){
        var pageNumberTv: TextView = itemView.findViewById(R.id.pageNumberTv)
        var imageIv: ImageView = itemView.findViewById(R.id.imageIv)
    }
}