
package com.example.testrxjava

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.testrxjava.databinding.ItemBinding

class CustomAdapter(
    private val getItemPosition: (Int)->Unit
) : RecyclerView.Adapter<CustomAdapter.CustomViewHolder>() {
    var list = listOf<ItemData>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        val item = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(item)
    }

    override fun onBindViewHolder(
        holder: CustomViewHolder,
        position: Int
    ) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList:List<ItemData>){
        list = newList
        notifyDataSetChanged()
    }

    inner class CustomViewHolder(private val binding: ItemBinding) : ViewHolder(binding.root) {
        fun bind(itemData: ItemData) {
            with(binding) {
                itemId.text = itemData.id.toString()
                itemTitle.text = itemData.title.toString()
                binding.root.setOnClickListener {
                    getItemPosition(list.indexOf(itemData))
                }
            }
        }
    }
}