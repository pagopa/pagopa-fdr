package it.gov.pagopa.fdr.service.re;

import io.quarkus.test.junit.QuarkusTest;
import it.gov.pagopa.fdr.service.re.model.*;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;

@QuarkusTest
public class ReServiceTest {

    @Inject
    ReService reService;

    @BeforeEach
    public void setup() {
        ReService mock = Mockito.mock(ReService.class);
        Mockito.doNothing().when(mock).sendEvent();
    }

    @Test
    public void testSend() {
        ReInterface reInterface =
                ReInterface.builder()
                    .appVersion(AppVersionEnum.FDR003)
                    .created(Instant.now())
                    .sessionId("sessionId")
                    .eventType(EventTypeEnum.INTERFACE)
                    .httpType(HttpTypeEnum.RES)
                    .httpMethod("GET")
                    .httpUrl("requestPath")
                    .payload("responsePayload")
                    .pspId("1")
                    .fdr("1")
                    .organizationId("1")
                    .fdrAction(FdrActionEnum.GET_FDR)
                    .build()
        ;

        reService.sendEvent(reInterface);
        Mockito.verify(reService, Mockito.times(1)).sendEvent(reInterface);
    }
}
