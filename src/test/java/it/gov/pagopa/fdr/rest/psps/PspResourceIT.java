package it.gov.pagopa.fdr.rest.psps;

import io.quarkus.test.junit.QuarkusIntegrationTest;

@QuarkusIntegrationTest
public class PspResourceIT extends PspResourceTest {
  // Execute the same tests but in packaged mode.

  @Override
  public void setup() {}
}
