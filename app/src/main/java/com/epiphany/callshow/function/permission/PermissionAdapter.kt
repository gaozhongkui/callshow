package com.epiphany.callshow.function.permission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.epiphany.callshow.R
import com.epiphany.callshow.databinding.ItemPermissionItemBinding

class PermissionAdapter : RecyclerView.Adapter<PermissionAdapter.VH>() {


    private var mData: List<PermissionStateBean>? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val root = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_permission_item, parent, false)

        return VH(root)
    }

    fun setData(data: List<PermissionStateBean>?) {
        mData = data
        notifyDataSetChanged()
    }

    fun getData(): List<PermissionStateBean>? {
        return mData
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val data = mData?.get(position)
        holder.binding?.data = data
        holder.data = data
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: ItemPermissionItemBinding? = DataBindingUtil.bind(itemView)
        var data: PermissionStateBean? = null

        init {
            binding?.root?.setOnClickListener {
                val data = data
                if (data?.isAuth == false) {
                    data.handle.invoke(it.context)
                }
            }
        }
    }
}