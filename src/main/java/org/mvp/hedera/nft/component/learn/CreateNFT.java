package org.mvp.hedera.nft.component.learn;

import com.hedera.hashgraph.sdk.*;
import org.bson.Document;
import org.mvp.hedera.account.component.AccountManagement;
import org.mvp.hedera.conf.HederaConfiguration;
import org.mvp.hedera.token.component.TokenManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

@Component
public class CreateNFT {

    @Autowired
    HederaConfiguration exampleConfiguration;

    @Autowired
    AccountManagement accountManagement;

    @Autowired
    TokenManagement tokenManagement;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Mono<Document> runCreation(String tokenName, String tokenSymbol, long initialSupply, long maxSupply) {

          return accountManagement.executeCreateAccount(1000)
                .doOnError(TimeoutException.class, error1 -> {})
                .doOnError(PrecheckStatusException.class, error1 -> {})
                .map(doc -> {

                    String accountId_string = doc.getInteger("shard").toString() + "." + doc.getInteger("realm").toString() + "." + doc.getInteger("num").toString();

                    Client client = exampleConfiguration.hederaClient();
                    PrivateKey supplyKey = exampleConfiguration.hederaPrivateKey().generate();
                    TokenCreateTransaction nftCreate = new TokenCreateTransaction()
                            .setTokenName(tokenName)
                            .setTokenSymbol(tokenSymbol)
                            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                            .setDecimals(0)
                            .setInitialSupply(initialSupply)
                            .setTreasuryAccountId(AccountId.fromString(accountId_string)) //treasuryAccountId
                            .setSupplyType(TokenSupplyType.FINITE)
                            .setMaxSupply(maxSupply)
                            .setSupplyKey(supplyKey)
                            .freezeWith(client);
                    //Sign the transaction with the treasury key
                    TokenCreateTransaction nftCreateTxSign = nftCreate.sign(exampleConfiguration.hederaPrivateKey());

                    //Submit the transaction to a Hedera network
                    try {
                        TransactionResponse nftCreateSubmit = nftCreateTxSign.execute(client);
                        TransactionReceipt nftCreateRx = nftCreateSubmit.getReceipt(client);
                        TokenId tokenId =  nftCreateRx.tokenId;
                        return Tuples.of(tokenId, accountId_string);
                    } catch (TimeoutException e) {
                        logger.error("TimeoutException: ", e.getMessage());
                        return Tuples.of(TokenId.fromString(""), e.getMessage());
                    } catch (PrecheckStatusException e) {
                        logger.error("PrecheckStatusException: ", e.getMessage());
                        return Tuples.of(TokenId.fromString(""), e.getMessage());
                    } catch (ReceiptStatusException e) {
                        logger.error("ReceiptStatusException: ", e.getMessage());
                        return Tuples.of(TokenId.fromString(""), e.getMessage());
                    }

                }).flatMap(tuple -> {
                      return tokenManagement.executeCreateToken(tuple.getT1(), tuple.getT2());
                });


        }

        @PostConstruct
        public void runNFT() {

            //Hooks.onOperatorDebug();

            runCreation("diploma", "GRAD", 0, 10)
                    .subscribe(tokenDocument -> {
                        logger.info(" TokenId: {}", tokenDocument.toString());
                    }, error -> {
                        logger.error("Error: ", error);
                    });

        }



}
