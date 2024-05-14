# login-with-asa

## Description

Use this repo as a reference of how to include Login with Asa in your project.

## Installation


## Usage
Subscription key, Asa Fintech code, Application code and Authorization key are required to use the SDK.

StartActivityForResult or registerForActivityResult in compose with next parameters:

val intent = Intent(this@MainActivity, LoginWithAsaActivity::class.java)
intent.putExtra(LoginWithAsaActivity.SUBSCRIPTION_KEY, "")
intent.putExtra(LoginWithAsaActivity.ASA_FINTECH_CODE, "")
intent.putExtra(LoginWithAsaActivity.APPLICATION_CODE, "")
intent.putExtra(LoginWithAsaActivity.AUTHORIZATION_KEY, "")
intent.putExtra(LoginWithAsaActivity.LOGIN_URL, "")

Subscription key - which was provided to you by Asa Financial
Asa Fintech code - the fintech code which you want to use to login
Application code - the application code which was provided to you by Asa Financial
Authorization key - the authorization key which was provided to you by Asa Financial
Login URL - optional if you want to use specific ASA login URL

OnActivityResult you will either get the Activity.RESULT_OK or Activity.RESULT_CANCELED

If activity result is Activity.RESULT_OK you will get the following data:

consumerCode.value = data?.getStringExtra(LoginWithAsaActivity.ASA_CONSUMER_CODE) ?: ""
bearerToken.value = data?.getStringExtra(LoginWithAsaActivity.BEARER_TOKEN) ?: ""
asaFintechCode.value = data?.getStringExtra(LoginWithAsaActivity.ASA_FINTECH_CODE) ?: ""
expiryDate.value = data?.getStringExtra(LoginWithAsaActivity.EXPIRY_DATE_FOR_TOKEN) ?: ""
email.value = data?.getStringExtra(LoginWithAsaActivity.EMAIL) ?: ""

## License

Apache License 2.0