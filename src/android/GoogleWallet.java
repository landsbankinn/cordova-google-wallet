package com.landsbankinn;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.tapandpay.TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tapandpay.TapAndPay;
import com.google.android.gms.tapandpay.TapAndPayClient;
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest;
import com.google.android.gms.tapandpay.issuer.UserAddress;
import com.google.android.gms.tapandpay.issuer.TokenInfo;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import java.util.List;

public class GoogleWallet extends CordovaPlugin {
    private static final String TAG = "GoogleWalletPlugin";
    private static final int REQUEST_CODE_PUSH_TOKENIZE = 3;
    private CordovaInterface cordova;
    private TapAndPayClient tapAndPayClient;
    private CallbackContext callbackContext;


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        tapAndPayClient = TapAndPay.getClient(this.cordova.getActivity());
        Log.i(TAG, "INITIALIZED");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        Log.i(TAG, action);
        Log.i(TAG, args.toString());

        this.callbackContext = callbackContext;

        if ("getActiveWalletID".equals(action)) {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    getActiveWalletID(callbackContext);
                }
            });
            return true;
        } else if ("getStableHardwareId".equals(action)) {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    getStableHardwareId(callbackContext);
                }
            });
            return true;
        } else if ("listTokens".equals(action)) {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    listTokens(callbackContext);
                }
            });
            return true;
        } else if ("pushTokenize".equals(action)) {
            final CordovaPlugin plugin = (CordovaPlugin) this;

            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String opc = args.getString(0);
                        String displayName = args.getString(1);
                        String lastDigits = args.getString(2);
                        JSONObject address = args.getJSONObject(3);

                        pushTokenize(opc, displayName, lastDigits, address, callbackContext);
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });

            return true;
        }
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
    }

    /**
     * Returns the Wallet ID of the active wallet. If there is no active wallet, the status
     * TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET is returned. A wallet can be created using the method createWallet.
     */
    private void getActiveWalletID(CallbackContext callbackContext) {
        Log.i(TAG, "getActiveWalletID");
        tapAndPayClient
                .getActiveWalletId()
                .addOnCompleteListener(
                        new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                Log.i(TAG, "getActiveWalletID onComplete " + task.isSuccessful());
                                if (task.isSuccessful()) {
                                    String result = task.getResult();
                                    callbackContext.success(result);
                                } else {
                                    ApiException apiException = (ApiException) task.getException();
                                    String message = apiException.getMessage();
                                    int statusCode = apiException.getStatusCode();
                                    Log.i(TAG, "getActiveWalletID onComplete " + Integer.toString(statusCode));
                                    try {
                                        JSONObject value = new JSONObject();
                                        value.put("message", message);
                                        value.put("statusCode", statusCode);
                                        callbackContext.error(value);
                                    } catch (Exception e) {
                                        callbackContext.error(e.getMessage());
                                    }
                                }
                            }
                        });
    }

    /**
     * List tokens in the active wallet
     * 
     * The listTokens method will return a list of tokens in the Google Pay wallet.
     * Note that the API only returns token details for tokens with metadata 
     * matching your app package name. You can check if your tokens have this
     * linking by tapping on the card in the Google Wallet app to see the card details view.
     */
    private void listTokens(CallbackContext callbackContext) {
        Log.i(TAG, "listTokens");
        tapAndPayClient
                .listTokens()
                .addOnCompleteListener(
                        new OnCompleteListener<List<TokenInfo>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<TokenInfo>> task) {
                                Log.i(TAG, "listTokens onComplete " + task.isSuccessful());
                                if (task.isSuccessful()) {
                                    JSONArray result = new JSONArray();
                                    try {
                                        for (TokenInfo token : task.getResult()) {
                                            Log.d(TAG, "Found token with ID: " + token.getIssuerTokenId());
                                            JSONObject item = new JSONObject();
                                            item.put("issuerTokenId", token.getIssuerTokenId());
                                            item.put("issuerName", token.getIssuerName());
                                            item.put("fpanLastFour", token.getFpanLastFour());
                                            item.put("dpanLastFour", token.getDpanLastFour());
                                            item.put("tokenServiceProvider", token.getTokenServiceProvider());
                                            item.put("network", token.getNetwork());
                                            item.put("tokenState", token.getTokenState());
                                            item.put("isDefaultToken", token.getIsDefaultToken());
                                            item.put("portfolioName", token.getPortfolioName());
                                            
                                        }
                                        callbackContext.success(result);
                                    } catch(Exception e) {
                                        callbackContext.error(e.getMessage());
                                    }
                                } else {
                                    ApiException apiException = (ApiException) task.getException();
                                    String message = apiException.getMessage();
                                    int statusCode = apiException.getStatusCode();
                                    Log.i(TAG, "listTokens onComplete " + Integer.toString(statusCode));
                                    try {
                                        JSONObject value = new JSONObject();
                                        value.put("message", message);
                                        value.put("statusCode", statusCode);
                                        callbackContext.error(value);
                                    } catch (Exception e) {
                                        callbackContext.error(e.getMessage());
                                    }
                                }
                            }
                        });
    }

    /**
     * Each physical Android device has a stable hardware ID which is consistent between wallets for a given device. This ID will change as a result of a factory reset.
     * 
     * The stable hardware ID MAY ONLY be used for the following purposes:
     * - to encrypt inside OPC and provide back to Google Pay for push provisioning
     * - to make a risk decision (without storing the value) before the start of a Push Provisioning flow
     * 
     * The stable hardware ID received through the client API MUST NOT be used for the following purposes:
     * - stored by the issuer locally or at the backend
     * - track user activity
     * 
     * The stable hardware ID may not be accessed by the issuer outside the Push Provisioning flow.
     * 
     * Note: Make sure you have a Wallet ID before retrieving the stable hardware ID. If a wallet is not created yet,
     * the call to task.getResult() will return an empty String.
     * 
     * https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?authuser=2#getstablehardwareid
     */
    private void getStableHardwareId(CallbackContext callbackContext) {
        Log.i(TAG, "getStableHardwareId");
        tapAndPayClient
                .getStableHardwareId()
                .addOnCompleteListener(
                        new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                Log.i(TAG, "getStableHardwareId onComplete " + task.isSuccessful());
                                if (task.isSuccessful()) {
                                    String hardwareId = task.getResult();
                                    if (hardwareId == "") {
                                        try {
                                            JSONObject value = new JSONObject();
                                            value.put("message", "No active wallet");
                                            value.put("statusCode", 15002);
                                            callbackContext.error(value);
                                        } catch (Exception e) {
                                            callbackContext.error(e.getMessage());
                                        }
                                    } else {
                                        callbackContext.success(hardwareId);
                                    }
                                } else {
                                    try {
                                        JSONObject value = new JSONObject();
                                        value.put("message", "Unknown error");
                                        value.put("statusCode", 0);
                                        callbackContext.error(value);
                                    } catch (Exception e) {
                                        callbackContext.error(e.getMessage());
                                    }
                                }
                            }
                        });
    }

    /**
     * Client-side push provisioning
     * 
     * pushTokenize starts the push tokenization flow in which the issuer provides most or all
     * card details needed for Google Pay to get a valid token. Tokens added using this method
     * are added to the active wallet.
     * 
     * @param opc Opaque Payment Card binary data
     * @param displayName Name or nickname used to describe the payment card in the user interface
     * @param address JSONObject containing optional string properties: name, address1, address2, localitiy, administrativeArea, countryCode, postalCode and phoneNumber
     * @param lastDigits Last 4 digits for the payment card required to correctly display the card in Google Pay UI
     * 
     * https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations?authuser=2#client-side_push_provisioning
     */
    private void pushTokenize(String opc, String displayName, String lastDigits, JSONObject address, CallbackContext callbackContext) {
      
        try {
            Log.i(TAG, "pushTokenize");

            UserAddress.Builder builder = UserAddress.newBuilder();
            if (address.has("name")) {
                builder.setName(address.getString("name"));
            }
            if (address.has("address1")) {
                builder.setAddress1(address.getString("address1"));
            }
            if (address.has("address2")) {
                builder.setAddress2(address.getString("address2"));
            }
            if (address.has("locality")) {
                builder.setLocality(address.getString("locality"));
            }
            if (address.has("administrativeArea")) {
                builder.setAdministrativeArea(address.getString("administrativeArea"));
            }
            if (address.has("countryCode")) {
                builder.setCountryCode(address.getString("countryCode"));
            }
            if (address.has("postalCode")) {
                builder.setPostalCode(address.getString("postalCode"));
            }
            if (address.has("phoneNumber")) {
                builder.setPhoneNumber(address.getString("phoneNumber"));
            }

            UserAddress userAddress = builder.build();

            PushTokenizeRequest pushTokenizeRequest =
                    new PushTokenizeRequest.Builder()
                            .setOpaquePaymentCard(opc.getBytes())
                            .setNetwork(TapAndPay.CARD_NETWORK_VISA)
                            .setTokenServiceProvider(TapAndPay.TOKEN_PROVIDER_VISA)
                            .setDisplayName(displayName)
                            .setLastDigits(lastDigits)
                            .setUserAddress(userAddress)
                            .build();
            
            cordova.setActivityResultCallback(this);

            tapAndPayClient.pushTokenize(this.cordova.getActivity(), pushTokenizeRequest, REQUEST_CODE_PUSH_TOKENIZE);
        } catch (Exception e) {
            Log.e(TAG, "pushTokenize error", e);
            callbackContext.error(e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult resultCode: " + resultCode + ", requestCode: " + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        // Push provisioning
        if (requestCode == REQUEST_CODE_PUSH_TOKENIZE) {
            if (resultCode == RESULT_CANCELED) {
                // The user canceled the request.
                try {
                    JSONObject value = new JSONObject();
                    value.put("type", "canceled");
                    Log.e(TAG, "onActivityResult error RESULT_CANCELED");
                    callbackContext.error(value);
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult error RESULT_CANCELED JSON exception", e);
                    callbackContext.error(e.getMessage());
                }
                return;
            } else if (resultCode == RESULT_OK) {
                // The action succeeded.
                String tokenId = data.getStringExtra(TapAndPay.EXTRA_ISSUER_TOKEN_ID);
                try {
                    JSONObject value = new JSONObject();
                    value.put("type", "result");
                    value.put("tokenId", tokenId);
                    Log.i(TAG, "onActivityResult ok tokenId:" + tokenId);
                    callbackContext.success(value);
                } catch (Exception e) {
                    Log.e(TAG, "onActivityResult error RESULT_OK JSON exception", e);
                    callbackContext.error(e.getMessage());
                }
                return;
            } else {
                Log.e(TAG, "onActivityResult error unknown resultCode: " + Integer.toString(resultCode));
                callbackContext.error(resultCode);
            }
        }
    }

}