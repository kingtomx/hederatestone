package org.mvp.hedera.token.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedera.hashgraph.sdk.*;
import org.bson.Document;
import org.mvp.hedera.conf.HederaConfiguration;
import org.mvp.hedera.repository.ReactiveBusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Service
public class TokenManagement {

    @Autowired
    HederaConfiguration hederaConfiguration;

    @Autowired
    ReactiveBusinessRepository reactiveBusinessRepository;

    @Autowired
    ObjectMapper objectMapper;

    public Mono<Document> executeCreateToken(TokenId tokenId, String accountId) {

        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
        try {
            HashMap<String, Object> tokenMap = objectMapper.readValue(objectMapper.writeValueAsString(tokenId), typeRef);
            tokenMap.put("accountId", accountId);
            return reactiveBusinessRepository.create("token", tokenMap);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

    }



}
