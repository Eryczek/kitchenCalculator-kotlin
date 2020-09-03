package com.kitchen_calculator.costmanager

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kitchen_calculator.costmanager.data.CostManagerDataBase
import com.kitchen_calculator.costmanager.data.objects.Dishes
import com.kitchen_calculator.costmanager.data.objects.Ingredients
import com.kitchen_calculator.costmanager.data.objects.IngredientsDish
import com.kitchen_calculator.costmanager.utilities.AddIngredientsRecycleViewAdapter
import kotlinx.android.synthetic.main.activity_create_dish.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CreateDishActivity : AppCompatActivity(){

    private var db: CostManagerDataBase? = null

    private val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_dish)

        db = CostManagerDataBase.getDataBase(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_create_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home){
            onBackPressed()
            return true
        }

        if (item.itemId == R.id.saveIcon) {

            var intent = Intent(this, AddIngredientCreateDishActivity::class.java)

            val dishName = findViewById<EditText>(R.id.etxtNameCreateDish).text.toString().capitalize()
            var dishWaste: Float
            var dishPortions: Float


            CoroutineScope(Dispatchers.IO).launch {

                if (findViewById<EditText>(R.id.etxtWasteCreateDish).text.toString().isNullOrBlank()){
                    dishWaste = -1F
                } else {
                    dishWaste = findViewById<EditText>(R.id.etxtWasteCreateDish).text.toString().toFloat()
                }

                if (findViewById<EditText>(R.id.etxtPortionCreateDish).text.toString().isNullOrBlank()){
                    dishPortions = -1F
                } else {
                    dishPortions = findViewById<EditText>(R.id.etxtPortionCreateDish).text.toString().toFloat()
                }


                var dishExisting: List<Dishes>? = db!!.dishesDao().getDishByName(dishName)

                if(dishName.isNullOrBlank()){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.emptyNameField), Toast.LENGTH_LONG).show() }
                } else if(!dishExisting.isNullOrEmpty()) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.theName) + dishExisting!![0].name + context.getString(R.string.alreadyExist), Toast.LENGTH_LONG).show() }
                } else if(dishWaste == -1F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.notWasteValue), Toast.LENGTH_LONG).show() }
                } else if(dishWaste < 0F || dishWaste >=100F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.addCorrectWasteValue), Toast.LENGTH_LONG).show() }
                } else if(dishPortions < 1F && dishPortions >= 0F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.addCorrectPortions), Toast.LENGTH_LONG).show() }
                } else if(dishPortions == -1F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.addPortionsValue), Toast.LENGTH_LONG).show() }
                } else{

                    intent.putExtra("dishName", dishName)
                    intent.putExtra("dishPortions", dishPortions.toString())
                    intent.putExtra("dishWaste", dishWaste.toString())
                    startActivity(intent)
                    finish()
                }
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(context, DishActivity::class.java)

        startActivity(intent)
        finish()
    }
}


