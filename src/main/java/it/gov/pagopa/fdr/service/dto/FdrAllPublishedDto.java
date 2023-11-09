package it.gov.pagopa.fdr.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FdrAllPublishedDto {

  private MetadataDto metadata;

  private Long count;

  private List<FdrSimplePublishedDto> data;
}
