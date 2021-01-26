package com.pete.nearsample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pete.nearsample.databinding.BlockItemviewBinding

class BlockAdapter : RecyclerView.Adapter<BlockAdapter.ViewHolder>() {

    val list = ArrayList<User>()

    inner class ViewHolder(private val binding: BlockItemviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            with(binding) {
                val ResultItem = list[position]
                tvVotedName.text = ResultItem.votedName
                tvPreviousHash.text = ResultItem.previousHash
                tvTotalPoint.text = ResultItem.totalPoint.toString()
                tvTimeStamp.text = ResultItem.timestamp
            }
        }
    }

    fun addNewUser(user: User) {
        list.add(user)
//        notifyItemChanged(list.size-1)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =  BlockItemviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}