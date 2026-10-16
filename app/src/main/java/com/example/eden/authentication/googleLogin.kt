package com.example.eden.authentication

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

class googleLogin(val activity: Activity, val launchAuth: (IntentSenderRequest) -> Unit,
                  val onLoginSuc: (BackendlessUser) -> Unit, val onLoginErr: (String) -> Unit)
{
    fun signIn()
    {
        val request = AuthorizationRequest.builder().setRequestedScopes(
            listOf(Scope("openid"), Scope("email"), Scope("profile"))
        ).build()

        Identity.getAuthorizationClient(activity).authorize(request).addOnSuccessListener {
                result -> if(result.hasResolution())
            {
                    val pendingIntent = result.pendingIntent
                    if(pendingIntent != null)
                    {
                        launchAuth(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                    }
                    else
                    {
                        onLoginErr("Error on authorization screen")
                    }
            }
            else
            {
                authorization(result)
            }
        }.addOnFailureListener { error -> onLoginErr(error.message ?: "Google failed to authorize")}

    }

    fun signOut(onSucc: () -> Unit, onError: (String) -> Unit)
    {
        Backendless.UserService.logout(

            object: AsyncCallback<Void>
            {
                override fun handleResponse(response: Void?)
                {
                    Log.d("Backendless","Logged out successfully")
                    activity.runOnUiThread { onSucc() }
                }

                override fun handleFault(fault: BackendlessFault?)
                {
                    Log.e("Backendless", "Logout failure "+ fault?.message)

                    activity.runOnUiThread { onError(fault?.message ?: "Logout fail") }
                }
            }
        )
    }

    fun authorizeIntent(intent: Intent)
    {
        try
        {
            val result = Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(intent)
            authorization(result)
        }
        catch (e: Exception)
        {
            Log.e("GoogleLogin","Failed to authorize", e)
            onLoginErr("Failed Google authorization")
        }
    }

    fun authorization(AR: AuthorizationResult)
    {
        val token=AR.accessToken

        if(token.isNullOrBlank())
        {
            onLoginErr("No access token returned")
            return
        }

        backendlessLogin(token)

    }

    fun backendlessLogin(token: String)
    {
        Backendless.UserService.loginWithOAuth2(
            "googleplus",
            token,
            HashMap<String, String>(),
            object : AsyncCallback<BackendlessUser>
            {
                override fun handleResponse(response: BackendlessUser?)
                {
                    if(response != null)
                    {
                        Log.d("Backendless","Successful backendless login")
                        activity.runOnUiThread { onLoginSuc(response) }
                    }
                    else
                    {
                        activity.runOnUiThread { onLoginErr("Backendless returned no users") }
                    }
                }

                override fun handleFault(fault: BackendlessFault?)
                {
                    val code = fault?.code ?: "Unknown error code"
                    val message= fault?.message ?: "Unknown message"

                    Log.e(
                        "BackendlessAuth",
                        "Login failed,  Error Code: $code, Error Message: $message"
                    )
                    activity.runOnUiThread { onLoginErr("Error "+ code+" "+message) }
                }
            }, true
        )
    }

}