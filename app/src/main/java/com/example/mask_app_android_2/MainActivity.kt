/*以下這一行的作用是指出這個檔案所在的命名空間，package(套件)是關鍵字,
如果我們寫了一個java檔案,其他的檔案要引用到他的class或class內的方法,就需要*/
package com.example.mask_app_android_2


/*以下為預設library*/
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

//okhttp library
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.*
import com.example.mask_app_android_2.databinding.ActivityMainBinding

//json library
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder

//Gson Library
import com.google.gson.Gson
import com.example.mask_app_android_2.data.PharmacyInfo
import java.io.IOException

//ProgressBar 忙碌圈圈
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView


class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding

    /*Bundle型別正是我們前面所導入的package之一，Bundle的內容與Android手機平臺的記憶體管理有關，
    Bundle類別可以保存Activity上一次關閉(stop)時的狀態，我們可以透過覆載onStop方法來保存關閉前的狀態，
    當程式啟動時，會再次呼叫onCreate方法，就能從savedInstanceState中得到前一次凍結的狀態。
    我們也可以透過Bundle來將這個Activity的內容傳到下一個Activity中。*/
    override fun onCreate(savedInstanceState: Bundle?)
    {
        /*意思是執行AppCompatActivity類別中onCreate方法的內容，
        因為我們已經覆載(@Override)了MainActivity類別的onCreate方法，
        因此如果我們想將原本的onCreate方法內容保留，再加上我們自己的內容，
        就要使用super語句，並傳入savedInstanceState參數*/
        super.onCreate(savedInstanceState)

        /*setContentView就是螢幕顯示的畫面，是透過各種介面元件的排列配置結構來描述的。
        要將一套版面配置的層次結構轉換到一個螢幕上時，Activity會呼叫它用來設定螢幕顯示內容的setContentView方法，
        並傳入定義了版面配置的Xml描述檔。當Activity被啟動並需要顯示到螢幕上時，
        系統會通知Activity並根據引用的Xml檔來描繪出使用者介面。*/
        //setContentView(R.layout.activity_main)    //因為新版kotlin修改掉synthetic所以不需要這行，由下面兩行取代
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPharmacyData()
    }

    private fun getPharmacyData()
    {
        //顯示忙碌圈圈
        binding.progressBar.visibility = View.VISIBLE

        //口罩資料網址
        val pharmaciesDataUrl =
            "https://raw.githubusercontent.com/thishkt/pharmacies/master/data/info.json"

        //Part 1: 宣告 OkHttpClient
        val okHttpClient = OkHttpClient().newBuilder().build()

        //Part 2: 宣告 Request，要求要連到指定網址
        val request: Request = Request.Builder().url(pharmaciesDataUrl).get().build()

        //Part 3: 宣告 Call
        val call = okHttpClient.newCall(request)

        //執行 Call 連線後，採用 enqueue 非同步方式，獲取到回應的結果資料
        /*enqueue就是禁止以下程式在主執行緒(main thread)下運行，原因是這樣耗時的工作應該另外開一條執行緒才不會卡住*/
        call.enqueue(object : Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                Log.d("SBK", "onFailure: $e")

                //關閉忙碌圈圈
                runOnUiThread{binding.progressBar.visibility = View.GONE}
            }


            //以下部分是我們跟url做request後得到的response
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response)
            {
                /*response.body?.string()就是我們從url的server取得的資料，切記不可重複拿兩次以上，
                會報error，另外直接print response是沒有資料的，必須透過body?.string的轉換才可以*/
                val pharmacyData = response.body?.string()

                //將 pharmaciesData 整包字串資料，轉成 JSONObject 格式
                val phDataJson = JSONObject(pharmacyData)   //此時phDataJson是org.json.JSONObject格式

                //這個時候，我們就可以透過 getString 的方式，裡面放 key (name) 值，
                //即可以獲取到最外層的 type 欄位資料值。
                /*android.util.Log常用的方法有以下5個：Log.v() Log.d() Log.i() Log.w() 和Log.e()。
                根據首字母對應VERBOSE，DEBUG , INFO , WARN，ERROR。
                1、Log.v 為 黑色 的，任何消息都會輸出，這裡的v代表verbose囉嗦的意思，平時使用就是Log.v("","")。
                2、Log.d 為 藍色 的，僅輸出debug調試的意思，但他會輸出上層的信息，過濾起來可以通過DDMS的Logcat標籤來選擇。
                3、Log.i 為 綠色 的，一般提示性的消息information，它不會輸出Log.v和Log.d的信息，但會顯示i、w和e的信息。
                4、Log.w 為 橙色 的，可以看作為warning警告，一般需要我們注意優化Android代碼，同時選擇它後還會輸出Log.e的信息。
                5、Log.e 為 紅色 的，可以想到error錯誤，這裡僅顯示紅色的錯誤信息，這些錯誤就需要我們認真的分析，查看棧的信息了。*/
                //Log.d("SBK", phDataJson.getString("type"))

                //features 是一個陣列 [] ，需要將他轉換成 JSONArray
                //val phDataArray = JSONArray(phDataJson.getString("features")) //此時phDataJson是org.json.JSONObject格式

                //藥局名稱變數宣告
                val phName = StringBuilder()

                //透過 for 迴圈，即可以取出所有的藥局名稱
                //利用json的格式取出資料
                /*for (i in 0 until phDataArray.length())
                {
                    val properties = phDataArray.getJSONObject(i).getString("properties")   //此時properties是java.lang.String
                    val property = JSONObject(properties)   //org.json.JSONObject
                    //將每次獲取到的藥局名稱，多加跳行符號，存到變數中
                    phName.append(property.getString("name") + "\n")
                }*/

                /*如果要用json的格式做運算，就還要自己寫很多try&catch，例如以下寫法*/
                /*try
                {
                    if(!phDataJson.isNull("type"))
                    {Log.d("SBK", "type: ${phDataJson.getString("type")}")}
                    else
                    {Log.d("SBK", "no value")}
                    Log.d("SBK", "type: ${phDataJson.getString("type")}")
                }
                catch (e: JSONException)
                {Log.e("SBK", "JSONException: $e")}
                catch (e: Exception)
                {Log.e("SBK", "Exception: $e")}*/

                //第一個欄位是填入原始資料的來源，第二個欄位是把資料轉換成甚麼樣的格式
                val phDataGson = Gson().fromJson(pharmacyData, PharmacyInfo::class.java)
                //從以下程式可以知道Gson比較有效率也比較簡單，遇到沒有的key也不會閃退
                //Log.d("SBK", "type: ${phDataGson.type}")    //這樣就可以直接拿到FeatureCollection
                //for (i in phDataGson.features)
                //    Log.d("SBK", "name: ${i.properties.name}")


                for (i in phDataGson.features) {
                    phName.append(i.properties.name + "\n")
                }

                /*要加上runOnUiThread是因為取得資料是在背景的環境下，而顯示要顯示在主要畫面，
                而處理主要畫面就是main thread的事情，因此必須要加上這個code做資料的轉換，才能顯示在畫面上*/
                runOnUiThread{
                    binding.tvPharmacyData.text = phName

                    //關閉忙碌圈圈
                    binding.progressBar.visibility = View.GONE
                }
            }

        })

    }
}