package com.patientmanagement.patientservice.awsService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient;

    @Value("${aws.ses.sender}")
    private String sender;

    public void sendEmail(String to, String subject, String htmlBody) {
        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        Content subjContent = Content.builder().data(subject).build();
        Content htmlContent = Content.builder().data(htmlBody).build();
        Body body = Body.builder().html(htmlContent).build();

        Message message = Message.builder()
                .subject(subjContent)
                .body(body)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(sender)
                .build();

        sesClient.sendEmail(request);
    }
}
