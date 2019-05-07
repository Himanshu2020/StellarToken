package com.howtodoinjava.demo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.stellar.sdk.Asset;
import org.stellar.sdk.ChangeTrustOperation;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.responses.AccountResponse;

public class IssueAsset {

	KeyPair issuingKeys;
	
	 KeyPair receivingKeys;
	
	 Server server;
	 
	 Asset newToken;
	 
	 String fundIssuingAccountSt;
	 String fundReceivingAccountSt;
	
	public IssueAsset() {
		// TODO Auto-generated method stub
		Network.useTestNetwork();
		server = new Server("https://horizon-testnet.stellar.org");
		// create issuer key and receving key
		issuingKeys = KeyPair.random();
		receivingKeys = KeyPair.random();
		
		// print the private keys generated
		System.out.println("Secret Key issuer: "+new String (issuingKeys.getSecretSeed()));
		System.out.println("Secret Key receiver: "+new String (receivingKeys.getSecretSeed()));
		
		// print the public keys generated  
		System.out.println("AccountID issuer: "+issuingKeys.getAccountId());
		System.out.println("AccountID receiver: "+receivingKeys.getAccountId());

		// issuer creates astro dollars
		newToken = Asset.createNonNativeAsset("astroDollars", issuingKeys);
		// Q1: HOW DO YOU LIMIT THE NUMBER OF TOKEN ISSUED?


		System.out.println("New token astro dollars created: "+newToken.toString());
			
				
	}
	
	/**
	 * 
	 */
	public void fundAccounts() {
		fundIssuingAccountSt = String.format("https://horizon-testnet.stellar.org/friendbot?addr=%s", issuingKeys.getAccountId());
		fundReceivingAccountSt = String.format("https://horizon-testnet.stellar.org/friendbot?addr=%s", receivingKeys.getAccountId());
		
		InputStream response;
		String body;
		try {
			response = new URL (fundIssuingAccountSt).openStream();
			body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
			System.out.println("Issuing account funded: \n"+body); 
			response = new URL (fundReceivingAccountSt).openStream();
			body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
			System.out.println("Receiving account funded: \n"+body); 
		} catch (IOException e) {
			e.printStackTrace();
		} 		
		
	}
	
	/**
	 * trust to be created between the receiving account and the issuing account
	 * the receiving account has to trust the issuing account
	 */
	public void accountsTrustlines() {
		
		// First, the receiving account must trust the asset
		AccountResponse receivingAccount;
		try {
			
			receivingAccount = server.accounts().account(receivingKeys);
			Transaction allowNewToken = new Transaction.Builder(receivingAccount)
					.addOperation(
				    // The `ChangeTrust` operation creates (or alters) a trustline
				    // The second parameter limits the amount the account can hold
				    new ChangeTrustOperation.Builder(newToken, "1000").build())
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

	/**
	 * send 135 new token coins from the issuer account 
	 */
	public void sendToken() {
		// the issuing account actually sends a payment using the asset
		try {
			
			AccountResponse issuing = server.accounts().account(issuingKeys);
			Transaction sendNewToken = new Transaction.Builder(issuing)
										   .addOperation(
										    new PaymentOperation.Builder(receivingKeys, newToken, "135").build())
										   .build();
			sendNewToken.sign(issuingKeys);
			server.submitTransaction(sendNewToken);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			System.out.println("coins sent from new token created ");
		}
			
	}
	
	/**
	 * check the balances of the two accounts issuer and receiver
	 */
	public void checkBalances() {
		
		AccountResponse accountIssuer;
		AccountResponse accountReceiver;
		
		try {

			accountIssuer = server.accounts().account(KeyPair.fromAccountId(issuingKeys.getAccountId()));
			accountReceiver = server.accounts().account(KeyPair.fromAccountId(receivingKeys.getAccountId()));
			
			System.out.println("Balances for account issuer: " + issuingKeys.getAccountId() +" (PK)");
			
			for (AccountResponse.Balance balance : accountIssuer.getBalances()) {
			  System.out.println(String.format(
			    "Type: %s, Code: %s, Balance: %s",
			    balance.getAssetType(),
			    balance.getAssetCode(),
			    balance.getBalance()));
			}
			// Q3: HOW DO YOU CHECK THE BALANCE OF THE ISSUER? I ASSUMED THE ISSUER OWNS THE TOKENS GENERATED AND I WOULD SEE A BALANCE 
			//     INSTEAD THE BELOW DOESN'T SHOW IT
			// Q4: IS THE stellar.TML FILE COMPULSORY ? IS IT ONE FILE FOR ALL ISSUERS OR ONE PER ISSUER? I 
			//     CANNOT SEE THE NUMBER OF TOKEN ISSUED THERE EITHER
			System.out.println("Balances for account receiver: " + receivingKeys.getAccountId() +" (PK)");
			for (AccountResponse.Balance balance : accountReceiver.getBalances()) {
				  System.out.println(String.format(
				    "Type: %s, Code: %s, Balance: %s",
				    balance.getAssetType(),
				    balance.getAssetCode(),
				    balance.getBalance()));
				}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * main
	 */
	public static void main(String[] args) {
		
		// init
		IssueAsset ia = new IssueAsset();
		
		// fund the accounts
		ia.fundAccounts();
		
		// receiving account trusts the issuing account
		ia.accountsTrustlines();
		
		// issuing account sends coins from the new token
		ia.sendToken();
		
		// check the two balances
		ia.checkBalances();
		
	}

}



