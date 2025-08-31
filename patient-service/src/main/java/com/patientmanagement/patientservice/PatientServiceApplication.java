package com.patientmanagement.patientservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration.class
})
public class PatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
        System.out.println("Patient Service Application is running...🎉✨");
    }

}
