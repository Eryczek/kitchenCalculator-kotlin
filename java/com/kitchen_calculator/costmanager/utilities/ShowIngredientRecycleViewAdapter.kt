package com.kitchen_calculator.costmanager.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kitchen_calculator.costmanager.R
import com.kitchen_calculator.costmanager.data.objects.Dishes

class ShowIngredientRecycleViewAdapter (val dish: List<Dishes>):
    RecyclerView.Adapter<ShowIngredientRecycleViewAdapter.ViewHolder>(){
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.txtNameShowGeneralGridItem)
        val price = itemView.findViewById<TextView>(R.id.txtPriceShowGeneralGridItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ShowIngredientRecycleViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.show_general_grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = dish.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val d = dish[position]
        with(holder) {
            name?.let {
                it.text = d.name
                it.contentDescription = d.name
            }
            price?.let {
                it.text = ""
            }

        }
    }
}