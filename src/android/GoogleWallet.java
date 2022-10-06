package com.landsbankinn;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tapandpay.TapAndPay;
import com.google.android.gms.tapandpay.TapAndPayClient;
import static com.google.android.gms.tapandpay.TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

public class GoogleWallet extends CordovaPlugin {
    private static final String TAG = "GoogleWalletPlugin";
    private CordovaInterface cordova;
    private TapAndPayClient tapAndPayClient;

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
        }
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
    }

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
                                        callbackContext.error("unknown error");
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
}