package com.victorqueiroga.serverwatch.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorqueiroga.serverwatch.service.ServerMonitoringService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para páginas de monitoramento
 */
@Slf4j
@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Profile("!dev")  // Exclui do profile dev
public class MonitoringController {

    private final ServerMonitoringService monitoringService;

    /**
     * Página principal de monitoramento
     */
    @GetMapping
    public String monitoringPage(Model model) {
        log.debug("Acessando página de monitoramento");
        
        // Inicializa cache se necessário
        monitoringService.initializeCache();
        
        model.addAttribute("title", "Monitoramento de Servidores");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/dashboard";
    }

    /**
     * Dashboard de monitoramento
     */
    @GetMapping("/dashboard")
    public String monitoringDashboard(Model model) {
        log.debug("Acessando dashboard de monitoramento");
        
        model.addAttribute("title", "Dashboard de Monitoramento");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/dashboard";
    }

    /**
     * Lista de servidores
     */
    @GetMapping("/servers")
    public String serversList(Model model) {
        log.debug("Acessando lista de servidores");
        
        model.addAttribute("title", "Lista de Servidores");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/servers";
    }

    /**
     * Página de detalhes de monitoramento
     */
    @GetMapping("/details")
    public String monitoringDetailsPage(Model model) {
        log.debug("Acessando página de detalhes de monitoramento");
        
        model.addAttribute("title", "Detalhes de Monitoramento");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/details";
    }
}