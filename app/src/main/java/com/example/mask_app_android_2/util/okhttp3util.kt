/*package com.example.mask_app_android_2.util
import okhttp3.*
import okio.IOException

/*main purpose:封裝 OkHttp，簡化原本繁雜步驟的程式碼，之後呼叫變得很簡單俐落。
不用每次只要使用到 OkHttp 都要寫一大堆程式碼。造成日後開發或維護上的麻煩。
使用單例模式 (Singleton)，確保 OkHttpClient 只有一個實例存在，減少連線反應延遲與降低記憶體空間，
改善提高整體運行效能。這次只簡單封裝 get 功能，若未來還需要更多 OkHttp 相關功能，
如：需要 post 或是上傳檔案、下載進度狀態...等，可以再進行功能擴充*/

class Okhttp3util
{
    private var varOkHttpClient: OkHttpClient? = null

    /*Kotlin 宣告成單例的方式，因為 Kotlin 沒有 static 修飾詞，
    所以需採用 「Companion Object」 關鍵字來修飾，
    如此即可以達到靜態的宣告方式，來確保這個類別只會存在一個實體的物件*/
    companion object
    {
        /*另外在透過 lazy 加載方式，並將模式設定為 「LazyThreadSafetyMode.SYNCHRONIZED」 ，
        鎖定只讓一條執行緒 (thread) 可以去初始化 lazy 屬性。所以在多執行緒的情況下，
        一但沒有初始化完成，其他執行緒將無法訪問使用*/
        val OkHttpUtil_var: Okhttp3util by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED)
        {
            Okhttp3util()
        }
    }

    //我的理解是OkHttpUtil_var這個變數就代表以下fun getAsync

    init
    {
        //Part 1: 宣告 OkHttpClient
        varOkHttpClient = OkHttpClient().newBuilder().build()
    }

    //Get 非同步
    fun getAsync(url: String, callback: ICallback)
    {
        //Part 2: 宣告 Request，要求要連到指定網址
        val request = with(Request.Builder())
        {
            url(url)
            get()
            build()
        }

        //Part 3: 宣告 Call
        val call = varOkHttpClient?.newCall(request)

        //執行 Call 連線後，採用 enqueue 非同步方式，獲取到回應的結果資料
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException)
            {
                callback.onFailure(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response)
            {
                callback.onResponse(response)
            }
        })


    }


    interface ICallback
    {
        fun onResponse(response: Response)

        fun onFailure(e: IOException)
    }
}

/*companion 和 ICallback是同一個層級，在MainActivity.kt中可以透過okhttp3util.***來呼叫
而fun getAsync則是只能利用companion中的OkHttpUtil_var這個變數來呼叫*/*/

package com.example.mask_app_android_2.util

import okhttp3.*
import okio.IOException


class OkHttpUtil {
    private var mOkHttpClient: OkHttpClient? = null

    companion object {
        val mOkHttpUtil: OkHttpUtil by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            OkHttpUtil()
        }
    }

    init {
        //Part 1: 宣告 OkHttpClient
        mOkHttpClient = OkHttpClient().newBuilder().build()
    }

    //Get 非同步
    fun getAsync(url: String, callback: ICallback) {
        //Part 2: 宣告 Request，要求要連到指定網址
        val request = with(Request.Builder()) {
            url(url)
            get()
            build()
        }

        //Part 3: 宣告 Call
        val call = mOkHttpClient?.newCall(request)

        //執行 Call 連線後，採用 enqueue 非同步方式，獲取到回應的結果資料
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                callback.onResponse(response)
            }
        })


    }


    interface ICallback {
        fun onResponse(response: Response)

        fun onFailure(e: IOException)
    }
}