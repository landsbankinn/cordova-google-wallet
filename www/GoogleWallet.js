const exec = cordova.require("cordova/exec");

const service = "GoogleWallet";

const tapAndPay = {
  getActiveWalletId,
  getStableHardwareId,
  listTokens,
  pushTokenize,
  isCardInWallet,
};

/**
 * Returns the Wallet ID of the active wallet. If there is no active wallet, the status
 * GoogleWalletStatusCodes.NO_ACTIVE_WALLET is returned. A wallet can be created using the method createWallet.
 */
function getActiveWalletId() {
  return new Promise((resolve, reject) => {
    exec(
      (walletId) => {
        resolve({ type: "result", walletId });
      },
      (error) => {
        if (typeof error === "string") {
          reject(error);
        } else {
          resolve({
            type: "error",
            statusCode: error.statusCode,
            message: error.message,
          });
        }
      },
      service,
      "getActiveWalletID"
    );
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
 *
 */
function getStableHardwareId() {
  return new Promise((resolve, reject) => {
    exec(
      (hardwareId) => {
        resolve({ type: "result", hardwareId });
      },
      (error) => {
        if (typeof error === "string") {
          reject(error);
        } else {
          resolve({
            type: "error",
            statusCode: error.statusCode,
            message: error.message,
          });
        }
      },
      service,
      "getStableHardwareId"
    );
  });
}

/**
 * List tokens in the active wallet
 *
 * The listTokens method will return a list of tokens in the Google Pay wallet.
 * Note that the API only returns token details for tokens with metadata matching
 * your app package name. You can check if your tokens have this linking by tapping
 * on the card in the Google Wallet app to see the card details view.
 *
 * https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?authuser=2#listtokens
 */
function listTokens() {
  return new Promise((resolve, reject) => {
    exec(
      (tokens) => {
        resolve({ type: "result", tokens });
      },
      (error) => {
        if (typeof error === "string") {
          reject(error);
        } else {
          resolve({
            type: "error",
            statusCode: error.statusCode,
            message: error.message,
          });
        }
      },
      service,
      "listTokens"
    );
  });
}

/**
 * Client-side push provisioning
 *
 * pushTokenize starts the push tokenization flow in which the issuer provides most or all
 * card details needed for Google Pay to get a valid token. Tokens added using this method
 * are added to the active wallet.
 *
 * https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations?authuser=2#client-side_push_provisioning
 */
function pushTokenize({ opc, displayName, lastDigits, address }) {
  return new Promise((resolve, reject) => {
    exec(
      ({ tokenId }) => {
        resolve({ type: "result", tokenId });
      },
      (error) => {
        if (typeof error === "string") {
          reject(error);
        } else {
          resolve({ type: "canceled" });
        }
      },
      service,
      "pushTokenize",
      [opc, displayName, lastDigits, address]
    );
  });
}

/**
 * Card lookup by last 4 FPAN digits
 * Please note this endpoint can return false positives since the last four FPAN digits are not necessarily unique among tokens.
 * @param indentifier last for FPAN (Last 4 card number digits)
 *
 * https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?authuser=2#istokenized
 */
function isCardInWallet({ identifier }) {
  return new Promise((resolve, reject) => {
    exec(
      ({ result }) => {
        resolve({ type: "result", result });
      },
      (error) => {
        if (typeof error === "string") {
          reject(error);
        } else {
          resolve({
            type: "error",
            statusCode: error.statusCode,
            message: error.message,
          });
        }
      },
      service,
      "isCardInWallet",
      [identifier]
    );
  });
}

module.exports = tapAndPay;
