package com.grace.onlyrecorder

import android.content.res.AssetManager

import android.util.Log
import com.google.android.gms.tasks.*
import com.google.android.gms.tasks.Task
import com.grace.recorderrunnable.Recorder
import com.jlibrosa.*
import org.tensorflow.lite.Interpreter as Interpreter

import org.tensorflow.lite.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.jlibrosa.audio.JLibrosa
import com.jlibrosa.audio.wavFile.WavFile
import java.io.*
import java.nio.ByteOrder


class MLcode(private val context: Recorder) {
    private var interpreter: Interpreter? = null
    var isInitialized = false
    var jLibrosa = JLibrosa()

    /** Executor to run inference task in the background. */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputSoundWavelength: Int = 0 // will be inferred from TF Lite model.
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model.

    /*fun initialize(): Task<Void> {
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
    }*/

    /*private fun initializeInterpreter() {
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
    }*/

    fun classify(filename: String): Boolean {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }


        // Pre-processing: resize the input image to match the model input shape.

        // Define an array to store the model output.
        val output = FloatArray(1) { 0F }
        val audioFeatureValues = jLibrosa.loadAndRead(filename, -1, -1)


        // Run inference with the input data.
        interpreter?.run(audioFeatureValues[1], output)

        // Post-processing: find the digit that has the highest probability
        // and return it a human-readable string.
        val result = output[0]
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



    fun convertAudioToByteBuffer(fileName: String): ByteArray {
        var byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val SAMPLE_RATE = 44100
        val featureValues : FloatArray
        featureValues = jLibrosa.loadAndRead(fileName, SAMPLE_RATE, -1)

        val wavFile = File(fileName)
        Log.d("test","Sub: 녹음파일 오픈")
        //val out = ByteArrayOutputStream()
        //val inputSt = BufferedInputStream(FileInputStream(wavFile))

        var read: Int
        Log.d("test","Sub: 녹음파일 읽는 중...")
        val buff = ByteArray(882000)
        //while (inputSt.read(buff).also { read = it } > 0) {
         //   out.write(buff, 0, read)
        //}
        //out.flush()
        val audioBytes: ByteArray = FloatArray2ByteArray(featureValues)

        Log.d("test","Sub: 녹음파일 변환 중...")


        return audioBytes
    }

    fun FloatArray2ByteArray(values: FloatArray): ByteArray {
        val buffer = ByteBuffer.allocate(4 * values.size)
        for (value in values) {
            buffer.putFloat(value)
        }
        return buffer.array()
    }

    fun extendBuffer(buffer: ByteBuffer, size: Int): ByteBuffer? {
        val localBuffer = ByteBuffer.allocate(
            buffer
                .capacity() + size
        )
        System.arraycopy(
            buffer.array(), 0, localBuffer.array(), 0,
            buffer.position()
        )
        localBuffer.position(buffer.position())
        return localBuffer
    }

    companion object {

        private const val TAG = "DigitClassifier"

        private const val FLOAT_TYPE_SIZE = 4

        private const val OUTPUT_CLASSES_COUNT = 1
    }
}