package com.grace.onlyrecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grace.onlyrecorder.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class MainActivity: BaseActivity() {

    val PERM_STORAGE = 99
    val PERM_RECORDER = 100
    val PERM_INTERNET = 101

    var flag = false

    var badWordSet = mutableSetOf<String>()
    var recordedWords: String? = null
    var resultCheckwords = false

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STORAGE)

        badWordSet.add("시발")
        badWordSet.add("존나")
        badWordSet.add("꺼져")
        badWordSet.add("병신")
        badWordSet.add("미친놈")
        badWordSet.add("새끼")

        var intent: Intent? = null
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

    }

    override fun permissionGranted(requestCode: Int) {
        when (requestCode) {
            PERM_STORAGE -> requirePermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ), PERM_RECORDER
            )
            PERM_RECORDER -> toast("START를 눌러 녹음을 시작하세요.")
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when (requestCode) {
            PERM_INTERNET, PERM_STORAGE, PERM_RECORDER -> {
                toast("외부 저장소 및 마이크 권한을 승인해야 앱을 사용할 수 있습니다. 앱을 종료합니다.")
                finish()
            }
        }
    }

    fun startProcessing(view: View) {
        binding.btnStart.visibility = View.INVISIBLE
        binding.btnStop.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Default).launch {
            var test1 = async {
                speechRecognition()
            }
            var test2 = async {
                serviceStart()
            }
            Log.d("코루틴","연산결과 = ${test1.await()} + ${test2.await()}")
        }
    }
    fun serviceStart() {
        val intent = Intent(this, MyService::class.java)
        startService(intent)
        flag = true
        thread(start=true) {
            while (flag) {
                Thread.sleep(5000)
                if (flag) {
                    stopService(intent)
                    flag = false
                    Thread.sleep(1000)
                    serviceStart()
                }
            }
        }
    }

    fun serviceStop(view: View) {
        binding.btnStart.visibility = View.VISIBLE
        binding.btnStop.visibility = View.INVISIBLE
        val intent = Intent(this, MyService::class.java)
        stopService(intent)
        flag = false
    }

    fun toast(message: String, duration: Int = 0) {
        var durationTime = Toast.LENGTH_SHORT
        if (duration != 0) durationTime = Toast.LENGTH_LONG
        Toast.makeText(MainApplication.applicationContext(), message, durationTime).show()
    }

    fun speechRecognition() {

        val mRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mRecognizer.setRecognitionListener(listener())
        mRecognizer.startListening(intent)

    }

    fun listener() = object: RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            toast("음성인식을 시작합니다.")
        }

        override fun onBeginningOfSpeech() {

        }

        override fun onRmsChanged(rmsdB: Float) {

        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        override fun onEndOfSpeech() {

        }

        override fun onError(error: Int) {
            toast("오류가 발생하였습니다.")
        }

        override fun onResults(results: Bundle?) {
            toast("음성인식을 종료합니다.")

            recordedWords = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0]
//            binding.textView.text = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0]
//
//            Log.d("flow","recordedWords: ${recordedWords}")
        }

        override fun onPartialResults(partialResults: Bundle?) {

        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }
    }
}




