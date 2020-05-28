package com.example.companytest.Data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.companytest.Model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM User where userName= :userName and password= :password")
    open fun getUser(userName: String?, password: String?): LiveData<MutableList<User?>?>?

    @Query("SELECT * FROM User where userName= :userName")
    open fun checkAlreadyUserExist(userName: String?): User?

    @Insert
    open fun insert(user: User?)
}