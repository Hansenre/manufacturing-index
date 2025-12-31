package com.manufacturing.manufacturingindex.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.manufacturing.manufacturingindex.dto.DrTopTypeDTO;
import com.manufacturing.manufacturingindex.dto.HfpiDTO;
import com.manufacturing.manufacturingindex.dto.OnePagerSummaryDTO;
import com.manufacturing.manufacturingindex.dto.OperationKpiDTO;
import com.manufacturing.manufacturingindex.model.OnePagerSummary;
import com.manufacturing.manufacturingindex.repository.DefectTypeRepository;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;
import com.manufacturing.manufacturingindex.repository.OnePagerSummaryRepository;

@Service
public class OnePagerService {

    private final KpiRecordRepository kpiRepo;
    private final HfpiEventRepository hfpiRepo;
    private final DefectTypeRepository defectRepo;
    private final OnePagerSummaryRepository summaryRepo;

    public OnePagerService(KpiRecordRepository kpiRepo,
                           HfpiEventRepository hfpiRepo,
                           DefectTypeRepository defectRepo,
                           OnePagerSummaryRepository summaryRepo) {

        this.kpiRepo = kpiRepo;
        this.hfpiRepo = hfpiRepo;
        this.defectRepo = defectRepo;
        this.summaryRepo = summaryRepo;
    }

    /* =====================
       OPERATION KPI
    ===================== */
    public OperationKpiDTO getOperationKpi(Long factoryId) {

        Object[] row = kpiRepo.calculateOperationKpi(factoryId);

        OperationKpiDTO dto = new OperationKpiDTO();

        if (row == null || row.length != 5) {
            return dto;
        }

        dto.setPairsProduced(row[0] != null ? ((Number) row[0]).intValue() : 0);
        dto.setWorkingDays(row[1] != null ? ((Number) row[1]).intValue() : 0);
        dto.setWorkforceNike(row[2] != null ? ((Number) row[2]).intValue() : 0);
        dto.setPph(row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
        dto.setDr(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);

        return dto;
    }

    /* =====================
       EXECUTIVE SUMMARY
    ===================== */
    public OnePagerSummaryDTO getSummary(Long factoryId) {

        OnePagerSummary entity = summaryRepo.findByFactoryId(factoryId);

        if (entity == null) {
            return null; // view já trata com th:if
        }

        OnePagerSummaryDTO dto = new OnePagerSummaryDTO();
        dto.setHighlight(entity.getHighlight());
        dto.setKeyAction(entity.getKeyAction());
        dto.setHelpNeeded(entity.getHelpNeeded());

        return dto;
    }

    /* =====================
       HFPI
    ===================== */
    public HfpiDTO getHfpi(Long factoryId) {

        HfpiDTO dto = new HfpiDTO();

        // Depois você liga isso com os cálculos reais
        dto.setHfpiGeral(0.0);
        dto.setHfpiFabrica(0.0);
        dto.setHfpiOnline(0.0);

        return dto;
    }

    /* =====================
       DR – TOP TYPES
    ===================== */
    public List<DrTopTypeDTO> getDrTopTypes(Long factoryId) {

        return defectRepo.findTopDefects(factoryId)
                .stream()
                .map(row -> new DrTopTypeDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }
}
