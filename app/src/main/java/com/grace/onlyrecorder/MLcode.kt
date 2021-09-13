package com.grace.onlyrecorder

import android.content.Context
import android.content.res.AssetManager

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.*
import com.jlibrosa.*
import com.jlibrosa.audio.wavFile.WavFile
import org.tensorflow.lite.Interpreter as Interpreter

import org.tensorflow.lite.*
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.jlibrosa.audio.JLibrosa




class MLcode(private val context: Context) {
    private var interpreter: Interpreter? = null
    var isInitialized = false
    var jLibrosa = JLibrosa()

    /** Executor to run inference task in the background. */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputSoundWavelength: Int = 0 // will be inferred from TF Lite model.
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model.

    fun initialize(): Task<Void> {
        val task = TaskCompletionSource<Void>()
        executorService.execute {
            try {
                initializeInterpreter()
                task.setResult(null)
            } catch (e: IOException) {
                task.setException(e)
            }
        }
        return task.task
    }

    private fun initializeInterpreter() {
        // Load the TF Lite model from asset folder and initialize TF Lite Interpreter with NNAPI enabled.
        val assetManager = context.assets
        val model = loadModelFile(assetManager, "final_model.tflite")
        val interpreter = Interpreter(model)



        // Read input shape from model file.
        val inputShape = interpreter.getInputTensor(0).shape()
        inputSoundWavelength = inputShape[1]
        modelInputSize = FLOAT_TYPE_SIZE * inputSoundWavelength

        // Finish interpreter initialization.
        this.interpreter = interpreter

        isInitialized = true
        Log.d(TAG, "Initialized TFLite interpreter.") }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classify(filename: String): Boolean {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

        // TODO: Add code to run inference with TF Lite.
        // Pre-processing: resize the input image to match the model input shape.

        // Define an array to store the model output.
        val output = FloatArray(1) { 0F }
        val audioFeatureValues = jLibrosa.loadAndRead(filename, -1, -1)


        // Run inference with the input data.
        interpreter?.run(audioFeatureValues[1], output)

        // Post-processing: find the digit that has the highest probability
        // and return it a human-readable string.
        val result = output[0]
        Toast.makeText(this.context, "ㅗㅗㅗㅗㅗㅗㅗ", Toast.LENGTH_SHORT).show()
        return result > 0.5
    }

    fun classifyAsync(filename: String): Task<Boolean> {
        val task = TaskCompletionSource<Boolean>()
        executorService.execute {
            val result = classify(filename)
            task.setResult(result)
        }
        return task.task
    }



    /*private fun convertBitmapToByteBuffer(wavFile: WavFile): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Convert RGB to grayscale and normalize pixel value to [0..1].
            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }*/

    companion object {

        private const val TAG = "DigitClassifier"

        private const val FLOAT_TYPE_SIZE = 4

        private const val OUTPUT_CLASSES_COUNT = 1
    }
}

