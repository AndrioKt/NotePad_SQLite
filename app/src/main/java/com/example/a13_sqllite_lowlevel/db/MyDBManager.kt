package com.example.a13_sqllite_lowlevel.db

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

class MyDBManager (val context:Context) { //создадим класс, содержащий все методы работы с базами данных
    val myDBHelper = MyDBHelper(context)
    var db:SQLiteDatabase? = null

    fun openDB(){
        db = myDBHelper.writableDatabase //указываем что бы наша БД открылась для записи
    }

     suspend fun insertToDB(title:String, content:String, uri:String,time:String) = withContext(Dispatchers.IO){//запись в БД (в аргументах название и data). используется во второстепенном потоке
        val values = ContentValues().apply {
            put(MyDBNameClass.COLUMN_NAME_TITLE, title) //сохраняем в БД титул
            put(MyDBNameClass.COLUMN_NAME_CONTENT, content)//сохраняем в БД контент
            put(MyDBNameClass.COLUMN_NAME_IMAGE_URI, uri)//сохраняем в БД url изображения
            put(MyDBNameClass.COLUMN_NAME_TIME, time)//сохраняем в БД время создания
        }
            db?.insert(MyDBNameClass.TABLE_NAME, null, values) //передаем в БД данные, указывая ее название, null и значения которые мы сохраняем values

     }
    @SuppressLint("Range")
    //для блокировки данного потока добавляем suspend и указываем withContext(Dispatchers.IO) (поток ввода/вывода)
    suspend fun readDBData(searchedText:String):ArrayList<ListItem> = withContext(Dispatchers.IO){ //возвращаем значение из БД. в данном случаем мы возвращаем только title (если необходимо возвращать и титул и контент, то необходимо создавать отдельный класс (ListItem) с хранением обеих переменных и в типах данныхъ указывать его)
        val dataList = ArrayList<ListItem>()
        val selection = "${MyDBNameClass.COLUMN_NAME_TITLE} like ?"//этот запрос обеспечивает поиск по title в соответствии с совпадением (like ?  - это запрос к SQLite означающий "поиск")
        val cursor = db?.query(MyDBNameClass.TABLE_NAME,null,selection, arrayOf("%$searchedText%"),null,null,null) //создаем переменную считывания из БД.
        //указываем название таблицы, и в данном случае 6 null, вместо которых могли бы быть методы (такие как сортировка и тд)
        //поля selection и selectionArgs необходимы для обеспечения поиска, в них передаются: колонка в которой ищем и сам аргумент (слово) по которому ищем
        //"%$searchedText%" - дает возможность поиска всовпадений не целого слова, а по любой букве в слове. Так же когда передана пустота, то отображается все что есть в БД
            while (cursor?.moveToNext()!!) { //пока двигаемся к следующему элементу
                val dataTitle = cursor.getString(cursor.getColumnIndex(MyDBNameClass.COLUMN_NAME_TITLE))//передаем титл
                val dataContent = cursor.getString(cursor.getColumnIndex(MyDBNameClass.COLUMN_NAME_CONTENT))//передаем описание
                val dataUri = cursor.getString(cursor.getColumnIndex(MyDBNameClass.COLUMN_NAME_IMAGE_URI))//передаем uri картинки
                val dataId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))//передаем id
                val time = cursor.getString(cursor.getColumnIndex(MyDBNameClass.COLUMN_NAME_TIME))//передаем времая
                val item = ListItem()//создаем обьект класса хранения информации
                item.title = dataTitle//присваиваем титул
                item.desc = dataContent//присваиваем описание
                item.uri = dataUri//присваиваем uri
                item.id = dataId//присваиваем id
                item.time = time//присваиваем время
                dataList.add(item)//каждый раз при доставании нового элементы мы помещаем его в массив
            }
        cursor.close() //по окончании требуется закрыть курсор
        return@withContext dataList
        }
    fun closeDB(){ //создаем функцию закрытия БД
        myDBHelper.close()
    }

    fun removeFromDb(id:String){//удаление из БД по id (т.к идентификатор уникальный и не будет удаления других случайных элементов)
        val selection = BaseColumns._ID + "=$id" //указываем стандартный класс присвоения id в БД. таким образом мы добавили поиск идентификатора элемента по которому свайпнули среди всего списка id
        db?.delete(MyDBNameClass.TABLE_NAME, selection, null) //удаляем из БД значение. указываем назвине таблицы, а так же id с которого удаляем (selection)
    }

    suspend fun  updateItem(title:String, content:String, uri:String, id:Int, time:String) = withContext(Dispatchers.IO){//функция обновления элемента (при редактировании записи будет обновлять запись, а не создавать новую)
        val selection = BaseColumns._ID + "=$id"//создаем переменную выборки
        val values = ContentValues().apply {
            put(MyDBNameClass.COLUMN_NAME_TITLE, title) //сохраняем в БД титул
            put(MyDBNameClass.COLUMN_NAME_CONTENT, content)//сохраняем в БД контент
            put(MyDBNameClass.COLUMN_NAME_IMAGE_URI, uri)//сохраняем в БД url изображения
            put(MyDBNameClass.COLUMN_NAME_TIME, time)//сохраняем в БД url изображения
        }
        db?.update(MyDBNameClass.TABLE_NAME, values, selection,null) //обновление БД, указывая (имя таблицы, значение на которое обновляем, selection по которому делаем выборку (в данном случае id), null)
    }
    }
