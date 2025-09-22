package com.patientmanagement.patientservice.serviceImplementation;

import com.patientmanagement.patientservice.dto.ExternalProductDTO;
import com.patientmanagement.patientservice.service.ExternalApiService;
import com.patientmanagement.patientservice.util.FakeStoreApiClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of {@link ExternalApiService} that uses {@link FakeStoreApiClient}.
 */
@Service
public class ExternalApiServiceImpl implements ExternalApiService {

    private final FakeStoreApiClient fakeStoreApiClient;

    /**
     * Constructs ExternalApiServiceImpl with the provided FakeStoreApiClient.
     *
     * @param fakeStoreApiClient the client used to call the external FakeStore API
     */
    public ExternalApiServiceImpl(FakeStoreApiClient fakeStoreApiClient) {
        this.fakeStoreApiClient = fakeStoreApiClient;
    }

    @Override
    public Mono<ExternalProductDTO> getProductById(Long id) {
        return fakeStoreApiClient.getProductById(id);
    }
}
