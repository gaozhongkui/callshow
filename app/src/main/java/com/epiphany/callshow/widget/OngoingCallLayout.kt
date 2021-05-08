package com.epiphany.callshow.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epiphany.call.extensions.beGone
import com.epiphany.call.extensions.beVisible
import com.epiphany.callshow.R
import com.epiphany.callshow.databinding.ItemOngoingCallToolBinding
import com.epiphany.callshow.databinding.ViewOngoingCallLayoutBinding
import com.epiphany.callshow.listener.DefaultButtonClickStyleListener

class OngoingCallLayout(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private var binding: ViewOngoingCallLayoutBinding
    private var mToolAdapter: CallToolAdapter? = null
    private var mDigitalKeyboardAdapter: DigitalKeyboardAdapter? = null
    private var mLayoutListener: IOngoingCallLayoutLayoutListener? = null

    init {
        val inflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(
                inflater, R.layout.view_ongoing_call_layout, this, true
        )
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        //设置工具箱
        mToolAdapter = CallToolAdapter(context)
        binding.rvToolView.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        binding.rvToolView.adapter = mToolAdapter

        //设置数字键盘
        mDigitalKeyboardAdapter = DigitalKeyboardAdapter(context)
        binding.rvDigitalKeyboard.layoutManager = GridLayoutManager(context, SPAN_COUNT)
        binding.rvDigitalKeyboard.adapter = mDigitalKeyboardAdapter

        binding.ivKeyboardOpen.setOnClickListener {
            mToolAdapter?.getItemDataByToolType(CallToolType.DIAL_UP_KEYBOARD)?.apply {
                state = true
                mLayoutListener?.onItemClick(this)
            }
        }
    }

    /**
     * 切换数字键盘展示状态
     */
    fun switchDigitalKeyboardVisible(isVisible: Boolean) {
        if (isVisible) {
            binding.rvDigitalKeyboard.beVisible()
            binding.ivKeyboardOpen.beVisible()
            binding.rvToolView.beGone()
        } else {
            binding.ivKeyboardOpen.beGone()
            binding.rvDigitalKeyboard.beGone()
            binding.rvToolView.beVisible()
        }

    }

    fun setListener(listener: IOngoingCallLayoutLayoutListener) {
        mLayoutListener = listener
        mToolAdapter?.setListener(listener)
        mDigitalKeyboardAdapter?.setListener(listener)
    }

    fun notifyCallToolAdapter() {
        mToolAdapter?.notifyDataSetChanged()
    }

    companion object {
        private const val SPAN_COUNT = 3
    }
}

private class DigitalKeyboardAdapter(cxt: Context) : RecyclerView.Adapter<DigitalKeyboardAdapter.NormalViewHolder>() {
    private val mLayoutInflater = LayoutInflater.from(cxt)
    private val mDataList = mutableListOf<Char>()
    private var mListener: IOngoingCallLayoutLayoutListener? = null

    init {
        for (item in 1 until 10) {
            mDataList.add(item.toString().toCharArray()[0])
        }
        mDataList.add('*')
        mDataList.add('0')
        mDataList.add('#')
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalViewHolder {
        return NormalViewHolder(mLayoutInflater.inflate(R.layout.item_digital_keyboard_view, parent, false))
    }

    override fun onBindViewHolder(holder: NormalViewHolder, position: Int) {
        if (position >= itemCount) {
            return
        }
        holder.onBindDataToView(mDataList[position])
    }

    override fun getItemCount(): Int = MAX_ITEM_COUNT

    companion object {
        private const val MAX_ITEM_COUNT = 12
    }

    fun setListener(listener: IOngoingCallLayoutLayoutListener) {
        mListener = listener
    }


    inner class NormalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val mTitleTxt: TextView = view.findViewById(R.id.tv_title)
        private var mItemData: Char? = null

        init {
            view.setOnTouchListener(DefaultButtonClickStyleListener())
            view.setOnClickListener {
                if (mItemData != null) {
                    mListener?.onDigitalKeyboardItemClick(mItemData!!)
                }
            }
        }

        fun onBindDataToView(data: Char) {
            mItemData = data
            mTitleTxt.text = data.toString()
        }
    }
}


