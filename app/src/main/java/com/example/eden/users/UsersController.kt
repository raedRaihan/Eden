package com.example.eden.users

import android.util.Log
import com.backendless.async.callback.AsyncCallback
import com.backendless.Backendless
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



    fun deleteUser(email: String)
    {

    }
}