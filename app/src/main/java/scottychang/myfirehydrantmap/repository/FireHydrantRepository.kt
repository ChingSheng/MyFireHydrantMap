package scottychang.cafe_walker.repositiory

import android.content.Context
import com.opencsv.CSVReader
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import scottychang.cafe_walker.model.FireHydrant
import scottychang.myfirehydrantmap.server.CafeNomadApi
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit


class FireHydrantRepository(private val context: Context) {
    private val url = "https://cafenomad.tw/api/v1.2/"
    private val timeoutSecond = 30L
    private val service: CafeNomadApi

    companion object {
        @Volatile private var INSTANCE: FireHydrantRepository? = null

        fun getInstance(context: Context): FireHydrantRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: FireHydrantRepository(context).also { INSTANCE = it }
        }
    }

    init {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logInterceptor)
            .connectTimeout(timeoutSecond, TimeUnit.SECONDS)
            .readTimeout(timeoutSecond, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val retrofit = Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()

        service = retrofit.create(CafeNomadApi::class.java)
    }

    fun loadFireHydrant(iis: InputStream, callback: MyCallback<List<FireHydrant>>?) {
        val result =  ArrayList<FireHydrant>()
        var fileReader: BufferedReader? = null
        var csvReader: CSVReader? = null
        try {
            fileReader = BufferedReader(InputStreamReader(iis))
            csvReader = CSVReader(fileReader)

            var record: Array<String>?
            csvReader.readNext() // skip Header

            record = csvReader.readNext()
            while (record != null) {
                record = csvReader.readNext()
                if (record != null) {
                    val element = FireHydrant(
                        id = record[4],
                        name = record[10] + "/" + record[4],
                        longitude = record[7],
                        latitude = record[8],
                        meta = record[9]
                    )
                    result.add(element)
                }
            }
            csvReader.close()
            callback!!.onSuccess(result)
        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
            callback!!.onFailure(e)
        } finally {
            try {
                fileReader!!.close()
                csvReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader/csvParser Error!")
                e.printStackTrace()
            }
        }
    }

//    fun loadCoffeeShops(city: String, callback: MyCallback<List<FireHydrant>>?) {
//        val file = File(getFilePath(city))
//        if (file.exists() && (createdBelowOneHour(file) || !isNetworkAvailable())) {
//            val reader = JsonReader(FileReader(getFilePath(city)))
//            val type = object : TypeToken<ArrayList<FireHydrant>>() {}.type
//            val data = Gson().fromJson<List<FireHydrant>>(reader, type)
//            callback?.onSuccess(data)
//        } else {
//            val callable = service.getCoffeeShops(city)
//            callable.enqueue(object : Callback<List<FireHydrant>> {
//                override fun onResponse(call: Call<List<FireHydrant>>, response: Response<List<FireHydrant>>) {
//                    cacheByFile(city, response.body())
//                    callback?.onSuccess(response.body() ?: ArrayList())
//                }
//
//                override fun onFailure(call: Call<List<FireHydrant>>, t: Throwable) {
//                    val error = if (t is UnknownHostException) Exception(context.getString(
//                        scottychang.myfirehydrantmap.R.string.network_error)) else t as Exception
//                    callback?.onFailure(error)
//                }
//            })
//        }
//    }
//
//    private fun cacheByFile(city: String, data: List<FireHydrant>?) {
//        val file = File(getFilePath(city))
//        file.createNewFile()
//        file.outputStream().use {
//            val s = Gson().toJson(data)
//            it.write(s.toByteArray())
//            it.close()
//        }
//    }
//
//    private fun createdBelowOneHour(file: File) = ((System.currentTimeMillis() - file.lastModified()) / 1000 / 60) < 60
//
//    private fun isNetworkAvailable(): Boolean {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetworkInfo = connectivityManager.activeNetworkInfo
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected
//    }
//
//    private fun getFilePath(fileName : String): String {
//        return context.getFilesDir().getPath().toString() + "/" + fileName + ".json"
//    }
}