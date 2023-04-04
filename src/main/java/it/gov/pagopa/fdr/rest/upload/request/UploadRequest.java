package it.gov.pagopa.fdr.rest.upload.request;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@SuperBuilder
@Jacksonized
@Schema(format = "json")
public class UploadRequest extends AbstractUpload {}
