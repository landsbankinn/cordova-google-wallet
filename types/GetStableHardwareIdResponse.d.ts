export type GetStableHardwareIdResponse =
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
    };
