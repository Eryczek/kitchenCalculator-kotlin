package com.kitchen_calculator.costmanager.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kitchen_calculator.costmanager.R
import com.kitchen_calculator.costmanager.data.objects.Menus

class MenusRecycleViewAdapter(val menusList: List<Menus>, val context: Context, val itemListener: menuItemListener):
    RecyclerView.Adapter<MenusRecycleViewAdapter.ViewHolder>(){

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.txtNameGeneralGridItem)
        val price = itemView.findViewById<TextView>(R.id.txtPriceGeneralGridItem)
    }

    override fun getItemCount() = menusList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MenusRecycleViewAdapter.ViewHolder{
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.general_grid_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menusList[position]
        with(holder){
            name?.let {
                it.text = menu.name
                it.contentDescription = menu.name
            }
            price?.let {
                if(!menu.restaurant.isNullOrBlank() && !menu.menuType.isNullOrBlank()){
                    it.text = menu.restaurant + " - " + menu.menuType
                } else if(!menu.restaurant.isNullOrBlank()){
                    it.text = menu.restaurant
                } else if(!menu.menuType.isNullOrBlank()){
                    it.text = menu.menuType
                } else {
                    it.text = ""
                }

            }

            holder.itemView.setOnClickListener {
                itemListener.onMenuItemClicked(menu)
            }
        }
    }

    interface menuItemListener {
        fun onMenuItemClicked(menu: Menus)
    }


}