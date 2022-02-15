package com.example.a13_sqllite_lowlevel.db

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a13_sqllite_lowlevel.EditActivity
import com.example.a13_sqllite_lowlevel.R

class MyAdapter (listMain:ArrayList<ListItem>,contextM:Context):RecyclerView.Adapter<MyAdapter.MyHolder>() {//создаем адаптер класса REcyclerView, с типом данных ViewHolder - значит содержит в себе контейнер с View
    var listArray = listMain //создаем переменную с нашим массивом
    var context = contextM//создаем переменную контекст на уровне всего класса, что бы она была доступна всем методам


    class MyHolder(itemView: View,contextV:Context) : RecyclerView.ViewHolder(itemView) { // создадим вложенный класс Холдера. Занимается заполнением каждого элемента шаблона
        val tvTitle:TextView = itemView.findViewById(R.id.tvTitle)//создадим переменную, которая будет указывать на наш textView, который мы нарисуем в Холдере
        val tvTime:TextView = itemView.findViewById(R.id.tvTime)//создадим переменную, которая будет указывать на наш textTime
        var context = contextV
        fun setData(item: ListItem){//создадим метод заполнения элемента
            tvTitle.text = item.title//берем из БД титул
            tvTime.text = item.time//берем из БД время
            itemView.setOnClickListener { //присваиваем слушатель нажатий, при нажатии на сохраненный титульник откроется окно редактирования
            val intent = Intent(context, EditActivity::class.java).apply {
                putExtra(Constants.KEY_TITLE,item.title)//передаем титул
                putExtra(Constants.KEY_DESC,item.desc)//передаем описание
                putExtra(Constants.KEY_URI,item.uri)//передаем URI
                putExtra(Constants.KEY_ID,item.id)//передаем ID
                }
                context.startActivity(intent)//запускаем активити для передачи данных
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {//здесь мы создаем шаблон (загружаем при помощи адаптера)
        val inflater = LayoutInflater.from(parent.context)//создаем inflater кеоторый надувает (создает элемент который будет отображен на экране)
        return MyHolder(inflater.inflate(R.layout.rc_item, parent, false),context) //требуется мередать Холдер с нашим View
        // при надувании Холдера указываем путь к шаблону, контекст (parent), и false (т.к необходимо указать хотим ли бы добавить в рут элемент)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {//этот методы передает уже созданный шаблон в Холдер, и отрисовывает изображение, в указанной позиции position (что бы знать откуда брать элемент). (метод необходим для заполнения шаблона)
        holder.setData(listArray.get(position))//берем холдер и заполняем элементом из списка по указанной позиции
    }

    override fun getItemCount(): Int {//данный метод указывает размер списка который надо передать (сколько элементов будет на экране)
        return  listArray.size //указываем размер массива (укажет на количество элементов)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun UpdateAdapter(listItems:List<ListItem>) { //функция обновления отображения элементов (необходим для постоянного обновления экрана при создании новых элементов)
        //в аргументе указываем новый список которым мы заменим старый для обновления

        listArray.clear()//сперва очищаем существующий список
        listArray.addAll(listItems)// в чистый список вставляем  новый
        notifyDataSetChanged() //обьявляем о новых изменениях (
    }

    fun removeItem(pos:Int,dbManager: MyDBManager) { //функция удаления элемента из списка
        //в аргументе указываем новый список которым мы заменим старый для обновления
        dbManager.removeFromDb(listArray[pos].id.toString())//удаляем из базы данных из списка с указанной позиции по id
        listArray.removeAt(pos)//удаляем элемент с указанной позиции
        notifyItemRangeChanged(0, listArray.size)//указываем адаптеру что размер изменился
        notifyItemRemoved(pos)//указываем адаптеру что был удален элемент
    }
}