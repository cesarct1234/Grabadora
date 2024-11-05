package com.cesar.grabadora

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioFile: File
    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        val stopButton = findViewById<Button>(R.id.stopButton)
        val recordButton = findViewById<Button>(R.id.recordButton)

        // Verificar permisos
        if (!hasPermissions()) {
            requestPermissions()
        }

        // Inicializar MediaRecorder
        mediaRecorder = MediaRecorder()
        mediaPlayer = MediaPlayer()

        // Inicializar archivo de audio
        audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audioRecording.3gp")

        // Configurar botones
        recordButton.setOnClickListener {
            if (hasPermissions()) {
                startRecording()
            } else {
                requestPermissions()
            }
        }

        stopButton.setOnClickListener {
            stopRecording()
        }

        playButton.setOnClickListener {
            playRecording()
        }
    }

    private fun hasPermissions(): Boolean {
        val recordAudioPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        )
        return recordAudioPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_CODE
        )
    }

    private fun startRecording() {
        try {
            audioFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audioRecording.3gp")
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFile.absolutePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
            isRecording = true
            Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        if (!isRecording) {
            Toast.makeText(this, "No hay ninguna grabación en curso", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mediaRecorder.apply {
                stop()
                reset()
            }
            isRecording = false
            Toast.makeText(this, "Grabación detenida. Archivo guardado en: ${audioFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al detener la grabación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playRecording() {
        if (!audioFile.exists()) {
            Toast.makeText(this, "Archivo de audio no encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mediaPlayer.apply {
                reset()
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
            }
            Toast.makeText(this, "Reproduciendo grabación", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al reproducir la grabación", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de grabación necesario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder.release()
        mediaPlayer.release()
    }
}
