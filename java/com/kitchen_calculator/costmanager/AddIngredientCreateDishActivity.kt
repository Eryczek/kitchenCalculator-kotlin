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
import com.kitchen_calculator.costmanager.data.objects.AddIngredient
import com.kitchen_calculator.costmanager.data.objects.Dishes
import com.kitchen_calculator.costmanager.data.objects.Ingredients
import com.kitchen_calculator.costmanager.data.objects.IngredientsDish
import com.kitchen_calculator.costmanager.utilities.AddIngredientsRecycleViewAdapter
import kotlinx.android.synthetic.main.activity_add_ingredient_create_dish.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AddIngredientCreateDishActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    AddIngredientsRecycleViewAdapter.AddIngredientsItemListener  {

    private var spinner: Spinner? = null
    private var db: CostManagerDataBase? = null
    private lateinit var ingredList: ArrayList<Ingredients>
    private lateinit var ingredientsChosenList: ArrayList<AddIngredient>
    private lateinit var deleteIngredientList: ArrayList<AddIngredient>
    private val context: Context = this
    private var pos: Int = 0
    private lateinit var itemSpinnerSelected: Ingredients
    private var dishName: String = ""
    private var dishPortions: String = ""
    private var dishWaste: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient_create_dish)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewAddIngredientCreateDish)

        db = CostManagerDataBase.getDataBase(this)

        ingredientsChosenList = ArrayList<AddIngredient>()
        deleteIngredientList = ArrayList<AddIngredient>()

        spinner = findViewById(R.id.spinnerAddIngredientCreateDish)
        spinner!!.setOnItemSelectedListener(this@AddIngredientCreateDishActivity)

        var arraySpinnerAdapter: ArrayAdapter<String>
        var intent = intent

        dishName = intent.getStringExtra("dishName")
        dishPortions = intent.getStringExtra("dishPortions")
        dishWaste = intent.getStringExtra("dishWaste")

        CoroutineScope(Dispatchers.IO).launch {

            if(savedInstanceState != null && savedInstanceState.containsKey("ingredStringList")){

                val ingredStringList = savedInstanceState.getStringArrayList("ingredStringList")

                ingredList = ArrayList<Ingredients>()

                for (ingred: String in ingredStringList!!)
                    ingredList.add(db!!.ingredientsDao().getIngredient(ingred)[0])

            } else {

                ingredList = ArrayList(db!!.ingredientsDao().getAll())

            }

            if(savedInstanceState != null && savedInstanceState.containsKey("ingredientsChosenNameStringList")){

                val ingredientsChosenNameStringList = savedInstanceState.getStringArrayList("ingredientsChosenNameStringList")
                val ingredientsChosenCuantityStringList = savedInstanceState.getStringArrayList("ingredientsChosenCuantityStringList")
                val ingredientsChosenPriceStringList = savedInstanceState.getStringArrayList("ingredientsChosenPriceStringList")

                for (i in 0..(ingredientsChosenNameStringList!!.size - 1)) {

                    val ingred = db!!.ingredientsDao().getIngredient(ingredientsChosenNameStringList.get(i))[0]

                    ingredientsChosenList.add(AddIngredient(ingred.id!!, ingred.name, ingred.cuantityType, ingredientsChosenCuantityStringList!!.get(i).toFloat(), ingredientsChosenPriceStringList!!.get(i).toFloat(), false))
                }
            }

            if (savedInstanceState != null && savedInstanceState.containsKey("deleteIngredientStringList")){

                val deleteIngredientStringList = savedInstanceState.getStringArrayList("deleteIngredientStringList")

                for (deleteIngred: String in deleteIngredientStringList!!){
                    for (ingredChosen: AddIngredient in ingredientsChosenList) {
                        if (deleteIngred.contentEquals(ingredChosen.name)) {
                            ingredChosen.isSelected = true
                            deleteIngredientList.add(ingredChosen)
                        }
                    }
                }

            }

            val recyclerViewAdapter = AddIngredientsRecycleViewAdapter(ingredientsChosenList.sortedBy { it.name }, this@AddIngredientCreateDishActivity)
            recyclerView.adapter = recyclerViewAdapter

            val txtDishName = findViewById<TextView>(R.id.txtDishNameAddIngredientCreateDish)
            txtDishName.text = context.getString(R.string.name) + " " + dishName

            arraySpinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getStringArrayFromIngredientsList())
            spinner!!.adapter = arraySpinnerAdapter

            if (savedInstanceState != null && savedInstanceState.containsKey("positionSpinner"))
                spinner!!.setSelection(savedInstanceState.getInt("positionSpinner"))

        }

        btnAddIngredient_AddIngredientCreateDish.setOnClickListener {

            val etxtCuantityIngredientCreateDishActivity = findViewById<EditText>(R.id.etxtCuantityAddIngredientCreateDish)
            var cuantity: Float

            if(etxtCuantityIngredientCreateDishActivity.text.isNullOrBlank()){
                cuantity = 0F
            } else {
                cuantity = etxtCuantityIngredientCreateDishActivity.text.toString().toFloat()
            }

            if (ingredList.isEmpty()) {
                runOnUiThread{Toast.makeText(context, context.getString(R.string.notMoreIngredients), Toast.LENGTH_LONG).show()}
            }else if(cuantity == 0F  ){
                runOnUiThread{Toast.makeText(context, context.getString(R.string.missingCuantity), Toast.LENGTH_LONG).show()}
            } else {

                for (ingredient: Ingredients in ingredList) {

                    if (itemSpinnerSelected.name.contentEquals(ingredient.name)) {

                        ingredientsChosenList.add(AddIngredient(ingredient.id!!, ingredient.name, ingredient.cuantityType, cuantity, ((ingredient.price/ingredient.cuantity) * cuantity), false))

                        arraySpinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getStringArrayFromIngredientsList())
                        spinner!!.adapter = arraySpinnerAdapter

                    }
                }

                ingredList.remove(itemSpinnerSelected)

                val recyclerViewAdapter = AddIngredientsRecycleViewAdapter(ingredientsChosenList.toList().sortedBy { it.name }, this@AddIngredientCreateDishActivity)
                recyclerView.adapter = recyclerViewAdapter

                etxtCuantityIngredientCreateDishActivity.setText("")
            }

        }

        btnDeleteIngredient_AddIngredientCreateDish.setOnClickListener {

            if(deleteIngredientList.isEmpty()){
                Toast.makeText(context, context.getString(R.string.notIngredientSelected), Toast.LENGTH_LONG).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {

                    for (deleteIngredient: AddIngredient in deleteIngredientList) {
                        ingredientsChosenList.remove(deleteIngredient)
                        ingredList.add(db!!.ingredientsDao().getIngredient(deleteIngredient.name)[0])
                    }

                    deleteIngredientList = ArrayList<AddIngredient>()

                    var arraySpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getStringArrayFromIngredientsList())
                    runOnUiThread {  spinner!!.adapter = arraySpinnerAdapter }

                    val recyclerViewAdapter = AddIngredientsRecycleViewAdapter(ingredientsChosenList.toList().sortedBy { it.name }, this@AddIngredientCreateDishActivity)
                    runOnUiThread { recyclerView.adapter = recyclerViewAdapter }

                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {


        if(!ingredList.isNullOrEmpty()) {

            pos = position
            itemSpinnerSelected = ingredList.get(position)

            val txtCuantityTypeAddIngredientCreateDishActivity = findViewById<TextView>(R.id.txtCuantityTypeAddIngredientCreateDish)

            when(itemSpinnerSelected.cuantityType){
                "Unit" -> txtCuantityTypeAddIngredientCreateDishActivity.text = getString(R.string.unites)
                "Kg" -> txtCuantityTypeAddIngredientCreateDishActivity.text = getString(R.string.kg)
                "Litre" -> txtCuantityTypeAddIngredientCreateDishActivity.text = getString(R.string.litres)
            }
        }
    }

    fun getStringArrayFromIngredientsList(): ArrayList<String> {

        var arrayList: ArrayList<String> = ArrayList()

        if (ingredList.isNullOrEmpty()) {

            arrayList.add(context.getString(R.string.noIngredients))

            return arrayList

        } else {

            var found = false

            ingredList = ArrayList(ingredList.sortedBy { it.name })

            for (ingred: Ingredients in ingredList) {
                if (ingredientsChosenList.isNotEmpty()) {
                    for (chosenIngredient: AddIngredient in ingredientsChosenList)
                        if (chosenIngredient.name.contentEquals(ingred.name)) {
                            found = true
                        }
                }

                if (found) {
                    found = false
                } else {
                    arrayList.add(ingred.name)
                }
            }

            if (arrayList.isEmpty()) {
                arrayList.add(context.getString(R.string.noIngredients))
            }
            return arrayList
        }
    }

    override fun onAddIngredientsItemClick(ingredientsDish: AddIngredient) {

        var found = false

        ingredientsDish.isSelected = !ingredientsDish.isSelected

        for (ingredient: AddIngredient in deleteIngredientList){
            if(ingredient.name.contentEquals(ingredientsDish.name)){
                found = true
            }
        }

        if (found){
            deleteIngredientList.remove(ingredientsDish)
        } else {
            deleteIngredientList.add(ingredientsDish)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(context, CreateDishActivity::class.java)

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

        if(item.itemId == R.id.saveIcon){

            var intent = Intent(context, DishActivity::class.java)
            var price: Float = 0F

            if(ingredientsChosenList.isEmpty()){
                Toast.makeText(context, context.getString(R.string.notIngredientchosen), Toast.LENGTH_LONG).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {

                    db!!.dishesDao().inserDish(Dishes(null, dishName, dishPortions.toFloat(), dishWaste.toFloat(), 0F))
                    val dish = db!!.dishesDao().getDishByName(dishName)[0]

                    for (ingredient: AddIngredient in ingredientsChosenList) {
                        price += ingredient.price
                        db!!.ingredientsDishDao().insertIngredientDish(IngredientsDish(ingredient.idIngredient, dish!!.id!!, ingredient.name, ingredient.cuantityType, ingredient.cuantity, ingredient.price))
                    }

                    db!!.dishesDao().updatePrice(dish!!.id!!, price)

                    startActivity(intent)
                    finish()
                }
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {

        val ingredStringList = ArrayList<String>()
        for (ingred: Ingredients in ingredList)
            ingredStringList.add(ingred.name)

        if(ingredStringList.isNotEmpty())
            outState.putStringArrayList("ingredStringList", ingredStringList)

        val ingredientsChosenNameStringList = ArrayList<String>()
        val ingredientsChosenCuantityStringList = ArrayList<String>()
        val ingredientsChosenPriceStringList = ArrayList<String>()

        for(ingredChosen: AddIngredient in ingredientsChosenList) {

            ingredientsChosenNameStringList.add(ingredChosen.name)
            ingredientsChosenCuantityStringList.add(ingredChosen.cuantity.toString())
            ingredientsChosenPriceStringList.add(ingredChosen.price.toString())
        }

        if(ingredientsChosenNameStringList.isNotEmpty()) {
            outState.putStringArrayList("ingredientsChosenNameStringList", ingredientsChosenNameStringList)
            outState.putStringArrayList("ingredientsChosenCuantityStringList", ingredientsChosenCuantityStringList)
            outState.putStringArrayList("ingredientsChosenPriceStringList", ingredientsChosenPriceStringList)
        }

        val deletedIngredientStringList = ArrayList<String>()
        for (deletedIngred: AddIngredient in deleteIngredientList) {

            deletedIngredientStringList.add(deletedIngred.name)
        }

        if(deletedIngredientStringList.isNotEmpty())
            outState.putStringArrayList("deleteIngredientStringList", deletedIngredientStringList)

        if(pos > 0)
            outState.putInt("positionSpinner", pos)

        super.onSaveInstanceState(outState)
    }

}