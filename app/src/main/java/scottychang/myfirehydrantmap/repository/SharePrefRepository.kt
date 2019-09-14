package scottychang.cafe_walker.repositiory

import android.content.Context
import scottychang.cafe_walker.model.TwZone

class SharePrefRepository {
    private val PREFERENCE_NAME = "SharePrefRepository"
    private val KEY_ZONE = "zone"

    companion object {
        @Volatile private var INSTANCE: SharePrefRepository? = null

        fun getInstance(): SharePrefRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: SharePrefRepository().also { INSTANCE = it }
        }
    }

    fun saveCity(context: Context, twZone: TwZone) {
        val editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
        editor.putInt(KEY_ZONE, twZone.ordinal)
        editor.apply()
    }

    fun loadZone(context: Context): TwZone {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val index = sharedPreferences.getInt(KEY_ZONE, -1)
        return if (index >= 0) TwZone.values()[index] else TwZone.UNKNOWN
    }
}