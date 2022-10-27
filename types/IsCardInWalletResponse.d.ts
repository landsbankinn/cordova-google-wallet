type IsCardInWalletResponse =
  | {
      type: "result";
      result: "token found" | "token not found";
    }
  | {
      type: "error";
      statusCode: GoogleWalletStatusCodes;
      message: string;
    };
