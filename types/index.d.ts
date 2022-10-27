/**
 * Returns the Wallet ID of the active wallet. If there is no active wallet, the status
 * GoogleWalletStatusCodes.NO_ACTIVE_WALLET is returned. A wallet can be created using the method createWallet.
 */
export function getActiveWalletId(): Promise<
  | {
      type: "result";
      walletId: string;
    }
  | {
      type: "error";
      statusCode:
        | GoogleWalletStatusCodes.UNAVAILABLE
        | GoogleWalletStatusCodes.NO_ACTIVE_WALLET;
      message: string;
    }
>;

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
export function GetStableHardwareId(): Promise<
  | {
      type: "result";
      hardwareId: string;
    }
  | {
      type: "error";
      statusCode:
        | GoogleWalletStatusCodes.UNKNOWN_ERROR
        | GoogleWalletStatusCodes.NO_ACTIVE_WALLET;
      message: string;
    }
>;

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
export function listTokens(): Promise<
  | {
      type: "result";
      tokens: GoogleWalletTokenInfo[];
    }
  | {
      type: "error";
      statusCode: GoogleWalletStatusCodes;
      message: string;
    }
>;

/**
 * Client-side push provisioning
 *
 * pushTokenize starts the push tokenization flow in which the issuer provides most or all
 * card details needed for Google Pay to get a valid token. Tokens added using this method
 * are added to the active wallet.
 *
 * https://developers.google.com/pay/issuers/apis/push-provisioning/android/wallet-operations?authuser=2#client-side_push_provisioning
 */
export function pushTokenize({
  opc,
  displayName,
  lastDigits,
  address,
}: {
  /**
   * Opaque Payment Card JWT
   */
  opc: string;
  /**
   * Name or nickname used to describe the payment card in the user interface
   */
  displayName: string;
  /**
   * Last 4 digits for the payment card required to correctly display the card in Google Pay UI
   */
  lastDigits: string;
  /**
   * Push provisioning takes a UserAddress that must be provided in order to skip manual
   * address entry. The issuer application should provide the entire address and phone
   * number on file. Missing or invalid address information may result in the user being
   * prompted to complete or correct the address. The address should be the correct address
   * of the user to the best of the issuer's knowledge. Using a fake or intentionally
   * incorrect addresses is not permitted by the Push Provisioning API terms of service.
   */
  address: {
    /** First line of address */
    address1?: string;
    /** Second line of address */
    address2?: string;
    /** The country code */
    countryCode?: string;
    /** The city, town, etc */
    locality?: string;
    /** The state, province, etc */
    administrativeArea?: string;
    /** Name of the person at this address */
    name?: string;
    /** The phone number associated with the card */
    phoneNumber?: string;
    /** The postal or zip code */
    postalCode?: string;
  };
}): Promise<
  | {
      type: "result";
      tokenId: string;
    }
  | {
      type: "canceled";
    }
>;

/**
 * Card lookup by last 4 FPAN digits
 * Please note this endpoint can return false positives since the last four FPAN digits are not necessarily unique among tokens.
 * @param indentifier last for FPAN (Last 4 card number digits)
 *
 * https://developers.google.com/pay/issuers/apis/push-provisioning/android/reading-wallet?authuser=2#istokenized
 */
export function isCardInWallet({ identifier }: { identifier: string }): Promise<
  | {
      type: "result";
      result: "token found" | "token not found";
    }
  | {
      type: "error";
      statusCode: GoogleWalletStatusCodes;
      message: string;
    }
>;
