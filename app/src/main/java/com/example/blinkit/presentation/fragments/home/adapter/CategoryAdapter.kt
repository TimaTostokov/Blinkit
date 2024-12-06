package com.example.blinkit.presentation.fragments.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.blinkit.data.remote.model.category.Category
import com.example.blinkit.databinding.ItemViewProductCategoryBinding

class CategoryAdapter(private val list: ArrayList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemViewProductCategoryBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemViewProductCategoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = list[position]
        holder.binding.apply {
            category.image?.let { ivCategoryImage.setImageResource(it) }
            tvVegetable.text = category.title
        }
    }

    override fun getItemCount() = list.size

}