package com.kitchen_calculator.costmanager.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kitchen_calculator.costmanager.R
import com.kitchen_calculator.costmanager.data.objects.Dishes

class DishesRecycleViewAdapter (val dishesList: List<Dishes>, val context: Context, val itemListener: dishItemListener):
    RecyclerView.Adapter<DishesRecycleViewAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.txtNameGeneralGridItem)
        val price = itemView.findViewById<TextView>(R.id.txtPriceGeneralGridItem)
    }

    override fun getItemCount() = dishesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        DishesRecycleViewAdapter.ViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.general_grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dish = dishesList[position]
        with(holder){
            name?.let {
                it.text = dish.name
                it.contentDescription = dish.name
            }
            price?.let{
                val dishPrice = dish.price!! / (dish.portions - ((dish.portions * dish.waste)/100F))
                it.text = "£" + String.format("%.2f ", dishPrice) + context.getString(R.string.perPortion)
            }

            holder.itemView.setOnClickListener {
                itemListener.onDishItemClicked(dish)
            }
        }
    }

    interface dishItemListener {
        fun onDishItemClicked(dish: Dishes)
    }

}
