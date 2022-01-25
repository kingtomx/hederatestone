package org.mvp.hedera.conf;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HederaConfiguration {

    @Bean
    public AccountId hederaAccountId() {
        return AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
    }

    @Bean
    public PrivateKey hederaPrivateKey() {
        return PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));
    }

    @Bean
    public Client hederaClient() {
        Client client = Client.forTestnet();
        client.setOperator(hederaAccountId(), hederaPrivateKey());
        return client;
    }

}
