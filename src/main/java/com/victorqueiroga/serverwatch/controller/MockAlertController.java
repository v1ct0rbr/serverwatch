package com.victorqueiroga.serverwatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.victorqueiroga.serverwatch.service.MockAlertService;

/**
 * Mock do AlertController para desenvolvimento
 */
@Controller
@Profile("dev")
@RequestMapping("/alerts")
public class MockAlertController {

    @Autowired
    private MockAlertService alertService;

    @GetMapping
    public String alerts(Model model) {
        model.addAttribute("alerts", alertService.getRecentAlerts());
        model.addAttribute("unresolvedCount", alertService.getUnresolvedAlertsCount());
        return "alerts/list";
    }

    @GetMapping("/unresolved")
    public String unresolvedAlerts(Model model) {
        model.addAttribute("alerts", alertService.getUnresolvedAlerts());
        return "alerts/unresolved";
    }
}