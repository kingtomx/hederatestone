package org.mvp.hedera.account.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Any;
import com.hedera.hashgraph.sdk.*;
import org.bson.Document;
import org.mvp.hedera.conf.HederaConfiguration;
import org.mvp.hedera.repository.ReactiveBusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

@Component
public class AccountManagement {

    @Autowired
    HederaConfiguration hederaConfiguration;

    @Autowired
    ReactiveBusinessRepository reactiveBusinessRepository;

    @Autowired
    ObjectMapper objectMapper;

    public Mono<Document> executeCreateAccount() throws PrecheckStatusException, TimeoutException {

        Client client = hederaConfiguration.hederaClient();


        PrivateKey newAccountPrivateKey = hederaConfiguration.hederaPrivateKey().generate();
        PublicKey newAccountPublicKey = hederaConfiguration.hederaPrivateKey().getPublicKey();

        TransactionResponse newAccount = new AccountCreateTransaction()
                .setKey(newAccountPublicKey)
                .setInitialBalance( Hbar.fromTinybars(1000))
                .execute(client);

        // Get the new account ID

        return Mono.fromCallable(() -> {
            return newAccount.getReceipt(client).accountId;
        }).flatMap(accountId -> {

            try {
                TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
                HashMap<String, Object> accountMap = objectMapper.readValue(objectMapper.writeValueAsString(accountId), typeRef);
                return reactiveBusinessRepository.create("account", accountMap);
            } catch (JsonProcessingException e) {
                return Mono.error(e);
            }

        });



    }


    public Mono<AccountBalance> getAccountBalance(Client client, AccountId accountId) throws PrecheckStatusException, TimeoutException {
        //Log the account ID
        System.out.println("The new account ID is: " +accountId);

        //Check the new account's balance
         return Mono.just(new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(client));

    }


    public Mono<TransactionResponse> deleteAccount(Client client, AccountId accountId) throws PrecheckStatusException, TimeoutException {
        return Mono.just(new AccountDeleteTransaction()
                .setAccountId(accountId)
                .execute(client));

    }




}
