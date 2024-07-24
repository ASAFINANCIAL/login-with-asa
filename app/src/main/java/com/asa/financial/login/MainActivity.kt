package com.asa.financial.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.asa.financial.login.ui.theme.AsaLoginExampleAppTheme

class MainActivity : ComponentActivity() {
    private var consumerCode = mutableStateOf("")
    private var bearerToken = mutableStateOf("")
    private var asaFintechCode = mutableStateOf("")
    private var expiryDate = mutableStateOf("")
    private var email = mutableStateOf("")

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                consumerCode.value = data?.getStringExtra(LoginWithAsaActivity.ASA_CONSUMER_CODE) ?: ""
                bearerToken.value = data?.getStringExtra(LoginWithAsaActivity.BEARER_TOKEN) ?: ""
                asaFintechCode.value = data?.getStringExtra(LoginWithAsaActivity.ASA_FINTECH_CODE) ?: ""
                expiryDate.value = data?.getStringExtra(LoginWithAsaActivity.EXPIRY_DATE_FOR_TOKEN) ?: ""
                email.value = data?.getStringExtra(LoginWithAsaActivity.EMAIL) ?: ""
            } else {
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsaLoginExampleAppTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column {
                        Text("ConsumerCode: ${consumerCode.value}")
                        LimitedText("BearerToken: ${bearerToken.value}", 12)
                        Text("AsaFintechCode: ${asaFintechCode.value}")
                        Text("ExpiryDate: ${expiryDate.value}")
                        Text("Email: ${email.value}")
                        Button(onClick = {
                            startLoginWithAsa()
                        }) {
                            Text("Start AsaAuthActivity")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LimitedText(text: String, maxLength: Int) {
        val limitedText = if (text.length > maxLength) text.take(maxLength) + "..." else text
        Text(text = limitedText)
    }

    private fun startLoginWithAsa() {
        val intent = Intent(this@MainActivity, LoginWithAsaActivity::class.java)
        intent.putExtra(
            LoginWithAsaActivity.SUBSCRIPTION_KEY,
            "9ae28108db93416fb44058a4a9a7a503"
        )
        intent.putExtra(LoginWithAsaActivity.ASA_FINTECH_CODE, "12345678")
        intent.putExtra(LoginWithAsaActivity.APPLICATION_CODE, "2001")
        intent.putExtra(LoginWithAsaActivity.AUTHORIZATION_KEY, "l8r/i8btbwBRCAaM2m7c")
        startForResult.launch(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AsaLoginExampleAppTheme {
        Greeting("Android")
    }
}