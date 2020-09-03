package com.kitchen_calculator.costmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kitchen_calculator.costmanager.data.CostManagerDataBase
import com.kitchen_calculator.costmanager.data.objects.*
import com.kitchen_calculator.costmanager.utilities.AddIngredientsRecycleViewAdapter
import kotlinx.android.synthetic.main.activity_edit_dish.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDishActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, AddIngredientsRecycleViewAdapter.AddIngredientsItemListener {

    private var dishName: String = ""
    private var pos = 0
    private val context = this
    private var db: CostManagerDataBase? = null
    private var dish: Dishes? = null
    private lateinit var ingredientChosenList: ArrayList<AddIngredient>
    private lateinit var ingredientsList: ArrayList<Ingredients>
    private lateinit var deleteIngredientList: ArrayList<AddIngredient>
    private var spinner: Spinner? = null
    private lateinit var itemSpinnerSelected: Ingredients

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_dish)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewEditDish)

        var intent = intent
        dishName = intent.getStringExtra("dishName")

        deleteIngredientList = ArrayList<AddIngredient>()

        spinner = findViewById(R.id.spinnerEditDish)
        spinner!!.setOnItemSelectedListener(this)

        var arraySpinnerAdapter: ArrayAdapter<String>

        db = CostManagerDataBase.getDataBase(this)

        CoroutineScope(Dispatchers.IO).launch {

            dish = db!!.dishesDao().getDishByName(dishName)[0]
            ingredientChosenList = ArrayList<AddIngredient>()


            if(savedInstanceState != null && savedInstanceState.containsKey("ingredStringList")){

                val ingredStringList = savedInstanceState.getStringArrayList("ingredStringList")

                ingredientsList = ArrayList<Ingredients>()

                for (ingred: String in ingredStringList!!)
                    ingredientsList.add(db!!.ingredientsDao().getIngredient(ingred)[0])

            } else {

                ingredientsList = ArrayList(db!!.ingredientsDao().getAll())

            }

            if(savedInstanceState != null && savedInstanceState.containsKey("ingredientsChosenNameStringList")){

                val ingredientsChosenNameStringList = savedInstanceState.getStringArrayList("ingredientsChosenNameStringList")
                val ingredientsChosenCuantityStringList = savedInstanceState.getStringArrayList("ingredientsChosenCuantityStringList")
                val ingredientsChosenPriceStringList = savedInstanceState.getStringArrayList("ingredientsChosenPriceStringList")

                for (i in 0..(ingredientsChosenNameStringList!!.size - 1)) {

                    val ingred = db!!.ingredientsDao().getIngredient(ingredientsChosenNameStringList.get(i))[0]

                    ingredientChosenList.add(AddIngredient(ingred.id!!, ingred.name, ingred.cuantityType,ingredientsChosenCuantityStringList!!.get(i).toFloat(), ingredientsChosenPriceStringList!!.get(i).toFloat(), false ))
                }

            } else {

                for (ingredient: IngredientsDish in db!!.ingredientsDishDao().getingredientsDishD(dish!!.id!!)) {
                    ingredientChosenList.add(AddIngredient(ingredient.ingredientId, ingredient.ingredientName, ingredient.ingredientCuantityType, ingredient.cuantity, ingredient.price, false))
                }

                for (ingredDish: AddIngredient in ingredientChosenList) {

                    val ingred = db!!.ingredientsDao().getIngredient(ingredDish.name)

                    if (!ingred.isNullOrEmpty())
                        ingredientsList.remove(ingred[0])
                }

            }

            if (savedInstanceState != null && savedInstanceState.containsKey("deleteIngredientStringList")){

                val deleteIngredientStringList = savedInstanceState.getStringArrayList("deleteIngredientStringList")

                for (deleteIngred: String in deleteIngredientStringList!!){
                    for (ingredChosen: AddIngredient in ingredientChosenList) {
                        if (deleteIngred.contentEquals(ingredChosen.name)) {
                            ingredChosen.isSelected = true
                            deleteIngredientList.add(ingredChosen)
                        }
                    }
                }

            }

            findViewById<EditText>(R.id.etxtNameEditDish).setText(dish!!.name)
            findViewById<EditText>(R.id.etxtPortionEditDish).setText(dish!!.portions.toString())
            findViewById<EditText>(R.id.etxtWasteEditDish).setText(dish!!.waste.toString())

            arraySpinnerAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                getStringArrayFromIngredientsList()
            )

            spinner!!.adapter = arraySpinnerAdapter

            if (savedInstanceState != null && savedInstanceState.containsKey("positionSpinner"))
                spinner!!.setSelection(savedInstanceState.getInt("positionSpinner"))

            val recyclerViewAdapter = AddIngredientsRecycleViewAdapter(
                ingredientChosenList.toList().sortedBy { it.name },
                this@EditDishActivity
            )
            recyclerView.adapter = recyclerViewAdapter
        }

        btnAddIngredientEditDish.setOnClickListener {

            val etxtCuantityEditDishActivity =
                findViewById<EditText>(R.id.etxtCuantityEditDish)
            var cuantity: Float

            if (etxtCuantityEditDishActivity.text.isNullOrBlank()) {
                cuantity = 0F
            } else {
                cuantity = etxtCuantityEditDishActivity.text.toString().toFloat()
            }

            if (ingredientsList.isEmpty()) {
                runOnUiThread { Toast.makeText(context,context.getString(R.string.notMoreIngredients), Toast.LENGTH_LONG).show() }
            } else if (cuantity == 0F) {
                runOnUiThread { Toast.makeText(context, context.getString(R.string.missingCuantity), Toast.LENGTH_LONG).show() }
            } else {

                for (ingredient: Ingredients in ingredientsList) {

                    if (itemSpinnerSelected.name.contentEquals(ingredient.name)) {

                        ingredientChosenList.add(AddIngredient(ingredient.id!!, ingredient.name, ingredient.cuantityType, cuantity, ((ingredient.price / ingredient.cuantity) * cuantity), false))

                        arraySpinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getStringArrayFromIngredientsList())
                        spinner!!.adapter = arraySpinnerAdapter
                    }
                }

                ingredientsList.remove(itemSpinnerSelected)

                val recyclerViewAdapter = AddIngredientsRecycleViewAdapter(ingredientChosenList.toList().sortedBy { it.name }, this@EditDishActivity)
                recyclerView.adapter = recyclerViewAdapter

                etxtCuantityEditDishActivity.setText("")
            }
        }

        btnDeleteIngredientEditDish.setOnClickListener {

            if(deleteIngredientList.isEmpty()){
                Toast.makeText(context, context.getString(R.string.notIngredientSelected), Toast.LENGTH_LONG).show()
            } else {
                CoroutineScope(Dispatchers.IO).launch {

                    for (deleteIngredient: AddIngredient in deleteIngredientList) {
                        ingredientChosenList.remove(deleteIngredient)
                        ingredientsList.add(db!!.ingredientsDao().getIngredient(deleteIngredient.name)[0])
                    }

                    deleteIngredientList = ArrayList<AddIngredient>()

                    var arraySpinnerAdapter: ArrayAdapter<String> = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, getStringArrayFromIngredientsList())
                    runOnUiThread {  spinner!!.adapter = arraySpinnerAdapter }

                    val recyclerViewAdapter = AddIngredientsRecycleViewAdapter(ingredientChosenList.toList().sortedBy { it.name }, this@EditDishActivity)
                    runOnUiThread { recyclerView.adapter = recyclerViewAdapter }

                }
            }
        }

    }

    fun getStringArrayFromIngredientsList(): ArrayList<String> {

        var arrayList: ArrayList<String> = ArrayList()

        if (ingredientsList.size == 0) {

            arrayList.add(context.getString(R.string.noIngredients))

            return arrayList

        } else {

            var found = false

            ingredientsList = ArrayList(ingredientsList.sortedBy { it.name })

            for (ingred: Ingredients in ingredientsList) {
                if (ingredientChosenList.isNotEmpty()) {
                    for (chosenIngredient: AddIngredient in ingredientChosenList)
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

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        if (ingredientsList.isNotEmpty()) {

            itemSpinnerSelected = ingredientsList.get(position)

            pos = position

            val txtCuantityTypeAddIngredientCreateDishActivity =
                findViewById<TextView>(R.id.txtCuantityTypeEditDish)

            when(itemSpinnerSelected.cuantityType){
                "Unit" -> txtCuantityTypeAddIngredientCreateDishActivity.text = getString(R.string.unites)
                "Kg" -> txtCuantityTypeAddIngredientCreateDishActivity.text = getString(R.string.kg)
                "Litre" -> txtCuantityTypeAddIngredientCreateDishActivity.text = getString(R.string.litres)
            }
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
        val intent = Intent(context, ShowDishActivity::class.java)

        intent.putExtra("dishName", dishName)

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

            val intent = Intent(context, ShowDishActivity::class.java)

            var price = 0F
            var dishPortions = 0F
            var dishWaste = 0F

            CoroutineScope(Dispatchers.IO).launch {

                if (findViewById<EditText>(R.id.etxtWasteEditDish).text.toString().isNullOrBlank()) {
                    dishWaste = -1F
                } else {
                    dishWaste = findViewById<EditText>(R.id.etxtWasteEditDish).text.toString().toFloat()
                }

                if (findViewById<EditText>(R.id.etxtPortionEditDish).text.toString().isNullOrBlank()) {
                    dishPortions = -1F
                } else {
                    dishPortions = findViewById<EditText>(R.id.etxtPortionEditDish).text.toString().toFloat()
                }

                val dishExisting: List<Dishes>?
                if (!findViewById<EditText>(R.id.etxtNameEditDish).text.toString().capitalize().contentEquals(dishName)) {
                    dishName = findViewById<EditText>(R.id.etxtNameEditDish).text.toString().capitalize()
                    dishExisting = db!!.dishesDao().getDishByName(dishName)
                } else {
                    dishExisting = null
                }

                if(dishName.isNullOrBlank()){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.emptyNameField), Toast.LENGTH_LONG).show() }
                } else if(!dishExisting.isNullOrEmpty()) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.theName) + dishExisting[0].name + context.getString(R.string.alreadyExist), Toast.LENGTH_LONG).show() }
                } else if (dishWaste == -1F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.notWasteValue), Toast.LENGTH_LONG).show()}
                } else if(dishWaste < 0F || dishWaste >=100F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.addCorrectWasteValue), Toast.LENGTH_LONG).show() }
                } else if (dishPortions < 1F && dishPortions >= 0F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.addCorrectPortions), Toast.LENGTH_LONG).show() }
                } else if (dishPortions == -1F) {
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.addPortionsValue), Toast.LENGTH_LONG).show() }
                } else if(ingredientChosenList.isEmpty()){
                    runOnUiThread { Toast.makeText(context, context.getString(R.string.notIngredientchosen), Toast.LENGTH_LONG).show() }
                } else {

                    db!!.ingredientsDishDao().deleteAllIngredientsFromDish(dish!!.id!!)

                    for (ingredient: AddIngredient in ingredientChosenList) {
                        price += ingredient.price
                        db!!.ingredientsDishDao().insertIngredientDish(IngredientsDish(ingredient.idIngredient, dish!!.id!!, ingredient.name, ingredient.cuantityType, ingredient.cuantity, ingredient.price))
                    }

                    db!!.dishesDao().updateDish(dish!!.id!!, dishName, dishPortions, dishWaste, price)

                    for(menuDishes: DishesMenu in db!!.dishesMenuDao().getDishesMenuByDishId(dish!!.id!!)){
                        db!!.dishesMenuDao().updateDishMenu(menuDishes.menuId, dish!!.id!!, dishName, (price / (dishPortions - ((dishPortions * dishWaste)/100F))))
                    }

                    intent.putExtra("dishName", dishName)

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
        for (ingred: Ingredients in ingredientsList)
            ingredStringList.add(ingred.name)

        if(ingredStringList.isNotEmpty())
            outState.putStringArrayList("ingredStringList", ingredStringList)

        val ingredientsChosenNameStringList = ArrayList<String>()
        val ingredientsChosenCuantityStringList = ArrayList<String>()
        val ingredientsChosenPriceStringList = ArrayList<String>()
        for(ingredChosen: AddIngredient in ingredientChosenList) {

            ingredientsChosenNameStringList.add(ingredChosen.name)
            ingredientsChosenCuantityStringList.add(ingredChosen.cuantity.toString())
            ingredientsChosenPriceStringList.add(ingredChosen.price.toString())
        }

        outState.putStringArrayList("ingredientsChosenNameStringList", ingredientsChosenNameStringList)
        outState.putStringArrayList("ingredientsChosenCuantityStringList", ingredientsChosenCuantityStringList)
        outState.putStringArrayList("ingredientsChosenPriceStringList", ingredientsChosenPriceStringList)

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
