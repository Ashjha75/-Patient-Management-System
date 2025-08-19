package com.patientmanagement.billerservice.grpc;


import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    @Override
    public void createBillingAccount(billing.BillingRequest billingRequest, StreamObserver<billing.BillingResponse> billingReponse) {

        log.info("createBillingAccount request for {}", billingRequest.toString());

        BillingResponse billingResponse = BillingResponse.newBuilder().setAccountID("12345").setStatus("ACTIVE").build();

        billingReponse.onNext(billingResponse);
        billingReponse.onCompleted();

    }
}
