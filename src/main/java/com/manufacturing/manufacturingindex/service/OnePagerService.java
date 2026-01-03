package com.manufacturing.manufacturingindex.service;

import java.util.ArrayList;
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

        // monthRef pode vir "" (All Months)
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
       DR – TOP TYPES (✅ agora retorna DTO sempre)
       ===================== */
    public List<DrTopTypeDTO> getDrTopTypes(Long factoryId, String fy, String quarter) {

        // cada linha = [name, total, rate]
        List<Object[]> raw = defectRepo.getDrTopTypes(factoryId, fy, quarter);

        List<DrTopTypeDTO> list = new ArrayList<>();

        for (Object[] r : raw) {
            if (r == null || r.length == 0) continue;

            String name = (r[0] != null) ? r[0].toString() : "—";
            long total = (r.length > 1 && r[1] != null) ? ((Number) r[1]).longValue() : 0L;
            double rate = (r.length > 2 && r[2] != null) ? ((Number) r[2]).doubleValue() : 0.0;

            list.add(new DrTopTypeDTO(name, total, rate));
        }

        return list;
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
