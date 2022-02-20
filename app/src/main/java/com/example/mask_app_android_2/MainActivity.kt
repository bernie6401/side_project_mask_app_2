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

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

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

                //藥局名稱變數宣告
                val phName = StringBuilder()

                //第一個欄位是填入原始資料的來源，第二個欄位是把資料轉換成甚麼樣的格式
                val phDataGson = Gson().fromJson(pharmacyData, PharmacyInfo::class.java)

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