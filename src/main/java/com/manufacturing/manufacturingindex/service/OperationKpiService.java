package com.manufacturing.manufacturingindex.service;

import org.springframework.stereotype.Service;

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
        r.setMonthRef(dto.getMonthRef());
        r.setKpiValue(value.doubleValue());

        repo.save(r);
    }
}
