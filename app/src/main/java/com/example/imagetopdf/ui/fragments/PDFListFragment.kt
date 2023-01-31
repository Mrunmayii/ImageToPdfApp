package com.example.imagetopdf.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.imagetopdf.Constants
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

        pdfRv = binding.pdfRv
        loadPdfDocuments()
    }

    private fun loadPdfDocuments(){
        Log.d(TAG, "loadPdfDocuments: ")
        pdfArrayList = ArrayList()
        pdfAdapter = PdfAdapter(mContext, pdfArrayList)
        binding.pdfRv.adapter = pdfAdapter

        val folder = File(mContext.getExternalFilesDir(null), Constants.PDF_FOLDER)
        if(folder.exists()){

            val files = folder.listFiles()
            Log.d(TAG, "loadPdfDocuments: FileCnt:  ${files.size}")
            for(file in files){
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
}