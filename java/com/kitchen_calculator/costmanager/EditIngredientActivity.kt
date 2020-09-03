package com.kitchen_calculator.costmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.kitchen_calculator.costmanager.data.CostManagerDataBase
import com.kitchen_calculator.costmanager.data.objects.Ingredients
import com.kitchen_calculator.costmanager.data.objects.IngredientsDish
import kotlinx.android.synthetic.main.activity_edit_ingredient.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditIngredientActivity : AppCompatActivity() {

    private var db: CostManagerDataBase? = null
    private var ingredient: Ingredients? = null
    private  val context = this
    private var ingredName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_ingredient)

        db = CostManagerDataBase.getDataBase(this)

        ingredName = intent.getStringExtra("ingredientName")

        CoroutineScope(Dispatchers.IO).launch {

            ingredient = db!!.ingredientsDao().getIngredient(ingredName)[0]

            etxtNameEditIngredient.setText(ingredient!!.name)
            etxtCuantityEditIngredient.setText(ingredient!!.cuantity.toString())
            etxtPriceEditIngredient.setText(ingredient!!.price.toString())
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(context, ShowIngredientActivity::class.java)

        intent.putExtra("ingredientName", ingredName)

        startActivity(intent)
        finish()
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

        if (item.itemId == R.id.saveIcon){

            var intent = Intent(this, ShowIngredientActivity::class.java)

            var found = false

            val name = etxtNameEditIngredient.text.toString().capitalize()
            var cuantity = findViewById<EditText>(R.id.etxtCuantityEditIngredient).text
            var price = findViewById<EditText>(R.id.etxtPriceEditIngredient).text

            CoroutineScope(Dispatchers.IO).launch {

                val ingredients = db!!.ingredientsDao().getAll()

                for (ingred: Ingredients in ingredients){
                    if (ingred.name.contentEquals(name) && !ingred.name.contentEquals(ingredName)) {
                        found = true
                    }
                }

                if (name.isNullOrBlank()){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.emptyNameField), Toast.LENGTH_LONG).show()}
                } else if(found){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.theName) + name + context.getString(R.string.alreadyExist), Toast.LENGTH_LONG).show()}
                } else if(cuantity.isNullOrBlank()){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.missingCuantity), Toast.LENGTH_LONG).show()}
                } else if(price.isNullOrBlank()){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.notPrice), Toast.LENGTH_LONG).show()}
                } else {

                    val ingredDishList = db!!.ingredientsDishDao().getIngredientsDishI(ingredient!!.id!!)

                    for(ingredDish: IngredientsDish in ingredDishList){

                        val oldIngredDishPrice = ingredDish.price
                        val ingredDishPrice =  (price.toString().toFloat() / cuantity.toString().toFloat()) * ingredDish.cuantity

                        db!!.ingredientsDishDao().updateIngredDish(ingredDish.dishId, ingredDish.ingredientId, name, ingredDish.ingredientCuantityType, ingredDish.cuantity, ingredDishPrice)

                        val dish = db!!.dishesDao().getDishById(ingredDish.dishId)[0]

                        val newDishPrice = (dish.price!! - oldIngredDishPrice  + ingredDishPrice)

                        db!!.dishesDao().updatePrice(dish.id!!, newDishPrice)

                    }


                    db!!.ingredientsDao().updateIngredient(ingredient!!.id!!, name, cuantity.toString().toFloat(), price.toString().toFloat())

                    intent.putExtra("ingredientName", name)

                    startActivity(intent)
                    finish()
                }
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
