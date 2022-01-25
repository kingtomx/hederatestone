package org.mvp.hedera.transaction.component.learn;

import com.hedera.hashgraph.sdk.*;
import org.mvp.hedera.conf.HederaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class CreateTransaction {

    @Autowired
    HederaConfiguration exampleConfiguration;

    public void executeCreateAccount() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        Client client = exampleConfiguration.hederaClient();

        PrivateKey newAccountPrivateKey = exampleConfiguration.hederaPrivateKey().generate();
        PublicKey newAccountPublicKey = exampleConfiguration.hederaPrivateKey().getPublicKey();

        TransactionResponse newAccount = new AccountCreateTransaction()
                .setKey(newAccountPublicKey)
                .setInitialBalance( Hbar.fromTinybars(1000))
                .execute(client);

        // Get the new account ID
        AccountId newAccountId = newAccount.getReceipt(client).accountId;

        //Log the account ID
        System.out.println("The new account ID is: " +newAccountId);

        //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .execute(client);

        System.out.println("The new account balance is: " +accountBalance.hbars);


        //Transfer hbar
        TransactionResponse sendHbar = new TransferTransaction()
                .addHbarTransfer(newAccountId, Hbar.fromTinybars(-1000)) //Sending account
                .addHbarTransfer(newAccountId, Hbar.fromTinybars(1000)) //Receiving account
                .execute(client);

        System.out.println("The transfer transaction was: " +sendHbar.getReceipt(client).status);

        //Request the cost of the query
        Hbar queryCost = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .getCost(client);

        System.out.println("The cost of this query is: " +queryCost);

        //Check the new account's balance
        AccountBalance accountBalanceNew = new AccountBalanceQuery()
                .setAccountId(newAccountId)
                .execute(client);

        System.out.println("The new account balance is: " +accountBalanceNew.hbars);

    }

}
