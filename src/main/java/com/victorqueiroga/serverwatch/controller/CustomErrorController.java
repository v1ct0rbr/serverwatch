package com.victorqueiroga.serverwatch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador customizado para tratamento de erros HTTP.
 * 
 * O Spring Boot mapeia automaticamente TODOS os erros para /error
 * Este controller processa a requisição e renderiza um template unificado
 * com styling customizado baseado no código de status.
 * 
 * Não é necessário criar um arquivo separado para cada código de erro (400,
 * 403, 404, etc).
 * Um único template (error.html) é utilizado para todos, com cores e ícones
 * específicos.
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    /**
     * Processa TODOS os erros HTTP.
     * O Spring Boot automaticamente redireciona para este endpoint (/error)
     * para qualquer erro não tratado.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 500;
        String errorMessage = (message != null) ? message.toString() : getDefaultMessage(statusCode);
        String requestUri = (uri != null) ? uri.toString() : "desconhecido";
        java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();

        logError(statusCode, errorMessage, requestUri, exception);

        // Adiciona os atributos ao model para o template usar
        model.addAttribute("status", statusCode);
        model.addAttribute("message", errorMessage);
        model.addAttribute("uri", requestUri);
        model.addAttribute("timestamp", timestamp);

        // Renderiza um único template para TODOS os erros
        // O template (error.html) usa o status code para escolher cores, ícones e
        // mensagens
        return "error";
    }

    /**
     * Retorna mensagens localizadas baseadas no código de status HTTP.
     * Adicione mais cases conforme necessário.
     */
    private String getDefaultMessage(int statusCode) {
        return switch (statusCode) {
            case 400 -> "A requisição enviada é inválida ou mal formatada";
            case 401 -> "Você precisa fazer login para acessar este recurso";
            case 403 -> "Você não tem permissão para acessar este recurso";
            case 404 -> "A página ou recurso solicitado não foi encontrado";
            case 500 -> "Ocorreu um erro interno no servidor";
            case 503 -> "O serviço está temporariamente indisponível";
            default -> "Ocorreu um erro ao processar sua requisição";
        };
    }

    /**
     * Loga o erro com o nível apropriado (ERROR para 5xx, WARN para 4xx)
     */
    private void logError(int statusCode, String message, String uri, Object exception) {
        String exceptionInfo = exception != null ? " - " + ((Throwable) exception).getMessage() : "";

        if (statusCode >= 500) {
            logger.error("Erro do servidor {}: {} - URI: {}{}",
                    statusCode, message, uri, exceptionInfo);
        } else {
            logger.warn("Erro do cliente {}: {} - URI: {}{}",
                    statusCode, message, uri, exceptionInfo);
        }
    }
}