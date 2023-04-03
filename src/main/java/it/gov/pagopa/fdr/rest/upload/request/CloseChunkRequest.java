package it.gov.pagopa.fdr.rest.upload.request;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class CloseChunkRequest extends AbstractUpload {}
