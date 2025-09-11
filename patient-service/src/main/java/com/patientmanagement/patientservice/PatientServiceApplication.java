package com.patientmanagement.patientservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration.class
})
@EnableScheduling
@EnableCaching
public class PatientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatientServiceApplication.class, args);
        System.out.println("Patient Service Application is running...🎉✨");
    }

}
