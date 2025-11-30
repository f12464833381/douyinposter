package com.fengjiandong.douyinposter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build // 【新增】导入 Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest // 【新增】导入 PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    // 存储相机拍摄的图片 URI
    private var currentPhotoUri: Uri? = null

    // 权限请求注册器
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                showImageSourceDialog()
            } else {
                Toast.makeText(this, "需要权限才能添加图片", Toast.LENGTH_LONG).show()
            }
        }

    // 图库多选注册器 (使用 PickMultipleVisualMedia 替代 GetContent)
    private val pickMultipleMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris: List<Uri>? ->
            if (!uris.isNullOrEmpty()) {
                startPostActivityWithUris(uris) // 【修改】直接启动 PostActivity 并传递列表
            } else {
                Toast.makeText(this, "没有选择图片", Toast.LENGTH_SHORT).show()
            }
        }

    // 相机拍照注册器
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                currentPhotoUri?.let {
                    startPostActivityWithUris(listOf(it)) // 【修改】传递单个 Uri 作为列表
                }
            } else {
                Toast.makeText(this, "拍照失败或已取消", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 设置 "+" 号按钮的点击事件
        val addImageButton: ImageView = findViewById(R.id.add_image_button)
        addImageButton.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
        val addImagePlaceholder: ImageView = findViewById(R.id.add_image_placeholder)
        addImagePlaceholder.setOnClickListener {
            checkPermissionsAndShowDialog()
        }
    }

    // 检查并请求所需的权限
    private fun checkPermissionsAndShowDialog() {
        val permissionsNeeded = mutableListOf<String>()

        // 针对 Android 13+ 和 12- 适配存储权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {

            // 使用字符串常量绕过IDE对Manifest.permission.READ_MEDIA_IMAGES的解析失败

            val READ_MEDIA_IMAGES_PERMISSION = "android.permission.READ_MEDIA_IMAGES"

            if (ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES_PERMISSION) != PackageManager.PERMISSION_GRANTED) {

                permissionsNeeded.add(READ_MEDIA_IMAGES_PERMISSION)

            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            showImageSourceDialog()
        }
    }

    // 弹出选择图片来源的对话框
    private fun showImageSourceDialog() {
        val options = arrayOf(getString(R.string.take_photo), getString(R.string.select_from_album)) // 【修改】引用字符串资源
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_image_source)) // 【修改】引用字符串资源
            .setItems(options) { _, which ->
                when (which) {
                    0 -> dispatchTakePictureIntent()
                    1 -> dispatchPickImageIntent()
                }
            }
            .show()
    }

    // 启动图库多选 Intent
    private fun dispatchPickImageIntent() {
        pickMultipleMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // 启动相机 Intent
    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                it
            )
            currentPhotoUri = photoUri
            takePictureLauncher.launch(photoUri)
        }
    }

    // 创建一个唯一的临时图片文件
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    //统一处理图片结果并启动 PostActivity (接收 List<Uri>)
    private fun startPostActivityWithUris(imageUris: List<Uri>) {
        val intent = Intent(this, PostActivity::class.java).apply {
            putParcelableArrayListExtra("image_uris", ArrayList(imageUris)) // 【关键修改】传递 ArrayList
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
}
