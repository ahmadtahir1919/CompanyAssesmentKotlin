package com.example.companytest.Views

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.companytest.Model.User
import com.example.companytest.R
import com.example.companytest.viewModel.UserViewModel
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_register.*
import java.text.DateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    private var gender = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        login_view?.setVisibility(View.VISIBLE)
        val userViewModel = getUserViewModel()
        ClickListners(userViewModel)
    }

    private fun ClickListners(userViewModel: UserViewModel?) {
        txt_sign_up?.setOnClickListener({ showRegisterScreen(login_view, register_view) })
        btn_sign_up?.setOnClickListener({
            if (edt_user_name?.getText().toString().trim { it <= ' ' } != "" || edt_sign_up_password?.getText().toString().trim { it <= ' ' } != "" || layout_edt_register_date_of_birth?.getEditText()?.getText().toString().trim { it <= ' ' } != "") {
                val user = User(edt_user_name?.getText().toString().trim { it <= ' ' }, edt_sign_up_password?.getText().toString().trim { it <= ' ' }, gender.toString(), layout_edt_register_date_of_birth?.getEditText()?.getText().toString().trim { it <= ' ' })
                userViewModel?.checkAlreadyUserExist(edt_user_name?.getText().toString().trim { it <= ' ' })?.observe(this@LoginActivity, Observer { isUserExist ->
                    if (isUserExist!!) {
                        Toast.makeText(this@LoginActivity, "User Already Exist", Toast.LENGTH_SHORT).show()
                    } else {
                        userViewModel.setUserData(user)
                        Toast.makeText(this@LoginActivity, "Sign up success", Toast.LENGTH_SHORT).show()
                        showRegisterScreen(register_view, login_view)
                    }
                })
            } else {
                Toast.makeText(this@LoginActivity, "Please fill All fields", Toast.LENGTH_SHORT).show()
            }
        })
        edt_date_of_birth?.setOnClickListener({ showAndSetDateOfBirth() })
        rd_gender?.setOnCheckedChangeListener({ group, checkedId -> setGender(checkedId) })
        btn_login?.setOnClickListener({
            userViewModel?.getAllData(edt_login_user_name?.getText().toString().trim { it <= ' ' }, layout_login_edt_register_password?.getEditText()?.getText().toString().trim { it <= ' ' })?.observe(this@LoginActivity, Observer { users ->
                if (users != null) {
                    if (users.size > 0) {
                        val i = Intent(this@LoginActivity, MainActivity::class.java)
                        i.putExtra("User", users[0]?.getUserName())
                        startActivity(i)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "User Not Exist or password not match", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "User Not Exist", Toast.LENGTH_SHORT).show()
                }
            })
        })
        txt_sign_in?.setOnClickListener({
            register_view?.setVisibility(View.GONE)
            login_view?.setVisibility(View.VISIBLE)
        })
    }

    private fun setGender(checkedId: Int) {
        if (checkedId == R.id.male) {
            gender = 1
        } else if (checkedId == R.id.female) {
            gender = 0
        }
    }

    private fun showAndSetDateOfBirth() {
        val newCalendar = Calendar.getInstance()
        val StartTime = DatePickerDialog(this@LoginActivity, OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val newDate = Calendar.getInstance()
            newDate[year, monthOfYear] = dayOfMonth
            edt_date_of_birth?.setText(DateFormat.getDateInstance().format(newDate.time))
        }, newCalendar[Calendar.YEAR], newCalendar[Calendar.MONTH], newCalendar[Calendar.DAY_OF_MONTH])
        StartTime.show()
    }

    private fun showRegisterScreen(login_view: ConstraintLayout?, register_view: ConstraintLayout?) {
        login_view?.setVisibility(View.GONE)
        register_view?.setVisibility(View.VISIBLE)
    }

    fun getUserViewModel(): UserViewModel? {
        return ViewModelProvider(this)
                .get(UserViewModel::class.java)
    }
}