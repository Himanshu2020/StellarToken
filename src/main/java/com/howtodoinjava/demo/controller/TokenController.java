package com.howtodoinjava.demo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;

@RestController
public class TokenController {

    @RequestMapping("/createAccount")
    public HashMap<String, String> createAccount() {

        KeyPair pair = KeyPair.random();
        HashMap<String, String> account = new HashMap<>();

        account.put("Public Key", pair.getAccountId());
        account.put("Secret Key", new String(pair.getSecretSeed()));
        return account;
    }

    @RequestMapping("/addTestAccountbalance")
    public String addTestAccountbalance() {

        String accountID = "GBE72FGMDBRQ3I4SW3WRYL6CI4CO3Y4AQHKQDELGTZ4NDZMURBT36YQG";

        String friendbotUrl = String.format(
                "https://friendbot.stellar.org/?addr=%s",
                accountID);

        InputStream response = null;
        try {
            response = new URL(friendbotUrl).openStream();
        } catch (Exception e) {

        }
        String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
        return body;
    }

    @RequestMapping("/getAccountDetails")
    public HashMap<String, String> getAccountDetails() {


        String accountID = "GC42DVONQAYPFH7LOOQ77IS4UUMWNB7H3VVS3HHMWHWC4EP2A4QO5QI3";

        KeyPair pair = KeyPair.fromAccountId(accountID);
        HashMap<String, String> accountdata = new HashMap<>();

        //    accountdata.put("Secret Key", new String(pair.getSecretSeed()));

        AccountResponse account = null;
        Server server = new Server("https://horizon-testnet.stellar.org");

        try {
            account = server.accounts().account(pair);
        } catch (IOException e) {

        }

        System.out.println("Balances for account " + pair.getAccountId());
        for (AccountResponse.Balance balance : account.getBalances()) {
   /*         System.out.println(String.format(
                    "Type: %s, Code: %s, Balance: %s",
                    balance.getAssetType(),
                    balance.getAssetCode(),
                    balance.getBalance()));*/


            accountdata.put("AssetType", new String(balance.getAssetType()));
//            accountdata.put("AssetCode", new String(balance.getAssetCode()));
            accountdata.put("Balance", new String(balance.getBalance()));
        }

        accountdata.put("ACCESS CODE",account.getBalances()[0].getAssetCode()+"");
        accountdata.put("aa",account.getBalances()[0].getBalance()+"");
        return accountdata;
    }

    @RequestMapping("/addTrust")
    public HashMap<String, String> addTrust() {



        String ISSURE_ACCOUNT = "";

        KeyPair pair = KeyPair.random();
        HashMap<String, String> account = new HashMap<>();

       // account.put("Public Key", pair.getAccountId());
        accountsTrustlines();
        account.put("Secret Key", new String("Adding Trust ......"));
        return account;
    }


    @RequestMapping("/makePayment")
    public HashMap<String, String> makePayment() {

        KeyPair pair = KeyPair.random();
        HashMap<String, String> account = new HashMap<>();

        account.put("Public Key", pair.getAccountId());
        account.put("Secret Key", new String(pair.getSecretSeed()));
        return account;
    }


    public void accountsTrustlines() {

        // First, the receiving account must trust the asset


        KeyPair IssuingAccountKey = KeyPair.fromAccountId("GAU5YQ25X627A6QEMTYVJ6BBM6AU3GK4ZYG3SSQKDYSJAXNHOYL277AD");

        KeyPair receivingKeys = KeyPair.fromAccountId("GCLZ23FA3I2MFEECT6IFASTRA2O4TASUDVINPZ56XAGMFJJR4M3AFNAJ");

        Asset newToken = Asset.create("credit_alphanum4","IGO","GAU5YQ25X627A6QEMTYVJ6BBM6AU3GK4ZYG3SSQKDYSJAXNHOYL277AD");

        AccountResponse receivingAccount;

        Server server;
     Network.useTestNetwork();
        server = new Server("https://horizon-testnet.stellar.org");

        try {

            receivingAccount = server.accounts().account(receivingKeys);
            Transaction allowNewToken = new Transaction.Builder(receivingAccount)
                    .addOperation(
                            // The `ChangeTrust` operation creates (or alters) a trustline
                            // The second parameter limits the amount the account can hold
                            new ChangeTrustOperation.Builder(newToken, "100000").build())
                    .build();
            allowNewToken.sign(receivingKeys);
            // 2: HOW DO YOU REMOVE THE LIMIT? HOW CAN A RECEIVE HOLD AS MANY AS WANTED? THERE IS NO DOCS AND "" OR NULL DOESN'T WORK

            server.submitTransaction(allowNewToken);

        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            System.out.println("Trust line created");
        }
    }
}
