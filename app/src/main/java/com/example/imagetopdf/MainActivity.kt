package com.example.imagetopdf

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.imagetopdf.databinding.ActivityMainBinding
import com.example.imagetopdf.ui.fragments.ImageListFragment
import com.example.imagetopdf.ui.fragments.PDFListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var btmNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImagesFragment()
//        btmNavigationView = findViewById(R.id.btmNavigationMenu)

        binding.btmNavigationMenu.setOnItemSelectedListener { menuItem ->
            val itemId = menuItem.itemId

            when(itemId){
                R.id.bottom_menu_images ->{
                    loadImagesFragment()
                }
                R.id.bottom_menu_pdfs -> {
                    loadPdfFragment()
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun loadImagesFragment(){
        title = "Images"
        val imageListFragment = ImageListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, imageListFragment, "ImageListFragment")
        fragmentTransaction.commit()
    }

    private fun loadPdfFragment(){
        title = "PDF List"
        val pdfListFragment = PDFListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, pdfListFragment, "PDFListFragment")
        fragmentTransaction.commit()
    }
}