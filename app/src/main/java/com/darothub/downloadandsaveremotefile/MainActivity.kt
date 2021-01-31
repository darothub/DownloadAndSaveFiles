package com.darothub.downloadandsaveremotefile

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.DownloadListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.darothub.downloadandsaveremotefile.adapter.CarListView
import com.darothub.downloadandsaveremotefile.adapter.carListView
import com.darothub.downloadandsaveremotefile.databinding.ActivityMainBinding
import com.darothub.downloadandsaveremotefile.model.CarOwners
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val listOfCarOwners by lazy {
        arrayListOf<CarOwners>()
    }

    lateinit var fileCreated: File
    lateinit var fileInputStream:FileInputStream
    lateinit var onComplete:BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val URL =
            "https://drive.google.com/u/0/uc?id=1giBv3pK6qbOPo0Y02H-wjT9ULPksfBCm&export=download"
        when (val perm = hasPermissions(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )) {
            is String -> {
                Toast.makeText(this, "$perm permissions not granted", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(perm), 100
                )
            }
            is Boolean -> {
                Toast.makeText(this, "permissions are granted", Toast.LENGTH_SHORT).show()
                if (isExternalStorageReadable()) {
                    val f = "${getExternalFilesDir("car_owners")?.absolutePath}/car_owners.csv"
                    fileCreated = File(f)
                    Log.d("FILEEXISTS", "${fileCreated.exists()}")
                    if (fileCreated.exists()) {
                        try {
                            fileInputStream = FileInputStream(fileCreated)
                            readfile(fileInputStream)
                            inflateRecyclerView()

                        } catch (e: IOException) {
                            Log.d("IOException", "${e.localizedMessage}")
                        }

                    } else {

                        val request = DownloadManager.Request(Uri.parse(URL))
                        request.setDescription("Car owners file")
                        request.setTitle("car_owners.csv")
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
                        request.setDestinationInExternalFilesDir(
                            this@MainActivity,
                            "/car_owners",
                            "car_owners.csv"
                        )

                        val manager1 = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                        val id = Objects.requireNonNull(manager1).enqueue(request)



                    }


                }

            }

        }


    }



    override fun onResume() {
        super.onResume()

        onComplete = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                fileInputStream = FileInputStream(fileCreated)

                readfile(fileInputStream)
                inflateRecyclerView()
                Log.d("DOWNLOAD", "Download is complete")
            }

        }
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))


    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(onComplete)
    }


    private fun readfile(f: FileInputStream) {
        Log.d("File", "$f")

        val instrmreader = InputStreamReader(f)
        val buff = BufferedReader(instrmreader)
        val line: List<String?>? = buff.readLines()
        if (line != null) {

            for (i in line) {
                val s = i?.split(",")
                val owner = CarOwners(
                    s?.get(0),
                    s?.get(1),
                    s?.get(2),
                    s?.get(3),
                    s?.get(4),
                    s?.get(5),
                    s?.get(6),
                    s?.get(7),
                    s?.get(8),
                    s?.get(9)
                )
                listOfCarOwners.add(owner)
                Log.d("Folder", "${s?.get(0)}\n")
            }
        }
        f.close()
        Log.d("Folder", "list of owners ${listOfCarOwners[1]}")
        listOfCarOwners.removeAt(0)
        binding.progressBar.visibility = View.GONE
        binding.loadingInfoTv.visibility = View.GONE

    }

    private fun inflateRecyclerView() {
        binding.epoxyRcv.visibility = View.VISIBLE
        binding.epoxyRcv.withModels {

            listOfCarOwners.forEach { owner ->
                carListView {
                    id(owner.id)
                    data(owner)
                }
            }
        }
    }


    private fun isExternalStorageReadable(): Boolean {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState())
        ) {
            Log.d("Folder", "External is readable")
            return true
        } else {
            Log.d("Folder", "External is not readable")
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults.all {
                                it == PackageManager.PERMISSION_GRANTED
                            }
                            )
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(this, "Permission is just granted", Toast.LENGTH_SHORT).show()
                } else {
                    val alertBuilder = AlertDialog.Builder(this)
                    alertBuilder.setTitle(getString(R.string.storage))
                    alertBuilder.setMessage(R.string.allow_storage)
                    alertBuilder.setPositiveButton(getString(android.R.string.ok)) { dialog, which ->
                        val settingIntent = Intent()
                        settingIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", this.packageName, null)
                        settingIntent.data = uri
                        startActivity(settingIntent)
                        return@setPositiveButton
                    }
                    alertBuilder.setNegativeButton(getString(android.R.string.cancel)) { dialog, which ->
                        Toast.makeText(
                            this,
                            "You will not be able to download and save files",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val alertDialog = alertBuilder.create()
                    alertDialog.setOnShowListener {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.black
                            )
                        )
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.purple_200
                            )
                        )
                    }
                    alertDialog.show()
                    Toast.makeText(this, "Permission is not granted", Toast.LENGTH_SHORT).show()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String?): Any {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return permission
                }
            }
        }
        return true
    }

}



//        when {
//            ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                // You can use the API that requires the permission.
//                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
//            }
//            ActivityCompat.shouldShowRequestPermissionRationale(
//                    this,
//                    android.Manifest.permission.CAMERA
//            ) -> {
//
//            }
//            else -> {
//                // You can directly ask for the permission.
//                // The registered ActivityResultCallback gets the result of this request.
//                ActivityCompat.requestPermissions(this,
//                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                                Manifest.permission.CAMERA), 100)
//            }
//        }

//
// workbookApache = XSSFWorkbook(file)
//                                val sheetApache = workbookApache.getSheetAt(0)
//                                Log.i("MainAct", "Sheet $sheetApache")
//
//                                val iterator = sheetApache.rowIterator()
//                                while(iterator.hasNext()){
//                                    val newRow = iterator.next()
//                                    val columnIterator = newRow.cellIterator()
//                                    while (columnIterator.hasNext()){
//                                        val column = columnIterator.next()
//                                        val value = column.stringCellValue
//                                        Log.i("MainAct", "content $value")
//                                    }
//                                }


//                    object : Thread() {
//                        override fun run() {
//                            try {
//                                binding.progressBar.visibility = View.GONE
//                                //you call here
//                                val transport = AndroidHttp.newCompatibleTransport()
//                                val factory: JsonFactory = JacksonFactory.getDefaultInstance()
//                                val sheetsService = Sheets.Builder(transport, factory, null)
//                                    .setApplicationName("My Awesome App")
//                                    .build()
//                                val spreadsheetId: String = Config.spreadsheet_id
//                                val range = "car_ownsers_data!B2:J65500"
//                                val result: ValueRange =
//                                    sheetsService.spreadsheets().values()[spreadsheetId, range]
//                                        .setKey(Config.google_api_key)
//                                        .execute()
//                                val numRows =
//                                    if (result.getValues() != null) result.getValues().size else 0
//                                Log.d("SUCCESS.", "rows retrived " + result.getValues());
//                            } catch (e: IOException) {
//                                binding.progressBar.visibility = View.GONE
//                                Log.e("Sheets failed", e.getLocalizedMessage())
//                            }
//                        }
//                    }.start()