package com.example.imagetopdf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.imagetopdf.databinding.ActivityImageViewBinding
import com.example.imagetopdf.databinding.ActivityMainBinding

class ImageViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageViewBinding
    private var imageUri = ""

    companion object {
        private const val TAG = "IMAGE_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Image View"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        imageUri = intent.getStringExtra("imageUri").toString()
        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.ic_image_black)
            .into(binding.imageTv)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}