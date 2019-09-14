package scottychang.myfirehydrantmap.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import scottychang.myfirehydrantmap.R

class PermissionCheckActivity : AppCompatActivity() {
    private val PERMISSION_ALL = 1
    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission_check)
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        } else {
            MapActivity.go(this, false)
            finish()
        }
    }

    private fun hasPermissions(): Boolean {
        for (permission in PERMISSIONS) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        for (grantResult:Int in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_not_grant, Toast.LENGTH_LONG).show()
                Handler().postDelayed({finish()}, 3000)
                return
            }
        }
        MapActivity.go(this, true)
        finish()
    }
}