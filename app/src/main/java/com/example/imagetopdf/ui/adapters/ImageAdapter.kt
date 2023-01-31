package com.example.imagetopdf.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.imagetopdf.ImageViewActivity
import com.example.imagetopdf.R
import com.example.imagetopdf.data.ImageModel

class ImageAdapter(
    private val context: Context,
    private val imageArrayList: ArrayList<ImageModel>
) : androidx.recyclerview.widget.RecyclerView.Adapter<ImageAdapter.ImageHolder>() {


    inner class ImageHolder(itemView: View) : ViewHolder(itemView){
        var imageTv = itemView.findViewById<ImageView>(R.id.imageTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_image, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val imgModel = imageArrayList[position]
        val imageUri = imgModel.imageUri

        Glide.with(context)
            .load(imageUri)
            .placeholder(R.drawable.ic_image_black)
            .into(holder.imageTv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ImageViewActivity::class.java)
            intent.putExtra("imageUri","$imageUri")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return imageArrayList.size
    }
}