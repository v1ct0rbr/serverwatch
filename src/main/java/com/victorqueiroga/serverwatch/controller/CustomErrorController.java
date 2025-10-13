package com.victorqueiroga.serverwatch.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class CustomErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public String handleError(HttpServletRequest request, Model model) {
        // Obter informações do erro
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        Integer statusCode = null;
        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
        }

        // Criar modelo de erro
        ErrorInfo errorInfo = createErrorInfo(statusCode, errorMessage, exception, requestUri, request);
        
        model.addAttribute("error", errorInfo);
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        // Log do erro
        log.error("Erro capturado: {} - {} - URI: {}", 
                 statusCode, 
                 errorInfo.getMessage(), 
                 errorInfo.getPath());

        // Retornar template específico baseado no código de erro
        String templateName = getErrorTemplate(statusCode);
        return "error/" + templateName;
    }

    private ErrorInfo createErrorInfo(Integer statusCode, Object errorMessage, 
                                    Object exception, Object requestUri, 
                                    HttpServletRequest request) {
        ErrorInfo errorInfo = new ErrorInfo();
        
        if (statusCode != null) {
            errorInfo.setStatus(statusCode);
            HttpStatus httpStatus = HttpStatus.resolve(statusCode);
            if (httpStatus != null) {
                errorInfo.setError(httpStatus.getReasonPhrase());
            }
        }

        // Definir mensagem personalizada baseada no status
        errorInfo.setMessage(getCustomMessage(statusCode, errorMessage));
        errorInfo.setDescription(getCustomDescription(statusCode));
        errorInfo.setPath(requestUri != null ? requestUri.toString() : request.getRequestURI());
        
        // Adicionar sugestões de ação
        errorInfo.setSuggestions(getSuggestions(statusCode));
        
        return errorInfo;
    }

    private String getCustomMessage(Integer statusCode, Object errorMessage) {
        if (statusCode == null) {
            return "Erro interno do sistema";
        }

        return switch (statusCode) {
            case 400 -> "Requisição inválida";
            case 401 -> "Acesso não autorizado";
            case 403 -> "Acesso negado";
            case 404 -> "Página não encontrada";
            case 405 -> "Método não permitido";
            case 500 -> "Erro interno do servidor";
            case 502 -> "Gateway inválido";
            case 503 -> "Serviço indisponível";
            default -> errorMessage != null ? errorMessage.toString() : "Erro desconhecido";
        };
    }

    private String getCustomDescription(Integer statusCode) {
        if (statusCode == null) {
            return "Ocorreu um erro inesperado no sistema. Nossa equipe foi notificada.";
        }

        return switch (statusCode) {
            case 400 -> "Os dados enviados são inválidos ou estão em formato incorreto.";
            case 401 -> "É necessário fazer login para acessar este recurso.";
            case 403 -> "Você não possui permissão para acessar este recurso.";
            case 404 -> "O recurso solicitado não foi encontrado no servidor.";
            case 405 -> "O método HTTP utilizado não é permitido para este recurso.";
            case 500 -> "Ocorreu um erro interno no servidor. Nossa equipe foi notificada.";
            case 502 -> "Erro de comunicação com serviços externos.";
            case 503 -> "O serviço está temporariamente indisponível. Tente novamente em alguns minutos.";
            default -> "Ocorreu um erro inesperado. Nossa equipe foi notificada.";
        };
    }

    private String[] getSuggestions(Integer statusCode) {
        if (statusCode == null) {
            return new String[]{"Recarregue a página", "Entre em contato com o suporte"};
        }

        return switch (statusCode) {
            case 400 -> new String[]{
                "Verifique os dados informados",
                "Certifique-se de que todos os campos obrigatórios estão preenchidos",
                "Verifique o formato dos dados (datas, emails, etc.)"
            };
            case 401 -> new String[]{
                "Faça login no sistema",
                "Verifique se suas credenciais estão corretas",
                "Entre em contato com o administrador se o problema persistir"
            };
            case 403 -> new String[]{
                "Verifique se você possui as permissões necessárias",
                "Entre em contato com o administrador do sistema",
                "Certifique-se de que está logado com a conta correta"
            };
            case 404 -> new String[]{
                "Verifique se o endereço está correto",
                "Volte à página inicial",
                "Use o menu de navegação"
            };
            case 500 -> new String[]{
                "Tente novamente em alguns minutos",
                "Se o problema persistir, entre em contato com o suporte",
                "Nossa equipe técnica foi notificada"
            };
            default -> new String[]{
                "Recarregue a página",
                "Tente novamente em alguns minutos",
                "Entre em contato com o suporte se necessário"
            };
        };
    }

    private String getErrorTemplate(Integer statusCode) {
        if (statusCode == null) {
            return "general";
        }

        return switch (statusCode) {
            case 401, 403 -> "access-denied";
            case 404 -> "404";
            case 500 -> "500";
            default -> "general";
        };
    }

    // Classe interna para informações de erro
    public static class ErrorInfo {
        private Integer status;
        private String error;
        private String message;
        private String description;
        private String path;
        private String[] suggestions;

        // Getters e Setters
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String[] getSuggestions() { return suggestions; }
        public void setSuggestions(String[] suggestions) { this.suggestions = suggestions; }
    }
}