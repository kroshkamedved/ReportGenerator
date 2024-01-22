package com.en.reportgenerator.dto;

import com.en.reportgenerator.domain.Compound;
import com.en.reportgenerator.domain.Experiment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class ReportDTO {
    private final Experiment experiment;
    private final List<Compound> reactants;
    private final List<Compound> reagents;
    private final List<Compound> products;
}
