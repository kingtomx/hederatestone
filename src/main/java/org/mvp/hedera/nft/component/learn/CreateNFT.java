package org.mvp.hedera.nft.component.learn;

import com.hedera.hashgraph.sdk.*;
import org.mvp.hedera.account.component.AccountManagement;
import org.mvp.hedera.conf.HederaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeoutException;

@Component
public class CreateNFT {

    @Autowired
    HederaConfiguration exampleConfiguration;

    @Autowired
    AccountManagement accountManagement;

    public Mono<TokenId> runCreation() {



          return accountManagement.executeCreateAccount(1000)
                .doOnError(TimeoutException.class, error1 -> {})
                .doOnError(PrecheckStatusException.class, error1 -> {})
                .map(doc -> {
                    Client client = exampleConfiguration.hederaClient();
                    PrivateKey supplyKey = exampleConfiguration.hederaPrivateKey().generate();
                    TokenCreateTransaction nftCreate = new TokenCreateTransaction()
                            .setTokenName("diploma")
                            .setTokenSymbol("GRAD")
                            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                            .setDecimals(0)
                            .setInitialSupply(0)
                            .setTreasuryAccountId(AccountId.fromString("")) //treasuryAccountId
                            .setSupplyType(TokenSupplyType.INFINITE)
                            .setMaxSupply(250)
                            .setSupplyKey(supplyKey)
                            .freezeWith(client);
                    //Sign the transaction with the treasury key
                    TokenCreateTransaction nftCreateTxSign = nftCreate.sign(exampleConfiguration.hederaPrivateKey());

                    //Submit the transaction to a Hedera network
                    TransactionResponse nftCreateSubmit = null;
                    try {
                        nftCreateSubmit = nftCreateTxSign.execute(client);
                        TransactionReceipt nftCreateRx = nftCreateSubmit.getReceipt(client);
                        return nftCreateRx.tokenId;
                    } catch (TimeoutException e) {
                        return TokenId.fromString("");
                    } catch (PrecheckStatusException e) {
                        return TokenId.fromString("");
                    } catch (ReceiptStatusException e) {
                        return TokenId.fromString("");
                    }


                });


        }

        @PostConstruct
        public void runNFT() throws ReceiptStatusException, PrecheckStatusException, TimeoutException {
            runCreation();
        }



}
