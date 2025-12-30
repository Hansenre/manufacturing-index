package com.manufacturing.manufacturingindex.config;

import org.springframework.stereotype.Service;

import com.manufacturing.manufacturingindex.dto.HfpiDashboardDTO;
import com.manufacturing.manufacturingindex.repository.HfpiEventRepository;
import com.manufacturing.manufacturingindex.repository.HfpiFactoryRepository;

@Service
public class HfpiDashboardService {

    private final HfpiEventRepository eventRepo;
    private final HfpiFactoryRepository factoryRepo;

    public HfpiDashboardService(
            HfpiEventRepository eventRepo,
            HfpiFactoryRepository factoryRepo) {
        this.eventRepo = eventRepo;
        this.factoryRepo = factoryRepo;
    }

    public HfpiDashboardDTO build(Long factoryId, String fy, String quarter) {

        System.out.println(">>> DASHBOARD BUILD");
        System.out.println("factoryId=" + factoryId);
        System.out.println("fy=" + fy);
        System.out.println("quarter=" + quarter);

        /* ===============================
           HFPI ONLINE
           =============================== */
        Object[] onlineWrapper =
                eventRepo.getHfpiOnlinePercent(factoryId, fy, quarter);

        double hfpiOnline = 0.0;

        if (onlineWrapper != null
                && onlineWrapper.length > 0
                && onlineWrapper[0] instanceof Object[]) {

            Object[] onlineRow = (Object[]) onlineWrapper[0];

            if (onlineRow.length >= 3 && onlineRow[2] != null) {
                hfpiOnline = ((Number) onlineRow[2]).doubleValue() / 100.0;
            }
        }

        /* ===============================
           HFPI FACTORY
           =============================== */
        Object[] factoryWrapper =
                factoryRepo.getHfpiFactoryPercent(factoryId, fy, quarter);

        double hfpiFactory = 0.0;

        if (factoryWrapper != null
                && factoryWrapper.length > 0
                && factoryWrapper[0] instanceof Object[]) {

            Object[] factoryRow = (Object[]) factoryWrapper[0];

            if (factoryRow.length >= 3 && factoryRow[2] != null) {
                hfpiFactory = ((Number) factoryRow[2]).doubleValue() / 100.0;
            }
        }

        /* ===============================
           HFPI FINAL — 60 / 40
           =============================== */
        double hfpiFinal =
                (hfpiOnline * 0.6) + (hfpiFactory * 0.4);

        System.out.println("HFPI ONLINE = " + hfpiOnline);
        System.out.println("HFPI FACTORY = " + hfpiFactory);
        System.out.println("HFPI FINAL = " + hfpiFinal);

        return new HfpiDashboardDTO(
                hfpiOnline,
                hfpiFactory,
                hfpiFinal
        );
    }
}
