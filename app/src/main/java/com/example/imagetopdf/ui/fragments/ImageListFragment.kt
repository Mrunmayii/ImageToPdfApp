package com.example.imagetopdf.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.imagetopdf.Constants
import com.example.imagetopdf.R
import com.example.imagetopdf.data.ImageModel
import com.example.imagetopdf.databinding.FragmentImageListBinding
import com.example.imagetopdf.ui.adapters.ImageAdapter
import java.io.File
import java.io.FileOutputStream


class ImageListFragment : Fragment() {


    private var _binding: FragmentImageListBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>
    private lateinit var allImagesArrayList: ArrayList<ImageModel>
    private lateinit var imageAdapter : ImageAdapter

    //uri of img picked
    private var imageUri: Uri? = null

    companion object{
        private const val TAG ="IMAGE_LIST_TAG"
        private const val STORAGE_REQ_CODE = 100
        private const val CAMERA_REQ_CODE = 101

    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        _binding = FragmentImageListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraPermissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        storagePermissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //show input image dialog
        binding.addImageFab.setOnClickListener {
            showInputImageDialog()
        }
        loadImages()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    //inflate menu_images.xml
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_images, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    //handle menu_images.xml iteam clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.delete_image){
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("Delete Image(s)")
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Delete All"){ dialog, which ->
                    deleteImages(true)
                }
                .setNeutralButton("Delete Selected"){ dialog, which ->
                    deleteImages(false)
                }
                .setNegativeButton("Cancel"){ dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        else if(item.itemId == R.id.image_pdf_item){
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("Convert to PDF")
                .setMessage("Convert All/Selected to PDF")
                .setPositiveButton("Convert All"){ dialog, which ->
                    convertToPDF(true)
                }
                .setNeutralButton("Convert Selected"){ dialog, which ->
                    convertToPDF(false)
                }
                .setNegativeButton("Cancel"){ dialog, which->
                    dialog.dismiss()
                }
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun convertToPDF(convertAll: Boolean){

    }

    private fun deleteImages(deleteAll: Boolean){
        //init separate array list of images to delete
        var imagesToDeleteList = ArrayList<ImageModel>()
        if(deleteAll){
            val folder = File(mContext.getExternalFilesDir(null), Constants.IMAGES_FOLDER)
            if(folder.exists()){
                folder.deleteRecursively()
            }
        } else{
            for(img in allImagesArrayList){
                //check if image selected
                if(img.checked){
                    //img selected added to list
                    try{
                        //get the path of image to delete
                        val path = "${img.imageUri.path}"
                        val file = File(path)
                        if(file.exists()){
                            val isDeleted = file.delete()
                        }
                    }
                    catch (e: Exception){
                        Log.d(TAG, "loadImages: ")
                    }
                }
            }
        }
        if(allImagesArrayList.isNotEmpty()){
            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
        }
        loadImages()
    }

    private fun loadImages(){
        allImagesArrayList = ArrayList()
        imageAdapter = ImageAdapter(mContext, allImagesArrayList)
        binding.imagesRv.adapter = imageAdapter

        val folder = File(mContext.getExternalFilesDir(null), Constants.IMAGES_FOLDER)
        if(folder.exists()) {
            val files = folder.listFiles()
            if(files != null) {
                Log.d(TAG, "folder has files")
                for(file in files) {
                    Log.d(TAG, "fileName: ${file.name}")

                    val imageUri = Uri.fromFile(file)
                    val imageModel = ImageModel(imageUri, false)
                    allImagesArrayList.add(imageModel)
                    imageAdapter.notifyItemInserted(allImagesArrayList.size)
                }
            }
            else {
                Log.d(TAG, "folder exists but no files")
            }
        }
        else {
            Log.d(TAG, "folder doesn't exist")

        }
    }

    private fun saveImageToAppLevelDirectory(imageUriToBeSaved: Uri){
        try{
            //get bitmap from image uri
            val bitmap:Bitmap
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(
                    mContext.contentResolver, imageUriToBeSaved)
                )
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(
                    mContext.contentResolver, imageUriToBeSaved
                )
            }

            //create folder to save image
            //this doesnt require any storage permissions
            //not accessible by any other app
            val directory = File(
                mContext.getExternalFilesDir(null),
                Constants.IMAGES_FOLDER
            )
            directory.mkdirs() // create directory if not existing already

            val timestamp = System.currentTimeMillis()
            val fileName = "$timestamp.jpeg"

            //sub folder and file name to be saved
            val file = File(mContext.getExternalFilesDir(null),"${Constants.IMAGES_FOLDER}/$fileName")
            //save img
            try{
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
                fos.close()
                Log.d(TAG, "saveIMGToAppdirectory: Saved")
                Toast.makeText(mContext, "Image Saved", Toast.LENGTH_SHORT).show()
            }
            catch (e: Exception){
                Log.e(TAG, "saveIMGtoAPPdirectory: ", e)
                Log.d(TAG, "saveIMGtoAPPdirectory: ${e.message}")
                Toast.makeText(mContext, "Failed to save image due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
        }

        catch(e: java.lang.Exception){
            Log.e(TAG, "saveIMGtoAPPdirectory",e)
            Log.d(TAG, "saveIMGtoAPPdirectory: ${e.message}")
            Toast.makeText(mContext, "Failed to prepare image due to ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInputImageDialog(){
        val popUpMenu = PopupMenu(mContext, binding.addImageFab)
        popUpMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popUpMenu.menu.add(Menu.NONE,2,2,"GALLERY")
        popUpMenu.show()
        popUpMenu.setOnMenuItemClickListener { menuItem->
            val itemId = menuItem.itemId
            if(itemId == 1){
                //camera is clicked check if permissions granted or not
                if (checkCameraPermission()){
                    pickImageCamera()
                }
                else{
                    reqCameraPermission()
                }
            }
            else if( itemId == 2){
                //gallery is clicked check if storage permissions granted or not
                if(checkStoragePermission()){
                    pickImageGallery()
                }
                else{
                    reqStoragePermission()
                }
            }
            return@setOnMenuItemClickListener true
        }

    }


    private fun pickImageGallery(){
        Log.d(TAG, "pickImageGallery: ")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    )
    { result ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            Log.d(TAG, "Gallery img: $imageUri")
            saveImageToAppLevelDirectory(imageUri!!)

            //notify adapter that new img inserted
            val imageModel = ImageModel(imageUri!!, false)
            allImagesArrayList.add(imageModel)
            imageAdapter.notifyItemInserted(allImagesArrayList.size)

        }
        else{
            Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageCamera(){
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP IMAGE TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP IMAGE DESC")
        imageUri = mContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
//            val data = result.data
//            imageUri = data!!.data
            Log.d(TAG, "Camera img: $imageUri")

            //save image
            saveImageToAppLevelDirectory(imageUri!!)

            //notify adapter that new img inserted
            val imageModel = ImageModel(imageUri!!, false)
            allImagesArrayList.add(imageModel)
            imageAdapter.notifyItemInserted(allImagesArrayList.size)
        }
        else{
            Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }


    //cam and storage permissions part
    private fun checkStoragePermission(): Boolean{
        Log.d(TAG, "checkStoragePerms")
        return ContextCompat.checkSelfPermission(
            mContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun reqStoragePermission(){
        Log.d(TAG, "reqStoragePerms")
        requestPermissions(storagePermissions, STORAGE_REQ_CODE)
    }

    private fun checkCameraPermission(): Boolean{
        Log.d(TAG, "checkCameraPermission")
        val cameraResult = ContextCompat.checkSelfPermission(
            mContext,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(
            mContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }

    private fun reqCameraPermission(){
        Log.d(TAG, "reqStoragePerms")
        requestPermissions(cameraPermissions, CAMERA_REQ_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: ")

        when(requestCode){
            CAMERA_REQ_CODE ->{
                if(grantResults.isNotEmpty()) {
                    val camAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if(camAccepted && storageAccepted){
                        Log.d(TAG, "onRequestPermissionsResult:Both perms granted ")
                        pickImageCamera()
                    }
                    else{
                        Toast.makeText(
                            mContext,
                            "Camera & Storage permissions required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else{
                    Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQ_CODE ->{
                //check if some action from permission dialog is performed or not
                if(grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted){
                        //storage perms granted, launch gallery
                        Log.d(TAG, "onRequestPermissionsResult:Storage perms granted ")
                        pickImageGallery()
                    }
                    else{
                        //permission denied
                        Toast.makeText(
                            mContext,
                            "Storage permissions required",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else{
                    //neither allowed nor denied, but cancelled
                    Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}