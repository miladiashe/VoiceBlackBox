package com.grace.onlyrecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grace.onlyrecorder.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity: BaseActivity() {

    val PERM_STORAGE = 99
    val PERM_RECORDER = 100
    var flag = false

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermissions(arrayOf(Manifest.permission.
                                    WRITE_EXTERNAL_STORAGE), PERM_STORAGE)

        binding.btnStart.setOnClickListener {
            binding.btnStart.visibility = View.INVISIBLE
            binding.btnStop.visibility = View.VISIBLE
            serviceStart()
        }

    }

    override fun permissionGranted(requestCode: Int) {
        when(requestCode){
            PERM_STORAGE -> requirePermissions(arrayOf(Manifest.permission.
                            RECORD_AUDIO), PERM_RECORDER)
            PERM_RECORDER -> Toast.makeText(baseContext,
                "START를 눌러 녹음을 시작하세요.",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when(requestCode){
            PERM_STORAGE, PERM_RECORDER -> {
                Toast.makeText(baseContext,
                    "외부 저장소 및 마이크 권한을 승인해야 앱을 사용할 수 있습니다." +
                            "앱을 종료합니다.",
                    Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    fun serviceStart() {
        val intent = Intent(this, MyService::class.java)
        startService(intent)
        flag = true
        thread(start=true) {
            while (flag) {
                Thread.sleep(10000)
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

}