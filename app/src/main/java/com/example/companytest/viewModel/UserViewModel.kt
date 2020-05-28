package com.example.companytest.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.companytest.Data.UserDataBase
import com.example.companytest.Model.User

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private var allData: LiveData<MutableList<User?>?>? = null
    var appDatabase: UserDataBase?
    private var userExist: MutableLiveData<Boolean?>? = null
    fun getAllData(userName: String?, password: String?): LiveData<MutableList<User?>?>? {
        return appDatabase?.getUserDao()?.getUser(userName, password).also { allData = it }
    }

    fun checkAlreadyUserExist(userName: String?): MutableLiveData<Boolean?>? {
        val user = appDatabase?.getUserDao()?.checkAlreadyUserExist(userName)
        userExist = MutableLiveData()
        return if (user != null) {
            userExist?.setValue(true)
            userExist
        } else {
            userExist?.setValue(false)
            userExist
        }
    }

    fun setUserData(user: User?) {
        appDatabase?.getUserDao()?.insert(user)
    }

    init {
        appDatabase = UserDataBase.Companion.getAppDatabase(application.applicationContext)
    }
}