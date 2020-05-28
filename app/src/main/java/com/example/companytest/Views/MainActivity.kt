package com.example.companytest.Views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.companytest.R
import com.example.companytest.RxBus
import com.example.companytest.adapter.ImagesAdapter
import com.example.companytest.adapter.ImagesAdapter.OnItemDelete
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var allFiles: MutableList<File?>? = null
    var userName: String? = "dummy"
    var adapter: ImagesAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getIntentValues()
        checkDirectoryExistOrNot()
        getImagesIfExist()
        initilizedAdapter()
        AdapterListner()
        checkAnyImageExistOrNot()
        ClickListners()
    }

    private fun ClickListners() {
        add_imgage_fab?.setOnClickListener({ openBottomSheet() })
        logout_fab?.setOnClickListener({
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        })

        RxBus.getInstance()?.observeUserImage()?.subscribe(object : Observer<File?> {


            override fun onComplete() {}
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(t: File) {
                try {
                    isBottomSheetOpen = false
                    checkDirectoryExistOrNot()
                    val defaultFile = createFile()
                    copyFile(t?.getAbsolutePath(), defaultFile?.getAbsolutePath())
                    showList(View.VISIBLE, View.GONE)
                    getAllDataAndNotifyAdapter()
                    smoothScrollToLastItemPosition()
                } catch (e: Exception) {
                    e.printStackTrace()
                }            }

            override fun onError(e: Throwable) {
            }
        })
    }

    private fun smoothScrollToLastItemPosition() {
        Handler().postDelayed({ allFiles?.size?.minus(1)?.let { recy_image?.smoothScrollToPosition(it) } }, 1300)
    }

    private fun getAllDataAndNotifyAdapter() {
        allFiles?.clear()
        allFiles = getListFiles(File(Environment.getExternalStorageDirectory().absolutePath + "/tempdatafile"))
        adapter?.notifyDataSetChanged()
    }

    private fun showList(visible: Int, gone: Int) {
        recy_image?.setVisibility(visible)
        txt_no_image_found?.setVisibility(gone)
    }

    private fun createFile(): File? {
        var dateAsString: String?
        val simpleDateFormat = SimpleDateFormat("MMddhhmmss")
        dateAsString = simpleDateFormat.format(Date())
        val defaultFile = File(Environment.getExternalStorageDirectory().absolutePath + "/tempdatafile/" + userName + "_" + dateAsString + ".jpg")
        if (!defaultFile.exists()) {
            try {
                defaultFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return defaultFile
    }

    private fun AdapterListner() {
        adapter?.setonItemDelete(object : OnItemDelete {
            override fun onItemDelete(position: Int, file: File?) {
                showAlert(position, file)
            }
        })
    }

    private fun checkAnyImageExistOrNot() {
        if (allFiles?.size!! > 0) {
            showList(View.VISIBLE, View.GONE)
        } else {
            showList(View.GONE, View.VISIBLE)
        }
    }

    private fun showAlert(position: Int, file: File?) {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setCancelable(true)
        builder.setTitle(R.string.alert)
        builder.setMessage(R.string.alert_des)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.confirm
        ) { dialog, which ->
            val deleted = file?.delete()
            allFiles?.removeAt(position)
            adapter?.notifyDataSetChanged()
            if (allFiles?.size == 0) {
                showList(View.GONE, View.VISIBLE)
            }
            if (deleted!!) {
                Toast.makeText(this@MainActivity, R.string.delete_success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, R.string.image_not_delete, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun initilizedAdapter() {
        adapter = ImagesAdapter(this@MainActivity, allFiles)
        recy_image?.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        recy_image?.setAdapter(adapter)
    }

    private fun getImagesIfExist() {
        allFiles = ArrayList()
        allFiles = getListFiles(File(Environment.getExternalStorageDirectory().absolutePath + "/tempdatafile"))
    }

    private fun getIntentValues() {
        if (intent != null) {
            if (intent.hasExtra("User")) {
                userName = intent.getStringExtra("User")
            }
        }
    }

    private fun checkDirectoryExistOrNot() {
        val defaultFile2 = File(Environment.getExternalStorageDirectory().absolutePath + "/tempdatafile")
        if (!defaultFile2.exists()) defaultFile2.mkdirs()
    }

    private fun getListFiles(parentDir: File?): MutableList<File?>? {
        val files = parentDir?.listFiles()
        val nameList: MutableList<String?> = ArrayList()
        try {
            for (file in files!!) {
                if (getUserFileName(file.name) == userName) {
                    nameList.add(file.name)
                    Log.e("FileName", file.name)
                    allFiles?.add(file)
                }
            }
        } catch (e: Exception) {
            Log.e("TAG", e.toString())
        }
        return allFiles
    }

    fun openBottomSheet() {
        val transaction = supportFragmentManager
                .beginTransaction()
        val bottomSheetAddImage = BottomSheetAddImage()
        bottomSheetAddImage.show(transaction, "")
        // isBottomSheetOpen=true;
    }

    override fun onPause() {
        super.onPause()

        //For privacy application close when user resume the App
        if (!isBottomSheetOpen) finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.Companion.getInstance()?.clearRx()
    }

    companion object {
        var isBottomSheetOpen = false
        fun copyFile(inputPath: String?, outputPath: String?) {
            var `in`: InputStream?
            var out: OutputStream?
            try {
                `in` = FileInputStream(inputPath)
                out = FileOutputStream(outputPath)
                val buffer = ByteArray(1024)
                var read: Int
                while (`in`.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                `in`.close()
                `in` = null
                out.flush()
                out.close()
                out = null
            } catch (fnfe1: FileNotFoundException) {
                Log.d("e", fnfe1.message)
            } catch (e: Exception) {
            }
        }

        fun getUserFileName(fileName: String?): String? {
            var userName: String?
            try {
                userName = fileName?.substring(0, fileName.indexOf("_"))
                // System.out.println(new Date() + ": " + "Corporate:
                // "+userName);
                return userName
            } catch (e: Exception) {
                userName = null
                e.printStackTrace()
            }
            return userName
        }
    }
}