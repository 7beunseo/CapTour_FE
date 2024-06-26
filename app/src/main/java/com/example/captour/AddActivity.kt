package com.example.captour

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.example.captour.databinding.ActivityAddBinding
import java.io.File
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat

class AddActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddBinding
    var uri : Uri? = null
    lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreference = PreferenceManager.getDefaultSharedPreferences(this)

        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar);

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnWrite.setOnClickListener {

            // 1. db에 저장하기
            /*
            val intent = intent
            val title = binding.title.text.toString()
            val content = binding.content.text.toString()
            val data = title + "\n" + content
            intent.putExtra("result", data)
            setResult(Activity.RESULT_OK, intent)

            val db = DBHelper(this).writableDatabase
            db.execSQL("insert into captour_db (data) values (?)", arrayOf<String>(data))
            db.close()
            finish()
            true
             */

            val dateFormat = SimpleDateFormat(("yyyy-MM-dd hh:mm:ss"))

            // 2. FirebaseStoreage 이용
            if(binding.title.text.isNotEmpty()) {
                // 로그인 이메일, 제목, 본문, 스타, 입력 시간
                val data = mapOf(
                    "email" to MyApplication.email,
                    "title" to binding.title.text.toString(),
                    "content" to binding.content.text.toString(),
                    "stars" to binding.ratingBar.rating.toFloat(),
                    "date_time" to dateFormat.format(System.currentTimeMillis())
                )
                // store에 저장
                MyApplication.db.collection("review")
                    .add(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "데이터 저장 성공", Toast.LENGTH_SHORT).show()
                        uploadImage(it.id)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "데이터 저장 실패", Toast.LENGTH_SHORT).show()

                    }
            } else {
                Toast.makeText(this, "제목을 작성해주세요", Toast.LENGTH_SHORT).show()
            }


            // 파일 객체 생성
            val file = File(filesDir, "count.txt")

            // 파일이 존재하지 않으면 초기화
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("0")
            }

            // 파일 읽기
            val readstream = file.bufferedReader()
            val count = readstream.readLine()?.toIntOrNull() ?: 0
            readstream.close()

            // 값 증가 및 파일에 쓰기
            val data = count + 1
            val writestream = file.bufferedWriter()
            writestream.write(data.toString())
            writestream.flush()
            writestream.close()
        }

        // 이미지 업로드
        val requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode === android.app.Activity.RESULT_OK){
                binding.addImageView.visibility = View.VISIBLE
                Glide
                    .with(applicationContext)
                    .load(it.data?.data)
                    .override(200,150)
                    .into(binding.addImageView)
                uri = it.data?.data!!
            }
        }

        binding.upload.setOnClickListener {
            Log.d("mobileapp", "in")
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestLauncher.launch(intent)
        }

    }

    fun uploadImage(docId : String){
        val imageRef = MyApplication.storage.reference.child("images/${docId}.jpg")

        val uploadTask = uri?.let { imageRef.putFile(it) }
        uploadTask?.addOnSuccessListener {
            Toast.makeText(this, "사진 업로드 성공", Toast.LENGTH_LONG).show()
        }
        uploadTask?.addOnFailureListener {
            Toast.makeText(this, "사진 업로드 실패", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        // 글자 크기 설정
        val fontSize = sharedPreference.getInt("font_size", 16)
        binding.title.textSize = fontSize + 10f
        binding.content.textSize = fontSize + 1f
        binding.info1.textSize = fontSize + 15f
        binding.info2.textSize = fontSize + 15f

        // 색상 설정
        val color = sharedPreference.getString("color", "#363C90")
        val colorCode = Color.parseColor(color)
        val colorStateList = ColorStateList.valueOf(colorCode)
        binding.toolbar.setBackgroundColor(colorCode)
        binding.upload.setBackgroundColor(colorCode)
        binding.btnWrite.setBackgroundColor(colorCode)

        // 폰트 굵기 설정
        val fontStyle = sharedPreference.getString("font_style", "regular")

        var typeface: Typeface?
        if(fontStyle == "regular") {
            typeface = ResourcesCompat.getFont(this, R.font.nanum_regular)
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.nanum_bold)
        }
        binding.info1.typeface = typeface
        binding.info2.typeface = typeface
        binding.title.typeface = typeface
        binding.content.typeface = typeface
        binding.btnWrite.typeface = typeface
        binding.upload.typeface = typeface
    }
}