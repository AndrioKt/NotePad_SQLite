package com.example.a13_sqllite_lowlevel.db

import android.provider.BaseColumns

//здесбь будут храниться все наименования связанные с БД
object MyDBNameClass :BaseColumns {
    const val TABLE_NAME = "my_table"
    const val COLUMN_NAME_TITLE = "title"
    const val  COLUMN_NAME_CONTENT = "content"
    const val  COLUMN_NAME_IMAGE_URI = "uri"
    const val  COLUMN_NAME_TIME = "time"

    const val  DATABASE_VERSION = 2
    const val  DATABASE_NAME = "NotePad.db"

    //такой способю обновления нельзя использовать когда приложение уже в проде и используется пользователями
    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +      // задаем переменную создания таблиц
    "${BaseColumns._ID} INTEGER PRIMARY KEY, ${COLUMN_NAME_TITLE} TEXT, $COLUMN_NAME_CONTENT TEXT, $COLUMN_NAME_IMAGE_URI TEXT, $COLUMN_NAME_TIME TEXT)"//сперва указывается название таблицы, а далее в скобках указываются все столбцы
    //таблицы и тип данных, который они будут содержать

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME" //создаем переменную для удаления таблицы (удалениме необходжимо при обновлении таблицы)

}