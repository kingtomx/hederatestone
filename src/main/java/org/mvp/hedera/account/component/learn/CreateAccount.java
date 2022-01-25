package org.mvp.hedera.account.component.learn;

import com.hedera.hashgraph.sdk.*;
import org.mvp.hedera.conf.HederaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class CreateAccount {

    @Autowired
    HederaConfiguration exampleConfiguration;

    public void executeCreateAccount() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        Client client = Client.forTestnet();
        client.setOperator(exampleConfiguration.hederaAccountId(), exampleConfiguration.hederaPrivateKey());

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


    }

}
