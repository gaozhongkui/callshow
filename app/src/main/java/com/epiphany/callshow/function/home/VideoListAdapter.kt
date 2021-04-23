package com.epiphany.callshow.function.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.epiphany.callshow.R
import com.epiphany.callshow.databinding.ItemHomeVideoViewBinding
import com.epiphany.callshow.model.VideoItemInfo

class VideoListAdapter(cxt: Context) : RecyclerView.Adapter<VideoListAdapter.NormalViewHolder>() {
    private val mDataList = mutableListOf<VideoItemInfo>()
    private val mLayoutInflater = LayoutInflater.from(cxt)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalViewHolder {
        val binding = DataBindingUtil.inflate<ItemHomeVideoViewBinding>(
            mLayoutInflater,
            R.layout.item_home_video_view, parent, false
        )
        return NormalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NormalViewHolder, position: Int) {
        if (position >= itemCount) {
            return
        }
        holder.onBindDataToView(mDataList[position])
    }

    override fun getItemCount(): Int = mDataList.size

    /**
     * 设置数据
     */
    @MainThread
    fun setDataList(data: List<VideoItemInfo>, isRefresh: Boolean) {
        if (isRefresh) {
            mDataList.clear()
        }
        mDataList.addAll(data)
        notifyDataSetChanged()
    }

    fun releaseData() {
        mDataList.clear()
    }

    inner class NormalViewHolder(private val binding: ItemHomeVideoViewBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun onBindDataToView(info: VideoItemInfo) {
            Glide.with(binding.root).load(info.previewPng).into(binding.ivImg)
            //计算宽高比
            val dimensionRatio = info.width.toFloat() / info.high.toFloat()
            //设置布局的旷告比
            val layoutParams = binding.ivImg.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.dimensionRatio = "1:$dimensionRatio"
            binding.ivImg.layoutParams = layoutParams
        }
    }
}

