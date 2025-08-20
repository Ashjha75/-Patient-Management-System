package com.patientmanagement.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Slf4j
@Service
public class BillingServiceGrpcClient {

    public final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;

    public BillingServiceGrpcClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @Value("${billing.service.port:9091}") int serverPort) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();
        this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);

        log.info("GRPC Server Info {}:{}", serverAddress, serverPort);

    }

    public BillingResponse createBillingAccount(String patientId, String name, String email) {

        BillingRequest request = BillingRequest.newBuilder().setName(name).setEmail(email).build();

        BillingResponse response = blockingStub.createBillingAccount(request);

        log.info("GRPC Response {}", response);
        return response;
    }
}