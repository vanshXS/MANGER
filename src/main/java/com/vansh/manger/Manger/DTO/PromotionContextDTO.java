package com.vansh.manger.Manger.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PromotionContextDTO {

    private boolean canPromote;
    private AcademicYearSummary currentAcademicYear;
    private AcademicYearSummary closedAcademicYear;
    private String message;

    @Data
    @Builder
    public static class AcademicYearSummary {
        private Long id;
        private String name;
    }
}
