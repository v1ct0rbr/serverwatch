package com.victorqueiroga.serverwatch.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.victorqueiroga.serverwatch.model.Alert;
import com.victorqueiroga.serverwatch.model.Alert.AlertStatus;
import com.victorqueiroga.serverwatch.model.Alert.AlertType;

/**
 * Mock do AlertService para desenvolvimento
 */
@Service
@Profile("dev")
public class MockAlertService {

    public List<Alert> getRecentAlerts() {
        // Retorna alertas mock para desenvolvimento
        Alert alert1 = Alert.builder()
                .id(1L)
                .title("High Response Time")
                .description("Server response time high")
                .alertType(AlertType.MONITORING)
                .status(AlertStatus.OPEN)
                .resolved(false)
                .currentValue("1500ms")
                .thresholdValue("1000ms")
                .metricName("RESPONSE_TIME")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .resolvedAt(null)
                .build();

        Alert alert2 = Alert.builder()
                .id(2L)
                .title("Server Down")
                .description("App server is not responding")
                .alertType(AlertType.MONITORING)
                .status(AlertStatus.OPEN)
                .resolved(false)
                .currentValue("DOWN")
                .thresholdValue("UP")
                .metricName("AVAILABILITY")
                .createdAt(LocalDateTime.now().minusMinutes(30))
                .resolvedAt(null)
                .build();

        return Arrays.asList(alert1, alert2);
    }

    public List<Alert> getUnresolvedAlerts() {
        return getRecentAlerts(); // Para o mock, retorna os mesmos alertas
    }

    public long getUnresolvedAlertsCount() {
        return getUnresolvedAlerts().size();
    }

    public void resolveAlert(Long alertId) {
        // Mock - não faz nada
    }
}