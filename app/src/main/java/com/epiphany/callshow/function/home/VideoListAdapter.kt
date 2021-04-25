package com.epiphany.callshow.function.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.epiphany.callshow.R
import com.epiphany.callshow.databinding.ItemHomeVideoViewBinding
import com.epiphany.callshow.model.VideoItemInfo

class VideoListAdapter(cxt: Context) : RecyclerView.Adapter<VideoListAdapter.NormalViewHolder>() {
    private val mDataList = mutableListOf<VideoItemInfo>()
    private val mLayoutInflater = LayoutInflater.from(cxt)
    private var mListener: IVideoListListener? = null
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
     * 获取所有数据集合
     */
    fun getDataList(): ArrayList<VideoItemInfo> {
        return ArrayList(mDataList)
    }

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

    fun setVideoListListener(listener: IVideoListListener) {
        mListener = listener
    }

    fun releaseData() {
        mDataList.clear()
    }

    inner class NormalViewHolder(private val binding: ItemHomeVideoViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                mListener?.onItemClick(adapterPosition)
            }
        }

        fun onBindDataToView(info: VideoItemInfo) {
            Glide.with(binding.root).load(info.previewPng)
                .placeholder(R.drawable.bg_video_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.DATA).into(binding.ivImg)
            binding.tvTitle.text = info.title
            //计算宽高比
            val dimensionRatio = info.width.toFloat() / info.high.toFloat()
            //设置布局的旷告比
            val layoutParams = binding.ivImg.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.dimensionRatio = "1:$dimensionRatio"
            binding.ivImg.layoutParams = layoutParams
        }
    }

    interface IVideoListListener {
        fun onItemClick(position: Int)
    }
}

