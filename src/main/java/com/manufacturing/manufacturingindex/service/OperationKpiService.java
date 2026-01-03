package com.manufacturing.manufacturingindex.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.manufacturing.manufacturingindex.dto.OperationKpiDTO;
import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiRecord;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.repository.KpiRecordRepository;

@Service
public class OperationKpiService {

    private final KpiRecordRepository repo;
    private final FactoryRepository factoryRepo;

    public OperationKpiService(KpiRecordRepository repo,
                               FactoryRepository factoryRepo) {
        this.repo = repo;
        this.factoryRepo = factoryRepo;
    }

    // =========================
    // SAVE (mantido) + monthRef obrigatório
    // =========================
    @Transactional
    public void save(Long factoryId, OperationKpiDTO dto) {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        saveMetric(factory, dto, "PAIRS_PRODUCED", dto.getPairsProduced());
        saveMetric(factory, dto, "WORKING_DAYS", dto.getWorkingDays());
        saveMetric(factory, dto, "WORKFORCE_NIKE", dto.getWorkforceNike());
        saveMetric(factory, dto, "PPH", dto.getPph());
        saveMetric(factory, dto, "DR", dto.getDr());
    }

    private void saveMetric(Factory factory,
                            OperationKpiDTO dto,
                            String metric,
                            Number value) {

        if (value == null) return;

        KpiRecord r = new KpiRecord();
        r.setFactory(factory);
        r.setType("OPERATION");
        r.setMetric(metric);
        r.setFy(dto.getFy());
        r.setQuarter(dto.getQuarter());

        // ✅ monthRef obrigatório no seu model -> garante valor
        String monthRef = normalizeMonthRef(dto.getMonthRef());
        r.setMonthRef(monthRef);

        r.setKpiValue(value.doubleValue());

        repo.save(r);
    }

    // =========================
    // READ – ONE PAGER (Quarter)
    // =========================
    public OperationKpiDTO getOperationKpi(Long factoryId,
                                           String fy,
                                           String quarter) {
        return getOperationKpi(factoryId, fy, quarter, null);
    }

    // =========================
    // READ – ONE PAGER (Month opcional)
    // =========================
    public OperationKpiDTO getOperationKpi(Long factoryId,
                                           String fy,
                                           String quarter,
                                           String monthRef) {

        OperationKpiDTO dto = new OperationKpiDTO();

        dto.setPairsProduced(
                repo.sumMetricWithMonth(factoryId, "PAIRS_PRODUCED", fy, quarter, monthRef)
        );

        dto.setWorkingDays(
                repo.sumMetricWithMonth(factoryId, "WORKING_DAYS", fy, quarter, monthRef)
        );

        dto.setWorkforceNike(
                repo.avgMetricWithMonth(factoryId, "WORKFORCE_NIKE", fy, quarter, monthRef)
        );

        dto.setPph(
                repo.avgMetricWithMonth(factoryId, "PPH", fy, quarter, monthRef)
        );

        dto.setDr(
                repo.avgMetricWithMonth(factoryId, "DR", fy, quarter, monthRef)
        );

        return dto;
    }

    // ============================================================
    // ✅ NOVO: suporte para tabela e EDIT/DELETE do Operation KPI
    // (não mexe em OnePager, só adiciona funções usadas no CRUD)
    // ============================================================

    /**
     * Tabela "KPIs already registered for this Factory"
     * (uma linha por FY+Quarter+MonthRef)
     */
    public List<Object[]> listOperationTable(Long factoryId) {
        return repo.listOperationTable(factoryId);
    }

    /**
     * Carrega o conjunto OPERATION do mês (para editar)
     */
    public List<KpiRecord> findOperationSet(Long factoryId,
                                            String fy,
                                            String quarter,
                                            String monthRef) {
        return repo.findOperationSet(factoryId, fy, quarter, normalizeMonthRef(monthRef));
    }

    /**
     * Deleta o conjunto OPERATION inteiro (FY+Quarter+MonthRef)
     */
    @Transactional
    public int deleteOperationSet(Long factoryId,
                                  String fy,
                                  String quarter,
                                  String monthRef) {
        return repo.deleteOperationSet(factoryId, fy, quarter, normalizeMonthRef(monthRef));
    }

    // ============================================================
    // AUX
    // ============================================================
    private String normalizeMonthRef(String monthRef) {
        if (monthRef == null) return "ALL";
        String m = monthRef.trim();
        if (m.isEmpty()) return "ALL";
        return m.toUpperCase(); // teu print mostra JUN/JUL/AUG
    }
}
