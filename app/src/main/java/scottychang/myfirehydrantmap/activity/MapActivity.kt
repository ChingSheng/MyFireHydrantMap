package scottychang.myfirehydrantmap.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import scottychang.cafe_walker.adapter.FireHydrantSimpleListAdapter
import scottychang.cafe_walker.data.ZoneString
import scottychang.cafe_walker.model.FireHydrantClusterItem
import scottychang.cafe_walker.model.LatLng
import scottychang.cafe_walker.model.TwZone
import scottychang.cafe_walker.viewmodel.FireHydrantViewModel
import scottychang.cafe_walker.viewmodel.PositioningViewModel
import scottychang.myfirehydrantmap.R

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private val MAX_ZOOM_IN_LEVEL = 20.0f
    private val MIN_ZOOM_IN_LEVEL = 11.0f
    private val DEFAULT_ZOOM_IN_LEVEL = 16.5f

    private val container: FrameLayout by bindView(R.id.container)
    private val recyclerView: RecyclerView by bindView(R.id.recycler_view)
    private val bottomSheet: ConstraintLayout by bindView(R.id.items)
    private val bottomSheetTitleItem: FrameLayout by bindView(R.id.title_item)
    private val bottomSheetTitle: TextView by bindView(R.id.city)
    private val bottomSheetIndicator: ImageView by bindView(R.id.indicator)
    private val floatingButton: FloatingActionButton by bindView(R.id.floating_button)
    private val loading: FrameLayout by bindView(R.id.loading)

    private lateinit var mapFragment: MapFragment
    private lateinit var fireHydrantViewModel: FireHydrantViewModel
    private lateinit var positioningViewModel: PositioningViewModel

    private lateinit var map: GoogleMap
    private lateinit var clusterManager: ClusterManager<FireHydrantClusterItem>

    companion object {
        private const val SHOW_INTRODUCTION_DIALOG = "show_dialog"
        fun go(context: Context, showIntroductionDialog: Boolean) {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra(SHOW_INTRODUCTION_DIALOG, showIntroductionDialog)
            context.startActivity(intent)
        }
    }

    fun <T : View> bindView(@IdRes resId: Int): Lazy<T> = lazy { findViewById<T>(resId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        showIntroductionDialogIfNeeded()

        mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    private fun showIntroductionDialogIfNeeded() {
        val shouldShowDialog = intent?.getBooleanExtra(SHOW_INTRODUCTION_DIALOG, false)
        shouldShowDialog?.let { showDialog ->
            if (showDialog) {
                val builder = AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog)
                builder.setMessage(R.string.first_launch_msg)
                builder.setPositiveButton(R.string.first_launch_button_text, null)
                builder.create().show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap!!
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isMapToolbarEnabled = false
        map.setMaxZoomPreference(MAX_ZOOM_IN_LEVEL)
        map.setMinZoomPreference(MIN_ZOOM_IN_LEVEL)

        clusterManager = ClusterManager(this, map)
        clusterManager.setOnClusterClickListener { zoomInFromCluster(it) }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)

        initPositioning()
        initFireHydrant()
        initFloatingButton()
    }

    private fun zoomInFromCluster(it: Cluster<FireHydrantClusterItem>): Boolean {
        val cameraPosition =
            CameraPosition.builder().target(it.position).zoom(map.cameraPosition.zoom + 2).build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        return true
    }

    private fun initPositioning() {
        positioningViewModel = ViewModelProviders.of(this).get(PositioningViewModel::class.java)
        positioningViewModel.latLng.observe(this, Observer<LatLng> { latLng -> setCenter(latLng) })
    }

    private fun setCenter(position: LatLng?) {
        val s: com.google.android.gms.maps.model.LatLng =
            com.google.android.gms.maps.model.LatLng(position!!.latitude, position!!.longitude)
        val cameraPosition = CameraPosition.builder().target(s).zoom(DEFAULT_ZOOM_IN_LEVEL).build()
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun initFireHydrant() {
        fireHydrantViewModel = ViewModelProviders.of(this).get(FireHydrantViewModel::class.java)
        fireHydrantViewModel.fireHydrantShops.observe(this, Observer { setupViewAndData() })
        fireHydrantViewModel.exceptions.observe(
            this,
            Observer {
                Toast.makeText(this, it?.message ?: "UnknownError", Toast.LENGTH_LONG).show()
            })
        fireHydrantViewModel.loading.observe(
            this,
            Observer { isLoading ->
                loading.visibility = if (isLoading!!) View.VISIBLE else View.GONE
            })
    }

    private fun setupViewAndData() {
        setupMapViewCluster()
        setupBottomSheetTitle()
        setupBottomSheetData()
    }

    private fun setupMapViewCluster() {
        map.clear()
        clusterManager.clearItems()
        clusterManager.addItems(fireHydrantViewModel.fireHydrantShops.value!!.map {
            FireHydrantClusterItem(
                it
            )
        })
        clusterManager.cluster()
    }

    private fun setupBottomSheetTitle() {
        BottomSheetBehavior.from(bottomSheet).setBottomSheetCallback(bottomSheetBehaviorCallback)
        bottomSheetTitleItem.setOnClickListener { toggleBottomSheetBehaviorState() }
        bottomSheetTitle.text = getString(ZoneString.data[fireHydrantViewModel.currentCity]!!)
    }

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val upShiftRatio = 0.7f // BottomSheet shift 1 unit, map shift 0.7 unit
            val distance =
                slideOffset * bottomSheet.resources.getDimensionPixelSize(R.dimen.item_height)
            if (distance > 0) {
                container.translationY = -distance * resources.displayMetrics.density * upShiftRatio
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheetIndicator.setImageResource(R.drawable.down)
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomSheetIndicator.setImageResource(R.drawable.up)
                }
            }
        }
    }

    private fun toggleBottomSheetBehaviorState() {
        bottomSheet.let {
            val bottomSheetBehavior = BottomSheetBehavior.from(it)
            bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
                BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
                else -> bottomSheetBehavior.state
            }
        }
    }

    private fun setupBottomSheetData() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FireHydrantSimpleListAdapter(
            fireHydrantViewModel.getDistancePairFromPosition(positioningViewModel.latLng.value!!),
            { id: String? -> id?.let {} },
            { id: String? ->
                id?.let {
                    val fireHydrant = fireHydrantViewModel.current[it]!!
                    setCenter(
                        LatLng(
                            fireHydrant.latitude?.toDouble() ?: .0,
                            fireHydrant.longitude?.toDouble() ?: .0
                        )
                    )
                }
            }
        )
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun initFloatingButton() {
        floatingButton.setOnClickListener {
            positioningViewModel.reloadPosition()
            updateViewData()
        }
        floatingButton.setOnLongClickListener { view ->
            createPopupMenu(view)
        }
    }

    private fun updateViewData() {
        val fireHydrantPair =
            fireHydrantViewModel.getDistancePairFromPosition(positioningViewModel.latLng.value!!)
        recyclerView.adapter?.let {
            (it as FireHydrantSimpleListAdapter).updateData(fireHydrantPair)
        }
    }

    //================================================================================
    // Menu
    //================================================================================

    private fun createPopupMenu(view: View?): Boolean {
        val popupMenu = PopupMenu(this, view!!)
        popupMenu.menuInflater.inflate(R.menu.map_menu, popupMenu.menu)
        popupMenu.menu.findItem(R.id.city_taipei).subMenu.clearHeader()
        popupMenu.menu.findItem(R.id.city_new_taipei).subMenu.clearHeader()
        popupMenu.setOnMenuItemClickListener { item -> this.onMenuItemSelected(item!!) }
        popupMenu.show()
        return true
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shang_chun -> TwZone.SHANG_CHUN
            R.id.chung_ho -> TwZone.CHUNG_HO
            R.id.chung_shan -> TwZone.CHUNG_SHAN
            R.id.chung_zheng -> TwZone.CHUNG_ZHENG
            R.id.shin_yi -> TwZone.SHIN_YI
            R.id.nay_hu -> TwZone.NAY_HU
            R.id.bei_tou -> TwZone.BEI_TOU
            R.id.nan_gang -> TwZone.NAN_GANG
            R.id.shih_lin -> TwZone.SHIH_LIN
            R.id.da_tong -> TwZone.DA_TONG
            R.id.da_an -> TwZone.DA_AN
            R.id.wen_shan -> TwZone.WEN_SHAN
            R.id.shin_dien -> TwZone.SHIN_DIEN
            R.id.sung_shan -> TwZone.SUNG_SHAN
            R.id.yong_ho -> TwZone.YONG_HO
            R.id.shi_zi -> TwZone.SHI_ZI
            R.id.wan_hua -> TwZone.WAN_HUA
            else -> null
        }?.let { zone: TwZone ->
            fireHydrantViewModel.setFireHydrant(zone)
        }
        return true
    }
}
