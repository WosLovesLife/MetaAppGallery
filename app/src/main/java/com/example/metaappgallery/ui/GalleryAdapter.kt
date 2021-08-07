package com.example.metaappgallery.ui

import android.widget.ImageView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.metaappgallery.R
import com.example.metaappgallery.pojo.PictureData
import kotlin.math.roundToInt

class GalleryAdapter private constructor(layoutResId: Int, val itemWidthPx: Int) :
    BaseQuickAdapter<PictureData, BaseViewHolder>(layoutResId),
    LoadMoreModule {

    companion object {
        fun create(itemWidthPx: Int): GalleryAdapter {
            return GalleryAdapter(R.layout.item_gallery_list, itemWidthPx)
        }
    }

    var isStaggeredGridItem = false

    override fun convert(holder: BaseViewHolder, item: PictureData) {
        val itemHeight = if (isStaggeredGridItem) {
            val ratio = (item.thumbHeight * 1f / item.thumbWidth)
            (itemWidthPx * ratio).roundToInt()
        } else {
            (itemWidthPx * 1.2).roundToInt()
        }
        holder.itemView.updateLayoutParams<RecyclerView.LayoutParams> {
            width = itemWidthPx
            height = itemHeight
        }
        holder.setText(R.id.pictureTitle, item.title)
        val imageView = holder.getView<ImageView>(R.id.imageView)
        imageView.scaleType = if (isStaggeredGridItem) {
            ImageView.ScaleType.CENTER
        } else {
            ImageView.ScaleType.CENTER_CROP
        }
        Glide.with(context)
            .load(item.thumb)
            .apply(RequestOptions().priority(Priority.LOW))
            .into(imageView)
    }

    override fun addLoadMoreModule(baseQuickAdapter: BaseQuickAdapter<*, *>): BaseLoadMoreModule {
        return super.addLoadMoreModule(baseQuickAdapter)
            .also { it.isEnableLoadMoreIfNotFullPage = false }
    }
}