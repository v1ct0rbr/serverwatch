package com.victorqueiroga.serverwatch.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorqueiroga.serverwatch.service.MockServerMonitoringService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock Controller para páginas de monitoramento em dev
 */
@Slf4j
@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
@Profile("dev")
public class MockMonitoringController {

    private final MockServerMonitoringService mockMonitoringService;

    @GetMapping
    public String monitoringPage(Model model) {
        log.debug("Mock: Acessando página de monitoramento");
        
        mockMonitoringService.initializeCache();
        
        model.addAttribute("title", "Monitoramento de Servidores");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/dashboard";
    }

    @GetMapping("/dashboard")
    public String monitoringDashboard(Model model) {
        log.debug("Mock: Acessando dashboard de monitoramento");
        
        model.addAttribute("title", "Dashboard de Monitoramento");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/dashboard";
    }

    @GetMapping("/servers")
    public String serversList(Model model) {
        log.debug("Mock: Acessando lista de servidores");
        
        model.addAttribute("title", "Lista de Servidores");
        model.addAttribute("currentPage", "monitoring");
        
        return "pages/monitoring/servers";
    }
}