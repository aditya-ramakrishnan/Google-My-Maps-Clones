package com.example.mymaps

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony.Mms.Part.FILENAME
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymaps.models.Place
import com.example.mymaps.models.UserMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
private const val TAG = "MainActivity"
private const val REQUEST_CODE = 1234
private const val FILENAME = "UserMaps.data"
class MainActivity : AppCompatActivity() {

    private lateinit var  userMaps: MutableList<UserMap>
    private lateinit var  mapAdapter: MapsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userMaps = deserializeUserMaps(this).toMutableList()
        rvMaps.layoutManager = LinearLayoutManager(this)
        mapAdapter = MapsAdapter(this, userMaps, object: MapsAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                Log.i(TAG, "onItemClick $position")

                val intent = Intent(this@MainActivity, DisplayMapActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, userMaps[position])
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })
        rvMaps.adapter = mapAdapter

        fabCreateMap.setOnClickListener {
            Log.i(TAG, "Tap on FAB")
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_map,null)
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Map title")
                .setView(mapFormView)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("OK", null)
                .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = mapFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            if (title.trim().isEmpty()) {
                Toast.makeText(this,"Map must have a valid title", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity, CreateMapActivity::class.java)
            intent.putExtra(EXTRA_MAP_TITLE, title)
            startActivityForResult(intent, REQUEST_CODE)
            dialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val userMap = data?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            Log.i(TAG, "onActivityResult with new map title ${userMap.title}")
            userMaps.add(userMap)
            mapAdapter.notifyItemInserted(userMaps.size-1)
            serializeUserMaps(this, userMaps)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun serializeUserMaps(context: Context, userMaps: List<UserMap>) {
        Log.i(TAG, "serializeUserMaps")
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use {it.writeObject(userMaps)}
    }

    private fun deserializeUserMaps(context: Context) : List<UserMap> {
        Log.i(TAG, "deserializeUserMaps")
        val dataFile = getDataFile(context)
        if (!dataFile.exists()) {
            Log.i(TAG, "Data file doesn't exist yet")
            return emptyList()
        }
        ObjectInputStream(FileInputStream(dataFile)).use {return it.readObject() as List<UserMap>}
    }

    private fun getDataFile(context : Context) : File {
        Log.i(TAG, "Getting file from directory ${context.filesDir}")
        return File(context.filesDir, FILENAME)
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Saratoga Restaurants",
                listOf(
                    Place("Sushi Heaven", "Favorite Japanese Restaurant", 37.29325103839423, -122.03177281534934),
                    Place("Mint Leaf Cuisine", "Favorite Thai Restaurant", 37.258810102772756, -122.0326061306926),
                    Place("Hong's Gourmet", "Favorite Chinese Restaurant", 37.25690874378931, -122.03441318651448)
                )
            ),
            UserMap("Saratoga High School",
                listOf(
                    Place("Music Building", "Had many rehearsals here", 37.266895689789266, -122.03043021534987),
                    Place("Football Field", "Had many Marching Band rehearsals here", 37.26659228424254, -122.02736081534991),
                    Place("McAfee", "Had many performances here", 37.26700212123412, -122.0314163153431)
                )),
            UserMap("Boba Tea Spots",
                listOf(
                    Place("TeaTop", "Best Boba place", 37.30869811392738, -122.01268073069177),
                    Place("Teaspoons", "Pretty decent", 37.3154968315623, -121.97815191719852),
                    Place("85C", "Backup option", 37.324074615477585, -122.01116837486973)
                )
            ),
            UserMap("Favorite Restaurants",
                listOf(
                    Place("Sweet Tomatoes", "Large selection of soups, salads, pizza, etc.", 37.34092975647402, -121.90879227301983),
                    Place("Boston Chowder", "Tasty comfort food", 37.22158606112165, -121.98080085952907),
                    Place("Chaat Bhavan", "Excellent North Indian food", 37.35201722372561, -122.00950741719798),
                    Place("Red Robin", "Great Burgers and Fries", 37.29010259953959, -121.99059287671982),
                    Place("Koja Kitchen", "Korean-American fusion", 37.32548234464136, -122.01242364864073)
                )
            )
        )
    }
}
