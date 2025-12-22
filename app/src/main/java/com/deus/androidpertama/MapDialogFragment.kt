package com.deus.androidpertama

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

class MapDialogFragment : DialogFragment() {
    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var hasLocationPermission: Boolean = false
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog)
        
        // Inisialisasi Fused Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        
        // Load konfigurasi OSMdroid
        val context = requireContext().applicationContext
        Configuration.getInstance().load(context, context.getSharedPreferences("osm_prefs", android.content.Context.MODE_PRIVATE))
        Configuration.getInstance().setUserAgentValue("AplikasAbsen")
        
        // Cek permission dan userId dari arguments
        hasLocationPermission = arguments?.getBoolean("hasLocationPermission", false) ?: false
        userId = arguments?.getInt("userId", 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_map, container, false)
        
        map = view.findViewById(R.id.dialog_map_view)
        val closeButton = view.findViewById<Button>(R.id.buttonClose)
        
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        
        // Inisialisasi Overlay Lokasi
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        map.overlays.add(myLocationOverlay)
        
        // Setup map berdasarkan permission
        setupMapAndLocation()
        
        closeButton.setOnClickListener {
            dismiss()
        }
        
        return view
    }
    
    private fun setupMapAndLocation() {
        if (hasLocationPermission && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Izin ada: Aktifkan overlay dan coba pusatkan peta
            myLocationOverlay.enableFollowLocation()
            myLocationOverlay.enableMyLocation()
            centerMapOnUserLocation()
        } else {
            // Izin belum ada atau ditolak: Tetapkan lokasi default Bandung
            val defaultPoint = GeoPoint(-6.9034, 107.6175) // Bandung
            map.controller.setCenter(defaultPoint)
            map.controller.setZoom(14.0)
        }
    }
    
    private fun centerMapOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(requireContext(), "Izin Lokasi Belum Diberikan", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Ambil Lokasi Terakhir
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userGeoPoint = GeoPoint(location.latitude, location.longitude)
                map.controller.animateTo(userGeoPoint)
                map.controller.setZoom(16.0)
                map.invalidate()
                
                // 3. Simpan ke Database menggunakan viewLifecycleOwner.lifecycleScope
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val db = AbsensiDatabase.getInstance(requireContext())
                        val attendance = AttendanceEntity(
                            user_id = userId,
                            check_in_time = System.currentTimeMillis(),
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        db.attendanceDao().insertAttendance(attendance)
                        
                        // Tampilkan toast di main thread
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Absen berhasil disimpan", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Error menyimpan absen: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Gagal Mendapatkan Lokasi.", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Atur ukuran dialog agar lebih besar dan responsif
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        // Gunakan WRAP_CONTENT agar dialog menyesuaikan konten
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    
    override fun onResume() {
        super.onResume()
        map.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        map.onPause()
    }
    
    companion object {
        fun newInstance(hasLocationPermission: Boolean, userId: Int): MapDialogFragment {
            return MapDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("hasLocationPermission", hasLocationPermission)
                    putInt("userId", userId)
                }
            }
        }
    }
}

