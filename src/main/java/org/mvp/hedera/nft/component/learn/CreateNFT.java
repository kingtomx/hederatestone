package org.mvp.hedera.nft.component.learn;

import com.hedera.hashgraph.sdk.*;
import org.mvp.hedera.conf.HederaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeoutException;

@Component
public class CreateNFT {

    @Autowired
    HederaConfiguration exampleConfiguration;


    public void runCreation() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {


        Client client = exampleConfiguration.hederaClient();

        // TREASURY ---------------------------------
        PrivateKey treasuryKey = exampleConfiguration.hederaPrivateKey().generate();
        PublicKey treasuryPublicKey = treasuryKey.getPublicKey();
        TransactionResponse treasuryAccount = new AccountCreateTransaction()
                .setKey(treasuryPublicKey)
                .setInitialBalance(new Hbar(1000))
                .execute(client);
        AccountId treasuryId = treasuryAccount.getReceipt(client).accountId;

        // SUPPLY ---------------------------------
        PrivateKey supplyKey = exampleConfiguration.hederaPrivateKey().generate();



        //Create the NFT
            TokenCreateTransaction nftCreate = new TokenCreateTransaction()
                    .setTokenName("diploma")
                    .setTokenSymbol("GRAD")
                    .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                    .setDecimals(0)
                    .setInitialSupply(0)
                    .setTreasuryAccountId(treasuryId) //treasuryAccountId
                    .setSupplyType(TokenSupplyType.INFINITE)
                    .setMaxSupply(250)
                    .setSupplyKey(supplyKey)
                    .freezeWith(client);


            //Sign the transaction with the treasury key
            TokenCreateTransaction nftCreateTxSign = nftCreate.sign(exampleConfiguration.hederaPrivateKey());

            //Submit the transaction to a Hedera network
            TransactionResponse nftCreateSubmit = nftCreateTxSign.execute(client);

            //Get the transaction receipt
            TransactionReceipt nftCreateRx = nftCreateSubmit.getReceipt(client);

            //Get the token ID
            TokenId tokenId = nftCreateRx.tokenId;

            //Log the token ID
            System.out.println("Created NFT with token ID " +tokenId);
        }

        @PostConstruct
        public void runNFT() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
            runCreation();
        }



}
