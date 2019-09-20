package com.app.maidi.widget

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.app.maidi.utils.Constants
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Event
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement
import org.hisp.dhis.android.sdk.utils.api.ValueType
import java.io.File
import java.io.FileOutputStream


class ExportPDF {

    val checked = "n"
    val unchecked = "o"

    private val context: Context
    private var pdfFile: File? = null
    private var document: Document? = null
    internal var pdfWriter: PdfWriter? = null
    private var paragraph: Paragraph? = null
    private var lineSeparator: LineSeparator? = null
    private var fZapfDingbats = Font(Font.FontFamily.ZAPFDINGBATS, 12f, Font.BOLD)
    private val fTitle = Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD)
    private val fSubTitle = Font(Font.FontFamily.TIMES_ROMAN, 18f, Font.BOLD)
    private val fLightText = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)
    private val fText = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.BOLD)
    private val fHighText = Font(Font.FontFamily.TIMES_ROMAN, 20f, Font.BOLD, BaseColor.RED)

    constructor(context: Context) {
        this.context = context
    }

    fun openDocument(fileTitle: String) : File {
        createFile(fileTitle)
        try {
            document = Document(PageSize.A4)
            pdfWriter = PdfWriter.getInstance(document!!, FileOutputStream(pdfFile!!))
            document!!.open()
        } catch (e: Exception) {
            Log.e("createFile", e.toString())
        }

        return pdfFile!!
    }

    fun openLandscapeDocument(fileTitle: String) : File {
        createFile(fileTitle)
        try {
            document = Document(PageSize.A4_LANDSCAPE)
            pdfWriter = PdfWriter.getInstance(document!!, FileOutputStream(pdfFile!!))
            document!!.open()
        } catch (e: Exception) {
            Log.e("createFile", e.toString())
        }

        return pdfFile!!
    }

    private fun createFile(fileTitle: String) {
        try {
            val folder = File(Environment.getExternalStorageDirectory(), Constants.EXPORT_FOLDER_NAME)
            if (!folder.exists())
                folder.mkdir()

            pdfFile = File(folder, fileTitle)
            if (!pdfFile!!.exists()) {
                pdfFile!!.createNewFile()
            }
        }catch (ex : Exception){
            Log.e("createFile", ex.toString())
        }
    }

    fun closeDocument() {
        document!!.close()
    }

    fun addMetaData(title: String, subject: String, author: String) {
        document!!.addTitle(title)
        document!!.addSubject(subject)
        document!!.addAuthor(author)
    }

    fun addTitle(title: String, subTitle: String, date: String) {
        try {
            paragraph = Paragraph()
            addChildP(Paragraph(title, fTitle))
            addChildP(Paragraph(subTitle, fSubTitle))
            addChildP(Paragraph("Date:$date", fHighText))
            paragraph!!.spacingAfter = 30f
            document!!.add(paragraph)
        } catch (e: Exception) {
            Log.e("addTitle", e.toString())
        }
    }

    fun addChildP(childParagraph: Paragraph) {
        childParagraph.alignment = Element.ALIGN_CENTER
        paragraph!!.add(childParagraph)
    }

    fun addLightParagraph(text: String) {
        try {
            paragraph = Paragraph(text, fLightText)
            paragraph!!.spacingAfter = 5f
            paragraph!!.spacingBefore = 5f
            paragraph!!.alignment = Element.ALIGN_LEFT
            document!!.add(paragraph)
        } catch (e: Exception) {
            Log.e("addParagraph", e.toString())
        }
    }

    fun addParagraph(text: String) {
        try {
            paragraph = Paragraph(text, fText)
            paragraph!!.spacingAfter = 5f
            paragraph!!.spacingBefore = 5f
            paragraph!!.alignment = Element.ALIGN_LEFT
            document!!.add(paragraph)
        } catch (e: Exception) {
            Log.e("addParagraph", e.toString())
        }
    }

    fun addLineSeparator(){
        try {
            lineSeparator = LineSeparator()
            lineSeparator!!.lineColor = BaseColor.BLACK
            lineSeparator!!.lineWidth = 1f
            document!!.add(lineSeparator)
        } catch (e: Exception) {
            Log.e("addParagraph", e.toString())
        }
    }

    fun addRightParagraph(text: String) {
        try {
            paragraph = Paragraph(text, fText)
            paragraph!!.spacingAfter = 5f
            paragraph!!.spacingBefore = 5f
            paragraph!!.alignment = Element.ALIGN_RIGHT
            document!!.add(paragraph)
        } catch (e: Exception) {
            Log.e("addParagraph", e.toString())
        }
    }

    fun createTable(header: Array<String>, clients: ArrayList<Array<String>>) {
        try {
            paragraph = Paragraph()
            paragraph!!.font = fText
            val pdfPTable = PdfPTable(header.size)
            pdfPTable.widthPercentage = 100f
            pdfPTable.spacingBefore = 20f

            var pdfPCell: PdfPCell
            var indexC = 0
            while (indexC < header.size) {
                pdfPCell = PdfPCell(Phrase(header[indexC++], fSubTitle))
                pdfPCell.horizontalAlignment = Element.ALIGN_CENTER
                pdfPCell.verticalAlignment = Element.ALIGN_CENTER
                pdfPCell.backgroundColor = BaseColor.GREEN
                pdfPTable.addCell(pdfPCell)
            }

            for (indexR in clients.indices) {
                val row = clients[indexR]

                indexC = 0
                while (indexC < header.size) {
                    pdfPCell = PdfPCell(Phrase(row[indexC]))
                    pdfPCell.horizontalAlignment = Element.ALIGN_CENTER
                    pdfPCell.verticalAlignment = Element.ALIGN_CENTER
                    pdfPCell.fixedHeight = 40f
                    pdfPTable.addCell(pdfPCell)
                    indexC++
                }
            }

            paragraph!!.add(pdfPTable)
            document!!.add(paragraph)

        } catch (e: Exception) {
            Log.e("createTable", e.toString())
        }
    }

    fun createForm(event : Event, dataElements : List<ProgramStageDataElement>){
        try {
            val pdfPTable = PdfPTable(2)
            pdfPTable.widthPercentage = 100f
            pdfPTable.spacingBefore = 20f

            var pdfPCell: PdfPCell

            for(programStageDataElement in dataElements){
                var dataElement = TrackerController.getDataElement(programStageDataElement.dataelement)
                var isHasValue = false
                var value = ""

                for(dataValue in event.dataValues){
                    if(dataElement.uid.equals(dataValue.dataElement)){
                        isHasValue = true
                        value = dataValue.value
                        break
                    }
                }

                pdfPCell = PdfPCell(addTitleParagragh(dataElement.displayName))
                pdfPCell.horizontalAlignment = Element.ALIGN_CENTER
                pdfPCell.verticalAlignment = Element.ALIGN_CENTER
                pdfPCell.fixedHeight = 40f
                pdfPTable.addCell(pdfPCell)

                if(dataElement.valueType == ValueType.BOOLEAN){
                    if(!value!!.trim().equals("")){
                        pdfPCell = PdfPCell(addCheckboxParagragh(isHasValue, value.toBoolean()))
                        pdfPCell.horizontalAlignment = Element.ALIGN_CENTER
                        pdfPCell.verticalAlignment = Element.ALIGN_CENTER
                        pdfPCell.fixedHeight = 40f
                    }else{
                        pdfPCell = PdfPCell(addCheckboxParagragh(isHasValue, false))
                        pdfPCell.horizontalAlignment = Element.ALIGN_CENTER
                        pdfPCell.verticalAlignment = Element.ALIGN_CENTER
                        pdfPCell.fixedHeight = 40f
                    }
                }else{
                    pdfPCell = PdfPCell(addInputTextParagragh(value))
                    pdfPCell.horizontalAlignment = Element.ALIGN_CENTER
                    pdfPCell.verticalAlignment = Element.ALIGN_CENTER
                    pdfPCell.fixedHeight = 40f
                }
                pdfPTable.addCell(pdfPCell)
            }

            //paragraph!!.add(pdfPTable)
            document!!.add(pdfPTable)

        } catch (e: Exception) {
            Log.e("createTable", e.toString())
        }
    }

    fun viewPDF() {
        /*val intent = Intent(context, ViewPDFActivity::class.java)
        intent.putExtra("path", pdfFile!!.absolutePath)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)*/

    }

    fun appViewPDF(activity: Activity) {
        if (pdfFile!!.exists()) {
            val uri = Uri.fromFile(pdfFile)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")

            try {
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.foxit.mobile.pdf.lite")
                    )
                ) //get package name from play store address bar
                Toast.makeText(
                    activity.applicationContext,
                    "No Pdf Reader Found! Please Install PDF Reader!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            Toast.makeText(activity.applicationContext, "No file found!", Toast.LENGTH_SHORT).show()
        }

    }

    //Title paragraph
    fun addTitleParagragh(title: String) : Paragraph{
        paragraph = Paragraph(title, fText)
        paragraph!!.spacingAfter = 5f
        paragraph!!.spacingBefore = 5f

        return paragraph!!
    }

    //Checkbox paragraph
    fun addCheckboxParagragh(hasValue: Boolean, value: Boolean) : Paragraph{
        paragraph = Paragraph()
        paragraph!!.spacingAfter = 5f
        paragraph!!.spacingBefore = 5f
        paragraph!!.let {
            try {
                if(hasValue){
                    if(value){
                        it.add(Chunk("Yes ", fText))
                        it.add(Chunk(checked, fZapfDingbats))
                        it.add(Chunk("No ", fText))
                        it.add(Chunk(unchecked, fZapfDingbats))
                    }else{
                        it.add(Chunk("Yes ", fText))
                        it.add(Chunk(unchecked, fZapfDingbats))
                        it.add(Chunk("No ", fText))
                        it.add(Chunk(checked, fZapfDingbats))
                    }
                }else{
                    it.add(Chunk("Yes ", fText))
                    it.add(Chunk(unchecked, fZapfDingbats))
                    it.add(Chunk("No ", fText))
                    it.add(Chunk(unchecked, fZapfDingbats))
                }
                //document!!.add(paragraph)
            } catch (e: Exception) {
                Log.e("addParagraph", e.toString())
            }
        }

        return paragraph!!
    }

    //Input Text paragraph
    fun addInputTextParagragh(value: String) : Paragraph{
        try {
            paragraph = Paragraph()
            paragraph!!.spacingAfter = 5f
            paragraph!!.spacingBefore = 5f
            paragraph!!.add(Phrase(value, fText))
            //document!!.add(paragraph)
        } catch (e: Exception) {
            Log.e("addParagraph", e.toString())
        }

        return paragraph!!
    }
}