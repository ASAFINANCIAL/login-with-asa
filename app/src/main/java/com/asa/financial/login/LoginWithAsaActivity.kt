package com.asa.financial.login

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class LoginWithAsaActivity : AppCompatActivity() {
    companion object {
        const val SUBSCRIPTION_KEY = "subscriptionKey"
        const val APPLICATION_CODE = "applicationCode"
        const val AUTHORIZATION_KEY = "authorizationKey"
        const val ASA_CONSUMER_CODE = "asaConsumerCode"
        const val BEARER_TOKEN = "bearerToken"
        const val ASA_FINTECH_CODE = "asaFintechCode"
        const val EXPIRY_DATE_FOR_TOKEN = "expiryDateForToken"
        const val EMAIL = "asa_email"
        const val ERROR = "asa_error"
        const val LOGIN_URL = "loginUrl"

    }
    private val redirectUrl: String = "https://loginwithasa/asalogin"
    private val redirectFailureUrl: String = "https://loginwithasa/asaloginfailed"
    private val defaultUrl = "https://openapiqa.asacore.com/Authentication/Authorization"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val subscriptionKey = intent.getStringExtra(SUBSCRIPTION_KEY)
        val asaFintechCode = intent.getStringExtra(ASA_FINTECH_CODE)
        val applicationCode = intent.getStringExtra(APPLICATION_CODE)
        val authorizationKey = intent.getStringExtra(AUTHORIZATION_KEY)
        val url = intent.getStringExtra(LOGIN_URL) ?: defaultUrl

        val progress = findViewById<LinearLayout>(R.id.progress_lt)
        val webView = findViewById<WebView>(R.id.web_view)
        progress.visibility = View.VISIBLE

        // Call the function to make the GET request
        thread {
            val result = getAPIResponse(
                url,
                asaFintechCode,
                applicationCode,
                authorizationKey,
                subscriptionKey
            )
            runOnUiThread {
                showWebView(progress, webView, result)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebView(
        progress: LinearLayout,
        webView: WebView,
        result: String
    ) {
        progress.visibility = View.GONE
        webView.visibility = View.VISIBLE

        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.isScrollbarFadingEnabled = false

        webView.settings.builtInZoomControls = false
        webView.settings.setSupportZoom(false)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.allowFileAccess = true
        webView.settings.domStorageEnabled = true

        // Use the result here
        webView.loadUrl(result)
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e("WebView", "Error loading page: ${error?.description}")
                } else {
                    Log.e("WebView", "Error loading page: ${error?.toString()}")
                }
                returnFailureResult()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("WebView", "Finished loading page: $url")
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.startsWith(redirectUrl)) {
                    Log.d("WebView", "Redirecting to: $url")
                    returnSuccessResult(request?.url)
                    return true
                } else {
                    returnFailureResult()
                    return true
                }
            }
        }
    }

    private fun returnSuccessResult(uri: Uri?) {
        uri?.let {
            val asaConsumerCode = uri.getQueryParameter("asaconsumerCode")
            val bearerToken = uri.getQueryParameter(BEARER_TOKEN)
            val asaFintechCode = uri.getQueryParameter(ASA_FINTECH_CODE)
            val expiryDateForToken = uri.getQueryParameter("expirydatefortoken")
            val email = uri.getQueryParameter("email")

            val data = Intent().apply {
                putExtra(ASA_CONSUMER_CODE, asaConsumerCode)
                putExtra(BEARER_TOKEN, bearerToken)
                putExtra(ASA_FINTECH_CODE, asaFintechCode)
                putExtra(EXPIRY_DATE_FOR_TOKEN, expiryDateForToken)
                putExtra(EMAIL, email)
            }
            setResult(RESULT_OK, data)
            finish()
        }

        returnFailureResult()
    }

    private fun returnFailureResult() {
        val data = Intent().apply {
            putExtra(ERROR, "Failed to authenticate")
        }
        setResult(RESULT_CANCELED, data)
        finish()
    }

    private fun getAPIResponse(
        urlStr: String,
        asaFintechCode: String?,
        applicationCode: String?,
        authorizationKey: String?,
        subscriptionKey: String?
    ): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"

        // Add headers
        conn.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey)
        conn.setRequestProperty("X-ASA-APIVersion", "1.07")
        conn.setRequestProperty("Content-Type", "application/json")

        // Enable output for the connection
        conn.doOutput = true

        // Write the JSON data to the output stream
        val outputData = """
        {
            "asaFintechCode": "$asaFintechCode",
            "applicationCode": "$applicationCode",
            "authorizationKey": "$authorizationKey",
            "redirectUrl": "$redirectUrl",
            "redirectFailureUrl": "$redirectFailureUrl",
            "scope": "openid",
            "subscriptionKey": "$subscriptionKey"
        }
    """.trimIndent()
        conn.outputStream.use { os ->
            val input = outputData.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }


        conn.connect()

        val responseCode = conn.responseCode
        Log.d("ResultActivity", "Response code: $responseCode")
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val response = reader.readText()
            reader.close()

            // Parse the JSON response
            val jsonObject = JSONObject(response)
            val status = jsonObject.getInt("status")
            val message = jsonObject.getString("message")
            val version = jsonObject.getString("version")
            val data = jsonObject.getJSONObject("data")
            val field = data.getString("field")
            val dataMessage = data.getString("message")

            Log.d(
                "ResultActivity",
                "Parsed JSON: status=$status, message=$message, version=$version, field=$field, dataMessage=$dataMessage"
            )

            return dataMessage
        } else {
            val reader = BufferedReader(InputStreamReader(conn.errorStream))
            val errorResponse = reader.readText()
            reader.close()
            Log.d(
                "ResultActivity",
                "Error response: $errorResponse"
            ) // Log the entire error response
            return "Error: $responseCode"
        }
    }
}