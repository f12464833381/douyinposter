package com.fengjiandong.douyinposter.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fengjiandong.douyinposter.R
import java.util.Collections

// 定义两种 Item 类型
private const val VIEW_TYPE_IMAGE = 1
private const val VIEW_TYPE_ADD_BUTTON = 2

class ThumbnailAdapter(
    private val imageUris: MutableList<Uri>, // 【修改】改为 MutableList 支持修改
    private val onImageClick: (position: Int) -> Unit,
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (position: Int) -> Unit // 【新增】删除按钮的回调
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition = 0

    fun updateSelection(newPosition: Int) {
        if (newPosition != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    // 处理拖动排序
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        // 确保拖动目标不是最后的“添加”按钮
        if (fromPosition < imageUris.size && toPosition < imageUris.size) {
            Collections.swap(imageUris, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
        }
    }

    // ------------------ ViewHolder 定义 ------------------

    // 1. 图片缩略图的 ViewHolder (包含删除按钮)
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImage: ImageView = itemView.findViewById(R.id.thumbnail_image)
        val deleteButton: ImageView = itemView.findViewById(R.id.thumbnail_delete_btn) // 【修改】获取新的删除按钮 ID
    }

    // 2. 添加按钮的 ViewHolder
    class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 布局 item_add_thumbnail.xml 只需要一个容器 View，无需内部组件
    }

    // ------------------ Adapter 核心方法 ------------------

    override fun getItemCount(): Int = imageUris.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position < imageUris.size) {
            VIEW_TYPE_IMAGE
        } else {
            VIEW_TYPE_ADD_BUTTON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val view = inflater.inflate(R.layout.thumbnail_item, parent, false)
                ImageViewHolder(view)
            }
            VIEW_TYPE_ADD_BUTTON -> {
                val view = inflater.inflate(R.layout.item_add_thumbnail, parent, false)
                AddButtonViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_IMAGE -> {
                val imageHolder = holder as ImageViewHolder
                val uri = imageUris[position]

                Glide.with(imageHolder.thumbnailImage.context)
                    .load(uri)
                    .centerCrop()
                    .into(imageHolder.thumbnailImage)

                // 选中状态边框
                if (position == selectedPosition) {
                    imageHolder.itemView.setBackgroundResource(R.drawable.thumbnail_selected_border)
                } else {
                    imageHolder.itemView.setBackgroundResource(0)
                }

                // 点击事件：切换大图
                imageHolder.itemView.setOnClickListener {
                    onImageClick(position)
                }

                // 【新增】删除按钮点击事件
                imageHolder.deleteButton.setOnClickListener {
                    onDeleteClick(position)
                }
            }
            VIEW_TYPE_ADD_BUTTON -> {
                holder.itemView.setOnClickListener {
                    onAddClick()
                }
            }
        }
    }
}
