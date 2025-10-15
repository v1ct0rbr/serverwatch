package com.victorqueiroga.serverwatch.controller;

import com.victorqueiroga.serverwatch.model.Alert;
import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.Severity;
import com.victorqueiroga.serverwatch.service.AlertService;
import com.victorqueiroga.serverwatch.service.ServerService;
import com.victorqueiroga.serverwatch.service.SeverityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para gerenciamento de alertas
 */
@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Slf4j
@Profile("!dev")  // Exclui este controller do profile dev
public class AlertController {

    private final AlertService alertService;
    private final ServerService serverService;
    private final SeverityService severityService;

    /**
     * Lista alertas com paginação e filtros
     */
    @GetMapping
    public String listAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Long severityId,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String title,
            Model model) {

        try {
            // Cria paginação
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            // Converte parâmetros para enums
            Alert.AlertStatus alertStatus = null;
            if (status != null && !status.trim().isEmpty()) {
                try {
                    alertStatus = Alert.AlertStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Status de alerta inválido: {}", status);
                }
            }

            Alert.AlertType alertTypeEnum = null;
            if (alertType != null && !alertType.trim().isEmpty()) {
                try {
                    alertTypeEnum = Alert.AlertType.valueOf(alertType.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Tipo de alerta inválido: {}", alertType);
                }
            }

            // Busca alertas com filtros
            Page<Alert> alerts = alertService.findByFilters(
                serverId, severityId, resolved, alertStatus, alertTypeEnum, title, pageable);

            // Carrega dados para filtros
            List<Server> servers = serverService.findAll();
            List<Severity> severities = severityService.findAll();

            // Estatísticas
            long totalAlerts = alertService.countActiveAlerts();
            long criticalAlerts = alertService.countActiveCriticalAlerts();

            // Adiciona atributos ao modelo
            model.addAttribute("alerts", alerts);
            model.addAttribute("servers", servers);
            model.addAttribute("severities", severities);
            model.addAttribute("alertStatuses", Alert.AlertStatus.values());
            model.addAttribute("alertTypes", Alert.AlertType.values());
            model.addAttribute("totalAlerts", totalAlerts);
            model.addAttribute("criticalAlerts", criticalAlerts);

            // Parâmetros de filtro
            model.addAttribute("currentServerId", serverId);
            model.addAttribute("currentSeverityId", severityId);
            model.addAttribute("currentResolved", resolved);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentAlertType", alertType);
            model.addAttribute("currentTitle", title);

            // Parâmetros de paginação
            model.addAttribute("currentPage", page);
            model.addAttribute("currentSize", size);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentSortDir", sortDir);

            return "alerts/list";

        } catch (Exception e) {
            log.error("Erro ao listar alertas: ", e);
            model.addAttribute("errorMessage", "Erro ao carregar alertas: " + e.getMessage());
            return "error/general";
        }
    }

    /**
     * Exibe formulário para novo alerta
     */
    @GetMapping("/new")
    public String newAlert(Model model) {
        try {
            Alert alert = new Alert();
            alert.setStatus(Alert.AlertStatus.OPEN);
            alert.setAlertType(Alert.AlertType.MONITORING);
            alert.setResolved(false);

            model.addAttribute("alert", alert);
            model.addAttribute("servers", serverService.findAll());
            model.addAttribute("severities", severityService.findAll());
            model.addAttribute("alertStatuses", Alert.AlertStatus.values());
            model.addAttribute("alertTypes", Alert.AlertType.values());
            model.addAttribute("pageTitle", "Novo Alerta");

            return "alerts/form";

        } catch (Exception e) {
            log.error("Erro ao carregar formulário de novo alerta: ", e);
            model.addAttribute("errorMessage", "Erro ao carregar formulário: " + e.getMessage());
            return "error/general";
        }
    }

    /**
     * Salva novo alerta
     */
    @PostMapping
    public String saveAlert(@Valid @ModelAttribute Alert alert,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("servers", serverService.findAll());
                model.addAttribute("severities", severityService.findAll());
                model.addAttribute("alertStatuses", Alert.AlertStatus.values());
                model.addAttribute("alertTypes", Alert.AlertType.values());
                model.addAttribute("pageTitle", "Novo Alerta");
                return "alerts/form";
            }

            Alert savedAlert = alertService.save(alert);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Alerta criado com sucesso! ID: " + savedAlert.getId());

            return "redirect:/alerts";

        } catch (Exception e) {
            log.error("Erro ao salvar alerta: ", e);
            model.addAttribute("errorMessage", "Erro ao salvar alerta: " + e.getMessage());
            model.addAttribute("servers", serverService.findAll());
            model.addAttribute("severities", severityService.findAll());
            model.addAttribute("alertStatuses", Alert.AlertStatus.values());
            model.addAttribute("alertTypes", Alert.AlertType.values());
            model.addAttribute("pageTitle", "Novo Alerta");
            return "alerts/form";
        }
    }

    /**
     * Exibe detalhes do alerta
     */
    @GetMapping("/{id}")
    public String viewAlert(@PathVariable Long id, Model model) {
        try {
            Optional<Alert> alertOpt = alertService.findById(id);
            if (alertOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Alerta não encontrado");
                return "error/404";
            }

            Alert alert = alertOpt.get();
            model.addAttribute("alert", alert);
            model.addAttribute("pageTitle", "Detalhes do Alerta");

            return "alerts/view";

        } catch (Exception e) {
            log.error("Erro ao visualizar alerta ID {}: ", id, e);
            model.addAttribute("errorMessage", "Erro ao carregar alerta: " + e.getMessage());
            return "error/general";
        }
    }

    /**
     * Exibe formulário para editar alerta
     */
    @GetMapping("/{id}/edit")
    public String editAlert(@PathVariable Long id, Model model) {
        try {
            Optional<Alert> alertOpt = alertService.findById(id);
            if (alertOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Alerta não encontrado");
                return "error/404";
            }

            model.addAttribute("alert", alertOpt.get());
            model.addAttribute("servers", serverService.findAll());
            model.addAttribute("severities", severityService.findAll());
            model.addAttribute("alertStatuses", Alert.AlertStatus.values());
            model.addAttribute("alertTypes", Alert.AlertType.values());
            model.addAttribute("pageTitle", "Editar Alerta");

            return "alerts/form";

        } catch (Exception e) {
            log.error("Erro ao carregar formulário de edição do alerta ID {}: ", id, e);
            model.addAttribute("errorMessage", "Erro ao carregar formulário: " + e.getMessage());
            return "error/general";
        }
    }

    /**
     * Atualiza alerta
     */
    @PostMapping("/{id}")
    public String updateAlert(@PathVariable Long id,
                             @Valid @ModelAttribute Alert alert,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("servers", serverService.findAll());
                model.addAttribute("severities", severityService.findAll());
                model.addAttribute("alertStatuses", Alert.AlertStatus.values());
                model.addAttribute("alertTypes", Alert.AlertType.values());
                model.addAttribute("pageTitle", "Editar Alerta");
                return "alerts/form";
            }

            alert.setId(id);
            Alert savedAlert = alertService.save(alert);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Alerta atualizado com sucesso!");

            return "redirect:/alerts/" + savedAlert.getId();

        } catch (Exception e) {
            log.error("Erro ao atualizar alerta ID {}: ", id, e);
            model.addAttribute("errorMessage", "Erro ao atualizar alerta: " + e.getMessage());
            model.addAttribute("servers", serverService.findAll());
            model.addAttribute("severities", severityService.findAll());
            model.addAttribute("alertStatuses", Alert.AlertStatus.values());
            model.addAttribute("alertTypes", Alert.AlertType.values());
            model.addAttribute("pageTitle", "Editar Alerta");
            return "alerts/form";
        }
    }

    /**
     * Exclui alerta
     */
    @PostMapping("/{id}/delete")
    public String deleteAlert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            alertService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Alerta excluído com sucesso!");

        } catch (Exception e) {
            log.error("Erro ao excluir alerta ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao excluir alerta: " + e.getMessage());
        }

        return "redirect:/alerts";
    }

    /**
     * Resolve alerta
     */
    @PostMapping("/{id}/resolve")
    public String resolveAlert(@PathVariable Long id,
                              @RequestParam String resolvedBy,
                              @RequestParam(required = false) String resolutionComment,
                              RedirectAttributes redirectAttributes) {
        try {
            alertService.resolveAlert(id, resolvedBy, resolutionComment);
            redirectAttributes.addFlashAttribute("successMessage", "Alerta resolvido com sucesso!");

        } catch (Exception e) {
            log.error("Erro ao resolver alerta ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao resolver alerta: " + e.getMessage());
        }

        return "redirect:/alerts/" + id;
    }

    /**
     * Reconhece alerta
     */
    @PostMapping("/{id}/acknowledge")
    public String acknowledgeAlert(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            alertService.acknowledgeAlert(id);
            redirectAttributes.addFlashAttribute("successMessage", "Alerta reconhecido com sucesso!");

        } catch (Exception e) {
            log.error("Erro ao reconhecer alerta ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao reconhecer alerta: " + e.getMessage());
        }

        return "redirect:/alerts/" + id;
    }

    // === API REST ===

    /**
     * Lista alertas (API REST)
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Page<Alert>> listAlertsApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Alert> alerts = alertService.findAll(pageable);

            return ResponseEntity.ok(alerts);

        } catch (Exception e) {
            log.error("Erro na API de listagem de alertas: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca alertas ativos (API REST)
     */
    @GetMapping("/api/active")
    @ResponseBody
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        try {
            List<Alert> activeAlerts = alertService.findRecentAlerts();
            return ResponseEntity.ok(activeAlerts);

        } catch (Exception e) {
            log.error("Erro na API de alertas ativos: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca alertas críticos (API REST)
     */
    @GetMapping("/api/critical")
    @ResponseBody
    public ResponseEntity<List<Alert>> getCriticalAlerts() {
        try {
            List<Alert> criticalAlerts = alertService.findActiveCriticalAlerts();
            return ResponseEntity.ok(criticalAlerts);

        } catch (Exception e) {
            log.error("Erro na API de alertas críticos: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Estatísticas de alertas (API REST)
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Object> getAlertStats() {
        try {
            var stats = new java.util.HashMap<String, Object>();
            
            stats.put("totalActive", alertService.countActiveAlerts());
            stats.put("totalCritical", alertService.countActiveCriticalAlerts());
            stats.put("statsByStatus", alertService.getAlertStatsByStatus());
            stats.put("statsByType", alertService.getAlertStatsByType());
            stats.put("statsBySeverity", alertService.getAlertStatsBySeverity());
            stats.put("topServersWithAlerts", alertService.getTopServersWithAlerts(5));
            stats.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Erro na API de estatísticas de alertas: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cria alerta via API REST
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Alert> createAlertApi(@Valid @RequestBody Alert alert) {
        try {
            Alert savedAlert = alertService.save(alert);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAlert);

        } catch (Exception e) {
            log.error("Erro na API de criação de alerta: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Atualiza status do alerta via API REST
     */
    @PatchMapping("/api/{id}/status")
    @ResponseBody
    public ResponseEntity<Alert> updateAlertStatusApi(@PathVariable Long id, 
                                                     @RequestParam String status) {
        try {
            Alert.AlertStatus alertStatus = Alert.AlertStatus.valueOf(status.toUpperCase());
            Alert updatedAlert = alertService.updateAlertStatus(id, alertStatus);
            return ResponseEntity.ok(updatedAlert);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erro na API de atualização de status do alerta ID {}: ", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}