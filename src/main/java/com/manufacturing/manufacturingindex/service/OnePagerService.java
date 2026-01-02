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
import com.manufacturing.manufacturingindex.repository.OnePagerSummaryRepository;

@Service
public class OnePagerService {

    private final OperationKpiService operationKpiService;
    private final HfpiEventRepository hfpiRepo;
    private final DefectTypeRepository defectRepo;
    private final OnePagerSummaryRepository summaryRepo;

    public OnePagerService(OperationKpiService operationKpiService,
                           HfpiEventRepository hfpiRepo,
                           DefectTypeRepository defectRepo,
                           OnePagerSummaryRepository summaryRepo) {

        this.operationKpiService = operationKpiService;
        this.hfpiRepo = hfpiRepo;
        this.defectRepo = defectRepo;
        this.summaryRepo = summaryRepo;
    }

    /* =====================
       OPERATION KPI (com month opcional)
       ===================== */
    public OperationKpiDTO getOperationKpi(Long factoryId,
                                           String fy,
                                           String quarter,
                                           String monthRef) {

        return operationKpiService.getOperationKpi(factoryId, fy, quarter, monthRef);
    }

    /* =====================
       EXECUTIVE SUMMARY
       ===================== */
    public OnePagerSummaryDTO getSummary(Long factoryId,
                                         String fy,
                                         String quarter) {

        OnePagerSummary entity =
                summaryRepo.findByFactoryIdAndFyAndQuarter(factoryId, fy, quarter);

        if (entity == null) return null;

        OnePagerSummaryDTO dto = new OnePagerSummaryDTO();
        dto.setHighlight(entity.getHighlight());
        dto.setKeyAction(entity.getKeyAction());
        dto.setHelpNeeded(entity.getHelpNeeded());

        return dto;
    }

    /* =====================
       HFPI (placeholder)
       ===================== */
    public HfpiDTO getHfpi(Long factoryId,
                           String fy,
                           String quarter) {

        HfpiDTO dto = new HfpiDTO();
        dto.setHfpiGeral(0.0);
        dto.setHfpiFabrica(0.0);
        dto.setHfpiOnline(0.0);
        return dto;
    }

    /* =====================
       DR – TOP TYPES
       ===================== */
    public List<DrTopTypeDTO> getDrTopTypes(Long factoryId,
                                           String fy,
                                           String quarter) {

        return defectRepo.findTopDefects(factoryId, fy, quarter)
                .stream()
                .map(row -> new DrTopTypeDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    /* =====================
       VOC – placeholder
       ===================== */
    public List<?> getVoc(Long factoryId,
                          String fy,
                          String quarter) {
        return List.of();
    }
}
