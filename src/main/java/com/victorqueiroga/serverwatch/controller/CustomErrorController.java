package com.victorqueiroga.serverwatch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        // Valores padrão caso os atributos sejam null
        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;
        String errorMessage = (message != null) ? message.toString() : "Erro interno do servidor";
        String requestUri = (uri != null) ? uri.toString() : "desconhecido";

        logger.error("Erro capturado: {} - {} - URI: {}", statusCode, errorMessage, requestUri);

        // Adicionar atributos ao modelo de forma segura
        model.addAttribute("status", statusCode);
        model.addAttribute("message", errorMessage);
        model.addAttribute("uri", requestUri);
        model.addAttribute("timestamp", java.time.LocalDateTime.now());

        // Criar objeto error para compatibilidade com o template
        ErrorInfo errorInfo = new ErrorInfo(statusCode, errorMessage, requestUri);
        model.addAttribute("error", errorInfo);

        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return "error/404";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
            return "error/401";
        }

        return "error/general";
    }

    // Classe interna para encapsular informações de erro
    public static class ErrorInfo {
        private final int status;
        private final String message;
        private final String path;

        public ErrorInfo(int status, String message, String path) {
            this.status = status;
            this.message = message;
            this.path = path;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getPath() {
            return path;
        }
    }
}