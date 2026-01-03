package com.manufacturing.manufacturingindex.controller;

import com.manufacturing.manufacturingindex.model.Factory;
import com.manufacturing.manufacturingindex.model.KpiQualityEvidence;
import com.manufacturing.manufacturingindex.model.KpiQualityEvidence.MetricKey;
import com.manufacturing.manufacturingindex.repository.FactoryRepository;
import com.manufacturing.manufacturingindex.service.KpiQualityEvidenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/one-pager/quality")
public class KpiQualityEvidenceController {

    private final FactoryRepository factoryRepo;
    private final KpiQualityEvidenceService service;

    public KpiQualityEvidenceController(FactoryRepository factoryRepo,
                                        KpiQualityEvidenceService service) {
        this.factoryRepo = factoryRepo;
        this.service = service;
    }

    // ✅ Tela para anexar evidências (month opcional: "JAN", "FEB"... ou vazio)
    @GetMapping("/{factoryId}")
    public String form(@PathVariable Long factoryId,
                       @RequestParam(required = false) String fy,
                       @RequestParam(required = false) String quarter,
                       @RequestParam(required = false) String month,
                       Model model) {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        // defaults (evita 400 quando abrir sem params)
        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";

        String monthRef = normalizeMonthRef(month);       // "" ou "JAN"
        Integer monthNumber = monthRefToNumber(monthRef); // null ou 1..12

        model.addAttribute("factory", factory);
        model.addAttribute("fy", fy);
        model.addAttribute("quarter", quarter);

        // para o HTML: null quando All Months
        model.addAttribute("month", monthRef.isBlank() ? null : monthRef);

        // listas pros selects
        model.addAttribute("fyList", List.of("FY24", "FY25", "FY26"));
        model.addAttribute("quarters", List.of("Q1", "Q2", "Q3", "Q4"));
        model.addAttribute("months", List.of("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"));

        // Se não tem month selecionado, não busca evidência
        if (monthNumber != null) {
            model.addAttribute("mppaEv", service.findOne(factoryId, fy, quarter, monthNumber, MetricKey.MPPA).orElse(null));
            model.addAttribute("mqaaEv", service.findOne(factoryId, fy, quarter, monthNumber, MetricKey.MQAA).orElse(null));
            model.addAttribute("btpEv",  service.findOne(factoryId, fy, quarter, monthNumber, MetricKey.BTP).orElse(null));
        } else {
            model.addAttribute("mppaEv", null);
            model.addAttribute("mqaaEv", null);
            model.addAttribute("btpEv", null);
        }

        return "one-pager/quality-evidence-form";
    }

    // ✅ Upload (month opcional)
    @PostMapping("/{factoryId}/upload")
    public String upload(@PathVariable Long factoryId,
                         @RequestParam(required = false) String fy,
                         @RequestParam(required = false) String quarter,
                         @RequestParam(required = false) String month,
                         @RequestParam(required = false) MultipartFile mppaFile,
                         @RequestParam(required = false) MultipartFile mqaaFile,
                         @RequestParam(required = false) MultipartFile btpFile) throws Exception {

        Factory factory = factoryRepo.findById(factoryId).orElseThrow();

        // defaults
        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";

        String monthRef = normalizeMonthRef(month);
        Integer monthNumber = monthRefToNumber(monthRef);

        // Se não selecionou mês, não salva (volta pro form sem month)
        if (monthNumber == null) {
            String url = UriComponentsBuilder.fromPath("/one-pager/quality/" + factoryId)
                    .queryParam("fy", fy)
                    .queryParam("quarter", quarter)
                    .build()
                    .toUriString();
            return "redirect:" + url;
        }

        if (mppaFile != null && !mppaFile.isEmpty()) {
            service.saveOrReplace(factory, fy, quarter, monthNumber, MetricKey.MPPA, mppaFile);
        }
        if (mqaaFile != null && !mqaaFile.isEmpty()) {
            service.saveOrReplace(factory, fy, quarter, monthNumber, MetricKey.MQAA, mqaaFile);
        }
        if (btpFile != null && !btpFile.isEmpty()) {
            service.saveOrReplace(factory, fy, quarter, monthNumber, MetricKey.BTP, btpFile);
        }

        String url = UriComponentsBuilder.fromPath("/one-pager/quality/" + factoryId)
                .queryParam("fy", fy)
                .queryParam("quarter", quarter)
                .queryParam("month", monthRef) // month sempre presente aqui (pois monthNumber != null)
                .build()
                .toUriString();

        return "redirect:" + url;
    }

    // ✅ Endpoint para exibir imagem no <img src="...">
    @GetMapping("/img/{evidenceId}")
    public ResponseEntity<Resource> image(@PathVariable Long evidenceId,
                                          HttpServletRequest request) throws Exception {

        KpiQualityEvidence ev = service.findById(evidenceId).orElseThrow();
        Path path = Path.of(ev.getStoragePath());

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource res = new FileSystemResource(path);

        String contentType = ev.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = request.getServletContext().getMimeType(path.toString());
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.noCache())
                .body(res);
    }

    // =========================
    // Helpers
    // =========================

    private String normalizeMonthRef(String month) {
        if (month == null) return "";
        String m = month.trim().toUpperCase();
        if (m.isBlank()) return "";
        if (m.equals("ALL")) return "";
        return m;
    }

    private Integer monthRefToNumber(String monthRef) {
        if (monthRef == null || monthRef.isBlank()) return null;

        return switch (monthRef.toUpperCase()) {
            case "JAN" -> 1;
            case "FEB" -> 2;
            case "MAR" -> 3;
            case "APR" -> 4;
            case "MAY" -> 5;
            case "JUN" -> 6;
            case "JUL" -> 7;
            case "AUG" -> 8;
            case "SEP" -> 9;
            case "OCT" -> 10;
            case "NOV" -> 11;
            case "DEC" -> 12;
            default -> null;
        };
    }
    
    @PostMapping("/{factoryId}/delete/{evidenceId}")
    public String deleteEvidence(@PathVariable Long factoryId,
                                 @PathVariable Long evidenceId,
                                 @RequestParam(required = false) String fy,
                                 @RequestParam(required = false) String quarter,
                                 @RequestParam(required = false) String month) throws Exception {

        // defaults (se vier vazio)
        if (fy == null || fy.isBlank()) fy = "FY26";
        if (quarter == null || quarter.isBlank()) quarter = "Q1";

        // apaga arquivo + banco
        service.deleteById(evidenceId);

        // volta pra mesma tela
        String monthRef = normalizeMonthRef(month);

        String url = org.springframework.web.util.UriComponentsBuilder
                .fromPath("/one-pager/quality/" + factoryId)
                .queryParam("fy", fy)
                .queryParam("quarter", quarter)
                .queryParam("month", monthRef)
                .build()
                .toUriString();

        return "redirect:" + url;
    }

}
