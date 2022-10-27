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

type PushTokenizeResponse =
  | {
      type: "result";
      tokenId: string;
    }
  | {
      type: "canceled";
    };
