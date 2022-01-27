package org.mvp.hedera.account.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import org.bson.Document;
import org.mvp.hedera.account.component.AccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping(value = "/api/v1/account")
public class AccountController {

    @Autowired
    AccountManagement accountManagement;

    @RequestMapping(value="/{accountid}", method = RequestMethod.GET, produces="application/json")
    public ResponseEntity get(@PathVariable("accountid") String accountid) {
        return null;
    }

    @RequestMapping(value="/", method = RequestMethod.POST, headers = {"Content-type=application/json"})
    public Mono<Document> create() throws ReceiptStatusException, JsonProcessingException, PrecheckStatusException, TimeoutException {
        return accountManagement.executeCreateAccount(1000);
    }


    /*
        @RequestMapping(value="/processinstance/start/{process_name}/{business_key}", method = RequestMethod.POST, headers = {"Content-type=application/json"})
    public  ResponseEntity startProcessInstance(@PathVariable("process_name") String processName, @PathVariable("business_key") String businessKey, @RequestBody Variables payload)  {
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            processEngine.getRuntimeService().startProcessInstanceByKey(processName, businessKey, payload.toMap());
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Content-Type", "application/json");
            Map<String, String> returnable = new HashMap<String, String>();
            returnable.put("businessKey", businessKey);
            return ResponseEntity.ok().headers(responseHeaders).body(returnable);
        }catch (Exception ex) {
            logger.error(ex.getMessage());
            Map<String, String> error = new HashMap<String, String>();
            error.put("businessKey", businessKey);
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

        @RequestMapping(value="/processinstance/{task_id}", method = RequestMethod.GET, produces="application/json")
    public  ResponseEntity getProcessInstance(@PathVariable("task_id") String taskId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            Map<String, Object> variables = processEngine.getTaskService().getVariables(taskId);
            return ResponseEntity.status(HttpStatus.OK).body(variables);
        }catch (Exception ex) {
            logger.error(ex.getMessage());
            Map<String, String> error = new HashMap<String, String>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

     */


}
