package com.example.a13_sqllite_lowlevel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.a13_sqllite_lowlevel.databinding.EditActivityBinding
import com.example.a13_sqllite_lowlevel.db.Constants
import com.example.a13_sqllite_lowlevel.db.MyDBManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    var id = 0
    private var isEditState = false //создапдим переменную для проверки (мы зашли для создания или редактирования записи)
    private var tempImageUri = "empty"

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), ActivityResultCallback { //при помощи OpenDocument() можно открыть документ для чтение изображения, при этом не будет создаваться его копия
            bindClass.imageView.setImageURI(it) //создадим переменную для запуска запроса по выбору картинки из галереи
            tempImageUri = it.toString()//задаем в переменную URI ссылку на картинку (для дальнейшего сохранения в БД)
            contentResolver.takePersistableUriPermission(it,Intent.FLAG_GRANT_READ_URI_PERMISSION) // таким образом мы ставим флаг на предоставление доступа для считывания файлов
        })

    private val MyDBManager = MyDBManager(this)//создаем переменную БД
     //переменная необходимая для защиты от сохранения null ссылок, задает ссылку по умолчанию

    lateinit var bindClass: EditActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        bindClass = EditActivityBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(bindClass.root)

        bindClass.imbEditImage.setOnClickListener{// присвоем кнопке запуск выбора картинки, с указанием типа выбираемого файла (картинка/все)
        selectImageFromGalleryResult.launch(arrayOf("image/*"))//запускаем выбор изображения из галереи передавая массив изображений/весь
        }
        getMyIntents()
    }


    fun onClickSave(view: View) {
        var i = intent
        val myTitle = bindClass.edTitle.text.toString()
        val myDesc = bindClass.edDesc.text.toString()
        if(myTitle!= "" && myDesc!= ""){//если поля не пустые

            CoroutineScope(Dispatchers.Main).launch {    //добавляем корутину (запись будет идти во 2 потоке)
                if(isEditState) //проверка для редактирования или для создания мы зашли в активити
                MyDBManager.updateItem(myTitle, myDesc, tempImageUri, id, getCurrentTime())//если зашли для редактирования, то запись с текущим id будет обновлена новыми значениям
            else
                MyDBManager.insertToDB(myTitle, myDesc, tempImageUri, getCurrentTime())// и мы зашли впервые, то произойдет сохранение в БД
                finish() }
        }
    }

    fun onClickAddImage(view: View) {
        bindClass.mainImageLayout.visibility = View.VISIBLE
        bindClass.fbAddImage.visibility = View.GONE
    }


    fun onClickDeleteImage(view: View) {
        bindClass.mainImageLayout.visibility = View.GONE
        bindClass.fbAddImage.visibility = View.VISIBLE
        tempImageUri = "empty"
    }

    fun getMyIntents(){//создаем функцию приема переданных данных (при редактировании)
        val i = intent
        bindClass.fbEditText.visibility = View.GONE//по умолчанию кнопка редактирования отсутствует (будет видна только если запись редактируется а не создается)

        if(i != null) {
            if (i.getStringExtra(Constants.KEY_TITLE) != null){//проверяем, если ключ не пустой, то
                bindClass.fbAddImage.visibility = View.GONE
                bindClass.edTitle.setText(i.getStringExtra(Constants.KEY_TITLE))// записываем в графу титул данные, переданные по ключу KEY_TITLE
                isEditState = true //если мы заходим в запись повторно (в ней не пустые значения) то ставим значение true
                bindClass.edTitle.isEnabled =false//делаем редактирование недоступным без нажатия кнопки "редактировать"
                bindClass.edDesc.isEnabled =false
                bindClass.fbEditText.visibility = View.VISIBLE
                tempImageUri = i.getStringExtra(Constants.KEY_URI)!! // присваиваю в переменную URI их БД. по которой идет проверка, если не empty то будет загружена картинка
                //так же необходимо выбрать цвет неактивного текста, для этого в папке res создаем папку color ...
                bindClass.edDesc.setText(i.getStringExtra(Constants.KEY_DESC))
                id = i.getIntExtra(Constants.KEY_ID, 0) //передаем значение ID
                if(i.getStringExtra(Constants.KEY_URI)!= "empty") {//если картинка не пустая
                    bindClass.mainImageLayout.visibility = View.VISIBLE//делаем окно изображение видимым
                    bindClass.fbAddImage.visibility = View.GONE//а кнопку добавления картинки невидимой
                    bindClass.imageView.setImageURI(Uri.parse(i.getStringExtra(Constants.KEY_URI)))//в imageView добавляем картинку из БД по uri, используя парсер
                    bindClass.imbDelete.visibility = View.GONE
                    bindClass.imbEditImage.visibility = View.GONE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MyDBManager.openDB()
    }

    override fun onDestroy() {//база данных закрывается при закрытии приложения
        super.onDestroy()
        MyDBManager.closeDB() //закрываем БД
    }

    fun onClickeditText(view: View) {//при нажатии на кнопку разблокируем текст
        bindClass.edTitle.isEnabled = true
        bindClass.edDesc.isEnabled = true
        bindClass.fbEditText.visibility = View.GONE
        bindClass.imbEditImage.visibility = View.VISIBLE
        bindClass.imbDelete.visibility = View.VISIBLE
        if(tempImageUri == "empty") bindClass.fbAddImage.visibility = View.VISIBLE
    }

    private fun getCurrentTime():String{ //функция сохраняющая время создания записи
        val time = Calendar.getInstance().time//используем стандартный класс работы с календарем и временем
        val formated = SimpleDateFormat("dd-MM-yy kk:mm", Locale.getDefault()) //указываем формат даты (можно заменить kk (24 часовой формат) на hh(12 часовой)
        val ftime = formated.format(time)//наше время с учетом заданного формата
        return ftime
    }
}
