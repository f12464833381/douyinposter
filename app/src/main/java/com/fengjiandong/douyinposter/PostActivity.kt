package com.fengjiandong.douyinposter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fengjiandong.douyinposter.adapter.ThumbnailAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.ArrayList
import java.util.Locale

class PostActivity : AppCompatActivity() {

    // --- UI 组件 ---
    private lateinit var backButton: ImageView
    private lateinit var cancelButton: TextView
    private lateinit var mainImagePreview: ImageView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var hashtagButton: Button
    private lateinit var atFriendButton: Button
    private lateinit var locationLayout: LinearLayout
    private lateinit var locationText: TextView
    private lateinit var publishButton: Button
    private lateinit var thumbnailRecyclerView: RecyclerView
    private lateinit var editCoverButton: TextView
    private lateinit var titleCountTextView: TextView
    private lateinit var descriptionCountTextView: TextView

    // --- 数据和适配器 ---
    private var imageUris = mutableListOf<Uri>()
    private lateinit var thumbnailAdapter: ThumbnailAdapter

    // --- 位置服务 ---
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // --- 常量 ---
    private val MAX_TITLE_LENGTH = 20
    private val MAX_DESCRIPTION_LENGTH = 3000

    // --- 启动器 ---
    private val addMoreMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            val oldSize = imageUris.size
            imageUris.addAll(uris)
            thumbnailAdapter.notifyItemRangeInserted(oldSize, uris.size)
        }
    }

    private val selectTopicLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedText = result.data?.getStringExtra("selected_text")
            selectedText?.let { appendTextToDescription(it) }
        }
    }

    private val selectUserLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedText = result.data?.getStringExtra("selected_text")
            selectedText?.let { appendTextToDescription(it) }
        }
    }

    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "需要定位权限才能获取位置信息", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        initViews()
        setupLocationClient()
        setupTextWatchers()
        setupClickListeners()

        val initialUris: ArrayList<Uri>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("image_uris", Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("image_uris")
        }

        if (initialUris.isNullOrEmpty()) {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imageUris.addAll(initialUris)
        setupThumbnailRecyclerView()
        setupItemTouchHelper()
        updateMainPreview(0)
    }

    private fun initViews() {
        backButton = findViewById(R.id.back_button)
        cancelButton = findViewById(R.id.cancel_button)
        mainImagePreview = findViewById(R.id.main_image_preview)
        titleEditText = findViewById(R.id.title_edit_text)
        descriptionEditText = findViewById(R.id.description_edit_text)
        hashtagButton = findViewById(R.id.hashtag_button)
        atFriendButton = findViewById(R.id.at_friend_button)
        locationLayout = findViewById(R.id.location_layout)
        locationText = findViewById(R.id.location_text)
        publishButton = findViewById(R.id.publish_button)
        thumbnailRecyclerView = findViewById(R.id.thumbnail_recycler_view)
        editCoverButton = findViewById(R.id.edit_cover_button)
        titleCountTextView = findViewById(R.id.title_count_text_view)
        descriptionCountTextView = findViewById(R.id.description_count_text_view)

        updateTitleCount(titleEditText.text.length)
        updateDescriptionCount(descriptionEditText.text.length)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { finish() }
        cancelButton.setOnClickListener { finish() }
        publishButton.setOnClickListener { /* No action */ }
        editCoverButton.setOnClickListener { Toast.makeText(this, getString(R.string.edit_cover), Toast.LENGTH_SHORT).show() }
        locationLayout.setOnClickListener { checkLocationPermission() }

        hashtagButton.setOnClickListener {
            val intent = Intent(this, TopicSelectionActivity::class.java)
            selectTopicLauncher.launch(intent)
        }
        atFriendButton.setOnClickListener {
            val intent = Intent(this, UserSelectionActivity::class.java)
            selectUserLauncher.launch(intent)
        }
    }

    private fun setupTextWatchers() {
        titleEditText.addTextChangedListener(createLengthWatcher(MAX_TITLE_LENGTH, ::updateTitleCount))

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateDescriptionCount(s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.length > MAX_DESCRIPTION_LENGTH) {
                        s.delete(MAX_DESCRIPTION_LENGTH, s.length)
                    }
                    // 移除监听器以避免无限循环，应用样式，然后重新添加监听器
                    descriptionEditText.removeTextChangedListener(this)
                    applyStylesToDescription()
                    descriptionEditText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun createLengthWatcher(maxLength: Int, updateView: (Int) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateView(s?.length ?: 0)
            }
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLength) {
                    Toast.makeText(this@PostActivity, "字数已达上限", Toast.LENGTH_SHORT).show()
                    s.delete(maxLength, s.length)
                }
            }
        }
    }

    private fun updateTitleCount(count: Int) {
        titleCountTextView.text = "$count/$MAX_TITLE_LENGTH"
    }

    private fun updateDescriptionCount(count: Int) {
        descriptionCountTextView.text = "$count/$MAX_DESCRIPTION_LENGTH"
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        locationText.text = "正在定位..."
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                reverseGeocodeLocation(location.latitude, location.longitude)
            } else {
                requestNewLocationData()
            }
        }.addOnFailureListener {
            locationText.text = "定位失败"
            Toast.makeText(this, "定位服务不可用或发生错误", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000
        ).build()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    reverseGeocodeLocation(location.latitude, location.longitude)
                } else {
                    locationText.text = "定位失败"
                }
                fusedLocationClient.removeLocationUpdates(this)
            }
        }, null)
    }

    private fun reverseGeocodeLocation(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            val locationName = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(this@PostActivity, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        addresses[0].locality ?: addresses[0].adminArea ?: "未知地点"
                    } else { "无法解析地点" }
                } catch (e: IOException) { "网络错误" }
            }
            locationText.text = locationName
        }
    }

    private fun setupThumbnailRecyclerView() {
        thumbnailAdapter = ThumbnailAdapter(
            imageUris = imageUris,
            onImageClick = { position -> updateMainPreview(position) },
            onAddClick = { addMoreMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onDeleteClick = { position -> showDeleteConfirmationDialog(position) }
        )
        thumbnailRecyclerView.adapter = thumbnailAdapter
        thumbnailRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupItemTouchHelper() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (fromPosition < imageUris.size && toPosition < imageUris.size) {
                    thumbnailAdapter.onItemMove(fromPosition, toPosition)
                    if (fromPosition == 0 || toPosition == 0) updateMainPreview(0)
                    return true
                }
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(thumbnailRecyclerView)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_confirmation_title))
            .setMessage(getString(R.string.delete_confirmation_message))
            .setPositiveButton(getString(R.string.confirm)) { _, _ -> deleteImageAt(position) }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteImageAt(position: Int) {
        if (position < imageUris.size) {
            imageUris.removeAt(position)
            thumbnailAdapter.notifyItemRemoved(position)
            thumbnailAdapter.notifyItemRangeChanged(position, imageUris.size + 1)
            if (imageUris.isEmpty()) {
                finish()
            } else {
                updateMainPreview(0)
            }
        }
    }

    private fun updateMainPreview(position: Int) {
        if (position < imageUris.size) {
            Glide.with(this)
                .load(imageUris[position])
                .into(mainImagePreview)
            thumbnailAdapter.updateSelection(position)
            editCoverButton.visibility = if (position == 0) View.VISIBLE else View.GONE
        }
    }

    private fun appendTextToDescription(text: String) {
        val currentText = descriptionEditText.text.toString()
        val newText = if (currentText.isEmpty()) "$text " else "$currentText$text "
        descriptionEditText.setText(newText)
        applyStylesToDescription()
        descriptionEditText.setSelection(descriptionEditText.length())
    }

    private fun applyStylesToDescription() {
        val text = descriptionEditText.text
        val spannable = SpannableString(text)
        val color = ContextCompat.getColor(this, R.color.link_blue)
        val hashtagPattern = Regex("#\\S+")
        val atUserPattern = Regex("@\\S+")
        val selection = descriptionEditText.selectionStart
        hashtagPattern.findAll(text).forEach {
            spannable.setSpan(ForegroundColorSpan(color), it.range.first, it.range.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        atUserPattern.findAll(text).forEach {
            spannable.setSpan(ForegroundColorSpan(color), it.range.first, it.range.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        descriptionEditText.setText(spannable, TextView.BufferType.SPANNABLE)
        descriptionEditText.setSelection(selection.coerceAtMost(descriptionEditText.length()))
    }
}
