package com.manufacturing.manufacturingindex.service;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiQualityEvidence;
import com.manufacturing.manufacturingindex.model.KpiQualityEvidence.MetricKey;
import com.manufacturing.manufacturingindex.repository.KpiQualityEvidenceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class KpiQualityEvidenceService {

    private final KpiQualityEvidenceRepository repo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public KpiQualityEvidenceService(KpiQualityEvidenceRepository repo) {
        this.repo = repo;
    }

    public Optional<KpiQualityEvidence> findOne(Long factoryId, String fy, String quarter, Integer month, MetricKey key) {
        return repo.findByFactory_IdAndFyAndQuarterAndMonthAndMetricKey(factoryId, fy, quarter, month, key);
    }

    public Optional<KpiQualityEvidence> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public KpiQualityEvidence saveOrReplace(Factory factory,
                                           String fy,
                                           String quarter,
                                           Integer month,
                                           MetricKey metricKey,
                                           MultipartFile file) throws Exception {

        validateImage(file);

        // pasta: uploads/onepager/quality/{factoryId}/{fy}/{quarter}/{month}/
        Path base = Paths.get(uploadDir, "onepager", "quality",
                String.valueOf(factory.getId()),
                fy,
                quarter,
                String.valueOf(month)
        ).toAbsolutePath().normalize();

        Files.createDirectories(base);

        String ext = getExtension(file.getOriginalFilename());
        String safeName = metricKey.name() + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ext;

        Path target = base.resolve(safeName).normalize();

        // buscar existente para esse período/métrica
        Optional<KpiQualityEvidence> existingOpt =
                repo.findByFactory_IdAndFyAndQuarterAndMonthAndMetricKey(factory.getId(), fy, quarter, month, metricKey);

        KpiQualityEvidence ev = existingOpt.orElseGet(KpiQualityEvidence::new);
        ev.setFactory(factory);
        ev.setFy(fy);
        ev.setQuarter(quarter);
        ev.setMonth(month);
        ev.setMetricKey(metricKey);
        ev.setFileName(file.getOriginalFilename());
        ev.setContentType(file.getContentType() == null ? "image/png" : file.getContentType());
        ev.setUploadedAt(LocalDateTime.now());

        // se já tinha, tenta apagar o arquivo antigo
        if (existingOpt.isPresent()) {
            try {
                Path old = Paths.get(existingOpt.get().getStoragePath());
                Files.deleteIfExists(old);
            } catch (Exception ignored) {}
        }

        // salva novo arquivo
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        ev.setStoragePath(target.toString());

        return repo.save(ev);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio.");
        }

        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!(ct.startsWith("image/"))) {
            throw new IllegalArgumentException("Envie apenas imagem (png/jpg/webp).");
        }
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf(".");
        if (idx < 0) return "";
        String ext = name.substring(idx).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) return "";
        return ext;
    }
    
    public void deleteById(Long evidenceId) throws Exception {
        var opt = repo.findById(evidenceId);
        if (opt.isEmpty()) return;

        KpiQualityEvidence ev = opt.get();

        // 1) apaga o arquivo do disco
        if (ev.getStoragePath() != null && !ev.getStoragePath().isBlank()) {
            java.nio.file.Files.deleteIfExists(java.nio.file.Path.of(ev.getStoragePath()));
        }

        // 2) apaga do banco
        repo.delete(ev);
    }

}
