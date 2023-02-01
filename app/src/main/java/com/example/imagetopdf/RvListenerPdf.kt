package com.example.imagetopdf

import com.example.imagetopdf.data.PdfModel
import com.example.imagetopdf.ui.adapters.PdfAdapter

interface RvListenerPdf {
    fun onPdfClick(pdfModel: PdfModel, position: Int)
    fun onPdfMoreClick(pdfModel: PdfModel, position: Int, holder: PdfAdapter.PdfHolder)

}