package it.gov.pagopa.fdr.service.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FdrAllCreatedDto {

  private MetadataDto metadata;

  private Long count;

  private List<FdrSimpleCreatedDto> data;
}
