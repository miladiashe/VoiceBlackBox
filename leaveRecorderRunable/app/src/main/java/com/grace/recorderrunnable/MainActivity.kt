package com.grace.recorderrunnable

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import com.grace.recorderrunnable.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.zip.Inflater


class MainActivity : BaseActivity() {

    val PERM_STORAGE = 99
    val PERM_RECORDER = 100
    val PERM_INTERNET = 101

    var badWordSet = mutableSetOf<String>()
    var recordedWords: String? = null
    var resultCheckwords = false

    var recorderThread = Recorder()
    var recorderHandler = recorderThread.recorderHandler
    val msg = Message()

    var serviceState: Boolean = false

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STORAGE)

        makeBadwordsList()

        var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        binding.btnStart.setOnClickListener {
            startService()
        }

        binding.btnStop.setOnClickListener {
            stopService()
        }
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

    fun toast(message: String, duration: Int = 0) {
        var durationTime = Toast.LENGTH_SHORT
        if (duration != 0) durationTime = Toast.LENGTH_LONG
        Toast.makeText(MainApplication.applicationContext(), message, durationTime).show()
    }

    fun startService() {
        Log.d("test","startService() 시작")
        serviceState = true
        binding.btnStart.visibility = View.INVISIBLE
        binding.btnStop.visibility = View.VISIBLE

        speechRecognition()

        if (recorderThread.state == Thread.State.NEW) {
            recorderThread.start()
        }
        msg.arg1 = 1
        recorderHandler!!.handleMessage(msg)
        Log.d("test","startService() 끝")
    }

    fun stopService(){
        Log.d("test","stopService() 시작")
        serviceState = false
        binding.btnStart.visibility = View.VISIBLE
        binding.btnStop.visibility = View.INVISIBLE

        msg.arg1 = 0
        recorderHandler!!.handleMessage(msg)
        Log.d("test","stopService() 끝")
    }

    fun speechRecognition() {
        Log.d("test","speechRecognition() 시작")

        val mRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mRecognizer.setRecognitionListener(listener())

        try{
            mRecognizer.startListening(intent)
        } catch (e : Exception) {
            toast("오류가 발생했습니다.")
        }
        Log.d("test","speechRecognition() 끝")
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

            if (serviceState){
                toast("다시 실행합니다.")
                speechRecognition()
            }
        }

        override fun onResults(results: Bundle?) {
            toast("음성인식을 종료합니다.")

            recordedWords = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0]
            binding.textView.text = recordedWords
            checkBadwords()

            if (serviceState){
                toast("다시 실행합니다.")
                speechRecognition()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {

        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }
    }

    fun makeBadwordsList() {
        badWordSet.add("시발")
        badWordSet.add("존나")
        badWordSet.add("꺼져")
        badWordSet.add("병신")
        badWordSet.add("미친놈")
        badWordSet.add("새끼")
    }

    fun checkBadwords() {
        Log.d("test","checkBadwords() 시작")
        for (badWord in badWordSet) {
            resultCheckwords = recordedWords!!.contains(badWord)

            if (resultCheckwords) {
                msg.arg2 = 1
                recorderHandler!!.handleMessage(msg)
                break
            } else {
                msg.arg2 = 0
                recorderHandler!!.handleMessage(msg)
            }
        }
        Log.d("test","recordedWords: ${recordedWords}")
        Log.d("test","wordsCheck: ${resultCheckwords}")
        Log.d("test","checkBadwords() 종료")
    }
}