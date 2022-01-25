package org.mvp.hedera.client.component.learn;

import com.hedera.hashgraph.sdk.Client;
import org.mvp.hedera.conf.HederaConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExampleClient {

    @Autowired
    HederaConfiguration exampleConfiguration;

    public void runClient() {
        Client client = exampleConfiguration.hederaClient();

    }

}
