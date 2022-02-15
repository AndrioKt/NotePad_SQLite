package com.example.a13_sqllite_lowlevel.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
//создаем класс работы с базой данных, где мы наследуемся от класса SQLiteOpenHelper, с указанием констекста, названия БД, версии и тд
class MyDBHelper (context: Context) : SQLiteOpenHelper(context,MyDBNameClass.DATABASE_NAME , null , MyDBNameClass.DATABASE_VERSION) { //при запуске класса будет создаваться БД
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(MyDBNameClass.CREATE_TABLE) //здесь мы создаем нашу БД, при этом указываем созданную переменную, куда сохранили строку с параметрами создания таблицы
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) { //при обновлении таблицы требуется ее удалить и далее заного создать с указанием другой версии
        db?.execSQL(MyDBNameClass.SQL_DELETE_TABLE)
        onCreate(db)
    }

}