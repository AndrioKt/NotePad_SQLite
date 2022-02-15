package com.example.a13_sqllite_lowlevel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a13_sqllite_lowlevel.databinding.ActivityMainBinding
import com.example.a13_sqllite_lowlevel.db.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//создадим package db где будем хранить все что связано с БД(переменные, константы, версию, названия баз данных)
class MainActivity : AppCompatActivity() {
    lateinit var bindClass: ActivityMainBinding
    val MyDBManager = MyDBManager(this) //создаем обьект класса MyDBManager, и контент - это активити (this)
    private var job: Job? = null //создадим переменную для остановки корутины

    val myAdapter = MyAdapter(ArrayList(),this)//создадим обьект адаптера

    override fun onCreate(savedInstanceState: Bundle?) {
        bindClass = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(bindClass.root)
        init() //инициализируем адаптер
        initSearchView()//инициализируем окно поиска
    }

    override fun onResume() {
        super.onResume()
        MyDBManager.openDB()//открываем БД
        fillAdapter("")

    }

    fun onClickNew(view: View) {//кнопка создания новой записи запускает новое окно
    val i = Intent(this,EditActivity::class.java)//инициализируем переменную перехода во второе активити
        startActivity(i) // запускаем переход во второе активити

    }

    override fun onDestroy() {//база данных закрывается при закрытии приложения
        super.onDestroy()
        MyDBManager.closeDB() //закрываем БД
    }

    //заполнение сохраненными записями осуществляется при помощи шаблона rc_item.
    //адаптер будет брать шаблон, заполнять его и выводить на экран

    fun init () { //создадим функцию инициализации адаптера

        bindClass.rcView.layoutManager = LinearLayoutManager(this) //указываем, что элементы будут располагаться в соответствии с RecyclerView
        val swapHelper = getSwapMg()// добавляем в инициализацию отслеживание свапов
        swapHelper.attachToRecyclerView(bindClass.rcView)//добавляем в rcView возможность проводить свап
        bindClass.rcView.adapter = myAdapter//присваиваем RecyclerView адаптер
    }

    private fun initSearchView(){
        bindClass.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{//setOnQueryTextListener обеспечивает поиск с постоянно обновляющейся очередью (написали 1 букву ищет по ней, дописали вторую, начинает искать по 2 буквам и тд)
            override fun onQueryTextSubmit(p0: String?): Boolean {//данный метод обеспечивает поиск только после нажатия подтверждения (submit)
            return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                // myAdapter.UpdateAdapter(MyDBManager.readDBData(text!!))//каждый раз будет обновляться адаптер (эта строка не актуальна в связи с добавлением Coroutines)
                //для добавления coroutines в gradle имплементируем строку implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0'
                fillAdapter(text!!) //для многопоточности
                return true
            }
        })
    }
    private fun fillAdapter(text: String){//метод, который заполнит адаптер данными из базы данных
        job?.cancel() //если корутина не запущена, то она создастся, а если одна уже запущена и снова пришел запрос, то произойдет остановка и запуск новой корутины
       job = CoroutineScope(Dispatchers.Main).launch{//создаем корутину(сопрограмму) для основного потока
            val list = MyDBManager.readDBData(text) //благодаря корутинам, данная функция заблокирует не весь поток а только корутину, пока не полностью не считает всю базу данных
            myAdapter.UpdateAdapter(list)//обновляем адаптер заполняя данными из БД
            if(list.size >0)bindClass.tvNoElements.visibility = View.GONE
            else bindClass.tvNoElements.visibility = View.VISIBLE
        }
    }

    private fun getSwapMg() : ItemTouchHelper{//создадим функцию, которая отслеживает drag и swipe
        return ItemTouchHelper(object :ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT){
            //данная строка указывает что отслеживание drag = 0, а отслеживание swipe будет выполняться и в право и влево

            override fun onMove(//заимплементируем методы для drag и swipe
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false//т.к drag у нас не включен то возвращаем false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {//функция выполнения действий при свайпе
                myAdapter.removeItem(viewHolder.adapterPosition, MyDBManager)//из viewHolder необходимо взять позицию по которой мы будем удалять, а так же указываем базу с которой будем удалять
            }
        })
    }
}

