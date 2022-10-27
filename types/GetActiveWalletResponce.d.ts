type GetActiveWalletResponse =
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
    };
