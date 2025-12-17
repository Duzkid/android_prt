package com.deus.androidpertama

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class FragmentOsmMap : Fragment() {
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    
    // Gunakan constant yang lebih mudah diakses
    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    
    // Perbaikan: Konfigurasi OSMdroid harus dipanggil di konteks yang memiliki akses ke penyimpanan
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // ... (Kode parameter tetap) ...
        }
        
        // 1. Inisialisasi Fused Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        // 2. Load konfigurasi OSMdroid di onCreate
        // Penting: Gunakan Context aplikasi untuk konfigurasi
        val context = requireContext().applicationContext
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE))
        Configuration.getInstance().setUserAgentValue("AplikasAbsen")
        
        // 3. Cek dan Minta Izin Lokasi
        requestPermissionsIfNecessary()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_osm_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        map = view.findViewById(R.id.osm_map_view)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        
        // 1. Inisialisasi Overlay Lokasi
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        
        // 2. Pindahkan inisialisasi overlay ke sini
        map.overlays.add(myLocationOverlay)
        
        // Panggil fungsi untuk mengaktifkan lokasi dan memusatkan peta
        setupMapAndLocation()
    }
    
    // --- Metode Penanganan Izin ---
    private fun requestPermissionsIfNecessary() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE // Izin storage OSMdroid untuk cache
        )
        
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }
    
    // --- Metode Utama Setup Lokasi ---
    private fun setupMapAndLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Izin ada: Aktifkan overlay dan coba pusatkan peta
            myLocationOverlay.enableFollowLocation()
            myLocationOverlay.enableMyLocation()
            centerMapOnUserLocation()
        } else {
            // Izin belum ada atau belum diberikan, akan diminta di onCreate
            // Tetapkan lokasi default jika tidak ada izin
            val defaultPoint = GeoPoint(-6.9034, 107.6175) // Contoh: Bandung
            map.controller.setCenter(defaultPoint)
            map.controller.setZoom(14.0)
        }
    }
    
    // --- Penanganan Hasil Izin ---
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin Lokasi diberikan, coba setup peta lagi
                setupMapAndLocation()
            }
        }
    }
    
    private fun centerMapOnUserLocation() {
        // Mendapatkan lokasi terakhir yang diketahui
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Harus dicek lagi meski sudah dicek di setupMapAndLocation
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userGeoPoint = GeoPoint(it.latitude, it.longitude)
                // Pindahkan kamera ke lokasi pengguna saat ini
                map.controller.animateTo(userGeoPoint)
                map.controller.setZoom(16.0)
                map.invalidate() // Refresh peta
            }
        }
    }
    
    // --- Lifecycle untuk OSMdroid ---
    override fun onResume() {
        super.onResume()
        map.onResume()
        // myLocationOverlay.enableMyLocation() // Aktifkan lagi jika dimatikan di onPause
        setupMapAndLocation() // Pastikan lokasi ter-update
    }
    
    override fun onPause() {
        super.onPause()
        map.onPause()
        // myLocationOverlay.disableMyLocation() // Matikan untuk menghemat baterai
    }
    
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentOsmMap.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentOsmMap().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}