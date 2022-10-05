package com.landsbankinn;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;

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
                                Log.i(TAG, "onComplete (getActiveWalletID) - " + task.isSuccessful());
                                if (task.isSuccessful()) {
                                    String result = task.getResult();
                                    Log.i(TAG, "SUCCESS-getActiveWalletID");
                                    callbackContext.success(result);
                                } else {
                                    ApiException apiException = (ApiException) task.getException();
                                    String message = apiException.getMessage();
                                    Log.i(TAG, "ERROR-getActiveWalletID");
                                    callbackContext.error(message);
                                }
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure (getActiveWalletID) - " + e.getMessage());
                        callbackContext.error(e.getMessage());
                    }
                })
                .addOnCanceledListener(
                        new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Log.i(TAG, "onCanceled (getActiveWalletID) - ");
                                callbackContext.error("canceled");
                            }
                        });
    }
}