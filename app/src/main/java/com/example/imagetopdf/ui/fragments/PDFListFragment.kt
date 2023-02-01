package com.example.imagetopdf.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdf.utils.Constants
import com.example.imagetopdf.R
import com.example.imagetopdf.utils.RvListenerPdf
import com.example.imagetopdf.activities.PdfViewActivity
import com.example.imagetopdf.data.PdfModel
import com.example.imagetopdf.databinding.FragmentPDFListBinding
import com.example.imagetopdf.ui.adapters.PdfAdapter
import java.io.File

class PDFListFragment : Fragment() {

    private var _binding: FragmentPDFListBinding? = null
    private val binding get() = _binding!!

    private lateinit var pdfArrayList: ArrayList<PdfModel>
    private lateinit var pdfAdapter: PdfAdapter
    private lateinit var mContext: Context
    private lateinit var pdfRv: RecyclerView


    companion object{
        private const val TAG = "PDF_LIST_TAG"
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPDFListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        pdfRv = view.findViewById(R.id.pdfRv)
        pdfRv = binding.pdfRv
        loadPdfDocuments()
    }

    private fun loadPdfDocuments(){
        Log.d(TAG, "loadPdfDocuments: ")
        pdfArrayList = ArrayList()
        pdfAdapter = PdfAdapter(mContext, pdfArrayList, object: RvListenerPdf {
            override fun onPdfClick(pdfModel: PdfModel, position: Int) {
                val intent = Intent(mContext, PdfViewActivity::class.java)
                intent.putExtra("pdfUri", "${pdfModel.uri}")
                startActivity(intent)
            }

            override fun onPdfMoreClick(
                pdfModel: PdfModel,
                position: Int,
                holder: PdfAdapter.PdfHolder
            ) {
                showMoreOptionsDialog(pdfModel, holder)
            }
        })
        pdfRv.adapter = pdfAdapter

        val folder = File(mContext.getExternalFilesDir(null), Constants.PDF_FOLDER)
        if(folder.exists()){

            val files = folder.listFiles()
            Log.d(TAG, "loadPdfDocuments: FileCnt:  ${files.size}")
            for(file in files!!){
                //get uri of file to pass in model
                val uri = Uri.fromFile(file)
                val pdfModel = PdfModel(file, uri)

                pdfArrayList.add(pdfModel)
                pdfAdapter.notifyItemInserted(pdfArrayList.size)
            }
        }else{
            Log.d(TAG, "loadPdfDocuments: No pdf files yet...")
        }
    }

    private fun showMoreOptionsDialog(pdfModel: PdfModel, holder: PdfAdapter.PdfHolder) {
        val popupMenu = PopupMenu(mContext, holder.moreBtn)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Rename")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Delete")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Share")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { it ->
            val itemId = it.itemId
            if(itemId == 0){
                renamePdf(pdfModel)
            }
            else if(itemId == 1){
                deletePdf(pdfModel)
            }
            else if(itemId == 2){
                sharePdf(pdfModel)
            }
            true

        }
    }

    private fun renamePdf(pdfModel: PdfModel){
        val view = LayoutInflater.from(mContext).inflate(R.layout.rename_dialog, null)
        val pdfNewNameEt = view.findViewById<EditText>(R.id.pdfNewNameEt)
        val renameBtn = view.findViewById<Button>(R.id.renameBtn)
        val prevName = "${pdfModel.file.nameWithoutExtension}"
        pdfNewNameEt.setText(prevName)

        val builder = AlertDialog.Builder(mContext)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()

        renameBtn.setOnClickListener {
            val newName = pdfNewNameEt.text.toString().trim()
            Log.d(TAG, "pdfRename: newName: $newName")
            if(newName.isEmpty()){
                Toast.makeText(mContext, "Enter Name", Toast.LENGTH_SHORT).show()
            }
            else{
                try {
                    val newFile = File(mContext.getExternalFilesDir(null), "${Constants.PDF_FOLDER}/$newName.pdf")
                    pdfModel.file.renameTo(newFile)
                    Toast.makeText(mContext, "Renamed Successfully", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    loadPdfDocuments()
                }
                catch (e: Exception){
                    Toast.makeText(mContext, "Failed to Rename due to ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "pdfRename: ", e)
                }

                alertDialog.dismiss()
            }
        }
    }

    private fun deletePdf(pdfModel: PdfModel){
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("Delete File")
            .setMessage("Are you sure you want to delete ${pdfModel.file.name}")
            .setPositiveButton("Delete"){ dialog, which ->
                try{
                    pdfModel.file.delete()
                    Toast.makeText(mContext, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                    loadPdfDocuments()
                }
                catch (e: Exception){
                    Toast.makeText(mContext, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "deletePdf: ", e)
                }
            }
            .setNegativeButton("Cancel"){ dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sharePdf(pdfModel: PdfModel){
        val file = pdfModel.file
        val fileUri = FileProvider.getUriForFile(mContext, "com.example.imagetopdf.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        startActivity(Intent.createChooser(intent, "Share PDF"))
    }

}