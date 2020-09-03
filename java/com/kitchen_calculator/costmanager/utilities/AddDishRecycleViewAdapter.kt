package com.kitchen_calculator.costmanager.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kitchen_calculator.costmanager.R
import com.kitchen_calculator.costmanager.data.objects.AddDish
import com.kitchen_calculator.costmanager.data.objects.DishesMenu
import kotlinx.android.synthetic.main.add_ingredient_and_dish_grid_item.view.*

class AddDishRecycleViewAdapter(val dishes: List<AddDish>, val itemListener: AddDishItemListener, val context: Context): RecyclerView.Adapter<AddDishRecycleViewAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.txtNameAddIngredientAndDishGridItem)
        val price = itemView.findViewById<TextView>(R.id.txtPriceAddIngredientAndDishGridItem)
        val layout = itemView.findViewById<ConstraintLayout>(R.id.constraintLayoutAddIngredientAndDishGridItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddDishRecycleViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.add_ingredient_and_dish_grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = dishes.size

    override fun onBindViewHolder(holder: AddDishRecycleViewAdapter.ViewHolder, position: Int) {
        val dish = dishes[position]
        with(holder){
            name?.let {
                it.text = dish.name
                it.contentDescription = dish.name
            }
            price?.let {
                it.text = "£" + String.format("%.2f", dish.price) + " " + context.getString(R.string.perPortion)
            }
            layout?.let {
                if(dish.isSelected){
                    it.setBackgroundResource(R.drawable.background_selected)
                } else {
                    it.setBackgroundResource(R.drawable.background)
                }
            }

            holder.itemView.setOnClickListener {
                itemListener.onAddDishItemClick(dish)

                if(dish.isSelected){
                    layout.setBackgroundResource(R.drawable.background_selected)
                } else {
                    layout.setBackgroundResource(R.drawable.background)
                }
            }
        }
    }

    interface  AddDishItemListener {
        fun onAddDishItemClick(dishMenu: AddDish)
    }
}