private class CallToolAdapter(cxt: Context) :
        RecyclerView.Adapter<CallToolAdapter.NormalViewHolder>() {
    private val mDataList = mutableListOf<CallToolInfo>()
    private val mLayoutInflater = LayoutInflater.from(cxt)
    private var mListener: IOngoingCallLayoutLayoutListener? = null

    init {
        val resources = cxt.resources
        mDataList.add(CallToolInfo(CallToolType.MUTE, resources.getString(R.string.mute), R.drawable.ic_iphone_mute, false))
        mDataList.add(CallToolInfo(CallToolType.DIAL_UP_KEYBOARD, resources.getString(R.string.dial_up_keyboard), R.drawable.ic_iphone_keyboard, false))
        mDataList.add(CallToolInfo(CallToolType.ON_SPEAKERPHONE, resources.getString(R.string.on_speakerphone), R.drawable.ic_iphone_voice, false))
        mDataList.add(CallToolInfo(CallToolType.ADD_PHONE, resources.getString(R.string.add_phone), R.drawable.ic_iphone_add))
        mDataList.add(CallToolInfo(CallToolType.KEEP, resources.getString(R.string.keep), R.drawable.ic_iphone_hold, false))
        mDataList.add(CallToolInfo(CallToolType.SOUND_RECORDING, resources.getString(R.string.sound_recording), R.drawable.ic_iphone_record, false))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NormalViewHolder {
        return NormalViewHolder(DataBindingUtil.inflate(mLayoutInflater,
                R.layout.item_ongoing_call_tool, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NormalViewHolder, position: Int) {
        holder.onBindDataToView(mDataList[position])
    }

    override fun getItemCount(): Int = MAX_ITEM_COUNT

    fun setListener(listener: IOngoingCallLayoutLayoutListener) {
        mListener = listener
    }

    fun getItemDataByToolType(toolType: CallToolType): CallToolInfo? {
        for (info in mDataList) {
            if (info.type == toolType) {
                return info
            }
        }
        return null
    }

    companion object {
        private const val MAX_ITEM_COUNT = 6
    }

    inner class NormalViewHolder(private val binding: ItemOngoingCallToolBinding) :
            RecyclerView.ViewHolder(binding.root) {
        private var mToolInfo: CallToolInfo? = null

        init {
            binding.root.setOnTouchListener(DefaultButtonClickStyleListener())
            binding.root.setOnClickListener {
                mToolInfo?.apply {
                    mListener?.onItemClick(this)
                }
            }
        }

        fun onBindDataToView(toolInfo: CallToolInfo) {
            mToolInfo = toolInfo
            binding.toolIcon.setImageResource(toolInfo.iconRes)
            binding.tvToolTitle.text = toolInfo.title
            if (toolInfo.state) {
                binding.tvToolTitle.setTextColor(Color.parseColor("#FF1971F5"))
            } else {
                binding.tvToolTitle.setTextColor(binding.root.resources.getColor(R.color.white))
            }
        }
    }
}

interface IOngoingCallLayoutLayoutListener {
    fun onItemClick(toolInfo: CallToolInfo)
    fun onDigitalKeyboardItemClick(char: Char)
}

data class CallToolInfo(val type: CallToolType, val title: String, var iconRes: Int, var state: Boolean = false)

enum class CallToolType {
    //录音
    SOUND_RECORDING,

    //拨号键盘
    DIAL_UP_KEYBOARD,

    //免提
    ON_SPEAKERPHONE,

    //添加电话
    ADD_PHONE,

    //保持
    KEEP,

    //静音
    MUTE
}
