package com.example.eden.users

import android.util.Log
import com.backendless.async.callback.AsyncCallback
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.exceptions.BackendlessFault

class UsersController
{
    fun addUser(user:Users)
    {
        Backendless.Data.of(Users::class.java).save(
            user,
            object : AsyncCallback<Users>{

                override fun handleResponse(response: Users?)
                {
                    Log.d("Backendless", "User saved!")
                    Log.d("Backendless", "Email: ${response?.email}")
                }

                override fun handleFault(fault: BackendlessFault?)
                {
                    Log.e("Backendless", "Error: ${fault?.message}")
                }
            }
        )
    }

   fun searchForUser(email: String?=null): Boolean
    {
        var foundUser= false
        Backendless.Data.of(Users::class.java).find(
                object : AsyncCallback<List<Users>>{

                override fun handleResponse(response: List<Users>?)
                {

                    response?.forEach { user ->
                        if(user.email == email)
                        {
                            Log.d("Backendless", "Found target user: ${user.name}")
                            foundUser = true
                        }

                    }
                }

                override fun handleFault(fault: BackendlessFault?) {
                    Log.e("Backendless", "Error: ${fault?.message}")
                }
            }
        )
        return foundUser
    }


    fun login(
        email: String,
        password: String,
        onSuccess: (BackendlessUser) -> Unit,
        onError: (String) -> Unit
    ) {
        Backendless.UserService.login(
            email,
            password,
            object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser) {
                    Log.d("Backendless", "Logged in: $email")
                    onSuccess(response)
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("Backendless", "Login failed: ${fault.message}")
                    onError(fault.message ?: "Could not log in.")
                }
            },
            true
        )
    }

    fun registerAdopter(
        first: String,
        last: String,
        email: String,
        password: String,
        onSuccess: (BackendlessUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = BackendlessUser().apply {
            setProperty("first", first)
            setProperty("last", last)
            setProperty("email", email)
            setProperty("userType", "Adopter")
            setPassword(password)
        }

        Backendless.UserService.register(
            user,
            object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser) {
                    Log.d("Backendless", "Registered adopter: $email")
                    onSuccess(response)
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.e("Backendless", "Registration failed: ${fault.message}")
                    onError(fault.message ?: "Could not create the account.")
                }
            }
        )
    }

    fun deleteUser(email: String)
    {

    }
}