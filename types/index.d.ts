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

export type GoogleWalletTokenInfo = {
  /**
   * The token reference ID.
   */
  issuerTokenId: string;
  /**
   * The name of the issuer.
   */
  issuerName: string;
  /**
   * The last four digits of the FPAN.
   */
  fpanLastFour: string;
  /**
   * The last four digits of the DPAN.
   */
  dpanLastFour: string;
  /**
   * The token provider specifies the tokenization service used to
   * create a given token. The terms “token provider” and
   * “token service provider” (TSP) are used interchangeably in this API.
   */
  tokenServiceProvider: GoogleWalletTokenProvider;
  /**
   * The CardNetwork.
   */
  network: GoogleWalletCardNetwork;
  /**
   * The TokenState.
   */
  tokenState: GoogleWalletTokenState;
  /**
   * True if the token is set as the default.
   */
  isDefaultToken: boolean;
  /**
   * The card portfolio.
   */
  portfolioName: string;
};

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

export type PushTokenizeArgs = {
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
};

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
}: PushTokenizeArgs): Promise<
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

/* ENUMS */

export enum GoogleWalletStatusCodes {
  UNKNOWN_ERROR = 0,
  /**
   * There is no active wallet.
   */
  NO_ACTIVE_WALLET = 15002,
  /**
   * The indicated issuer token ID does not match a token in the active wallet.
   * This status can be returned by calls that specify an issuer token ID.
   */
  TOKEN_NOT_FOUND = 15003,
  /**
   * The specified token was found but was not in a legal state for the
   * operation to succeed. For example, this can happen when attempting to
   * select as default a token that is not in the state TOKEN_STATE_ACTIVE.
   */
  INVALID_TOKEN_STATE = 15004,
  /**
   * Tokenization failed because the device did not pass a compatibility check.
   */
  ATATTESTATION_ERROR = 15005,
  /**
   * The TapAndPay API cannot be called by the current application.
   * If you get this error, make sure you are calling the API using a package name
   * and fingerprint that we have added to our allowlist.
   *
   * The app needs to be verified first by Google to be able to talk to the
   * TapAndPay api. See readme for further information.
   */
  UNAVAILABLE = 15009,

  // No documentation was found for following enum values

  SAVE_CARD_ERROR = 15019,
  INELIGIBLE_FOR_TOKENIZATION = 15021,
  TOKENIZATION_DECLINED = 15022,
  CHECK_ELIGIBILITY_ERROR = 15023,
  TOKENIZE_ERROR = 15024,
  TOKEN_ACTIVATION_REQUIRED = 15025,
  PAYMENT_CREDENTIALS_DELIVERY_TIMEOUT = 15026,
  USER_CANCELED_FLOW = 15027,
  ENROLL_FOR_VIRTUAL_CARDS_FAILED = 15028,
}

export enum GoogleWalletCardNetwork {
  AMEX = 1,
  DISCOVER = 2,
  MASTERCARD = 3,
  VISA = 4,
  INTERAC = 5,
  PRIVATE_LABEL = 6,
  EFTPOS = 7,
  MAESTRO = 8,
  ID = 9,
  QUICPAY = 10,
  JCB = 11,
  ELO = 12,
  MIR = 13,
}

export enum GoogleWalletTokenProvider {
  AMEX = 2,
  MASTERCARD = 3,
  VISA = 4,
  DISCOVER = 5,
  EFTPOS = 6,
  INTERAC = 7,
  OBERTHUR = 8,
  PAYPAL = 9,
  JCB = 13,
  ELO = 14,
  GEMALTO = 15,
  MIR = 16,
}
export enum GoogleWalletTokenState {
  /**
   * This state is visible in the SDK but is not possible for push
   * provisioning. You can safely ignore this state.
   */
  UNTOKENIZED = 1,
  /**
   * The token is not currently available for payments,
   * but will be after some time.
   */
  PENDING = 2,
  /**
   * The token is in the active wallet, but requires additional user
   * authentication for use (yellow path step-up).
   */
  NEEDS_IDENTITY_VERIFICATION = 3,
  /**
   * The token has been temporarily suspended.
   */
  SUSPENDED = 4,
  /**
   * The token is active and available for payments.
   */
  ACTIVE = 5,
  /**
   * Token has been issued by TSP but Felica provisioning has not been completed.
   */
  FELICA_PENDING_PROVISIONING = 6,
}
