/*
 * Copyright 2019 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.afinogeev.treasy.analyzer

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.MlKitException
import ru.afinogeev.treasy.util.ImageUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Exception
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.*
import android.os.Environment





/**
 * Analyzes the frames passed in from the camera and returns any detected text within the requested
 * crop region.
 */
class TextAnalyzer(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val result: MutableLiveData<String>,
    private val imageCropPercentages: MutableLiveData<Pair<Int, Int>>
) : ImageAnalysis.Analyzer {

    // TODO: Instantiate TextRecognition detector
    private val detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        // We requested a setTargetAspectRatio, but it's not guaranteed that's what the camera
        // stack is able to support, so we calculate the actual ratio from the first frame to
        // know how to appropriately crop the image we want to analyze.
        val imageHeight = mediaImage.height
        val imageWidth = mediaImage.width
        val actualAspectRatio = imageWidth / imageHeight

        val convertImageToBitmap = ImageUtils.convertYuv420888ImageToBitmap(mediaImage)
        val cropRect = Rect(0, 0, imageWidth, imageHeight)

        // If the image has a way wider aspect ratio than expected, crop less of the height so we
        // don't end up cropping too much of the image. If the image has a way taller aspect ratio
        // than expected, we don't have to make any changes to our cropping so we don't handle it
        // here.
        val currentCropPercentages = imageCropPercentages.value ?: return
        if (actualAspectRatio > 3) {
            val originalHeightCropPercentage = currentCropPercentages.first
            val originalWidthCropPercentage = currentCropPercentages.second
            imageCropPercentages.value =
                Pair(originalHeightCropPercentage / 2, originalWidthCropPercentage)
        }

        // If the image is rotated by 90 (or 270) degrees, swap height and width when calculating
        // the crop.
        val cropPercentages = imageCropPercentages.value ?: return
        val heightCropPercent = cropPercentages.first
        val widthCropPercent = cropPercentages.second
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
            else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
        }

        cropRect.inset(
            (imageWidth * widthCrop / 2).toInt(),
            (imageHeight * heightCrop / 2).toInt()
        )
        val croppedBitmap =
            ImageUtils.rotateAndCrop(convertImageToBitmap, rotationDegrees, cropRect)

        //bitmapToFile(croppedBitmap)

        val image = InputImage.fromBitmap(croppedBitmap, 0)
        //val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        recognizeText(image).addOnCompleteListener {
            imageProxy.close()
        }
    }

    // Method to save an bitmap to a file
    private fun bitmapToFile(bitmap:Bitmap){


        //Create a new file that points to the root directory, with the given name:

        val wrapper = ContextWrapper(context)
        //Create a new file that points to the root directory, with the given name:
        var file = wrapper.getExternalFilesDir(null)
        if (file != null) {
            //Log.println(Log.INFO,"write", file.absolutePath)
        }
        file = File(file,"123.jpg")

        try{
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }

    }

    private fun recognizeText(
        image: InputImage
    ): Task<Text> {
        // Pass image to an ML Kit Vision API
        return detector.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                val str = findNumber(visionText)
                if (str.length>9) {
                    result.value = str
                }
                //result.value = findNumber(visionText)
                //result.value = visionText.text
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                //Log.e(TAG, "Text recognition error", exception)
                val message = getErrorMessage(exception)
                message?.let {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun findNumber(text: Text):String {
        for (block in text.textBlocks) {
            for (line in block.lines) {
                val filteredText = line.text.filter { it.isDigit() }
                //Log.println(Log.INFO,"text", filteredText)
                when (filteredText.length) {
                    11 -> {
                        if(filteredText[1] == '9'){
                            if(filteredText[0] == '7' || filteredText[0] == '8'){
                                return filteredText.substring(1,11)
                            }
                        }
                    }
                    16 -> {
                        val fc = filteredText[0]
                        if (fc == '2' || fc == '4' || fc == '5')
                            return filteredText
                    }
                    10 -> {
                        if (filteredText[0] == '9')
                            return filteredText
                    }
                }
            }
        }
        return ""
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

    companion object {
        private const val TAG = "TextAnalyzer"
    }
}