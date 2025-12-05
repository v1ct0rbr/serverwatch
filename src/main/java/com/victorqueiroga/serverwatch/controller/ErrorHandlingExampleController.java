package com.victorqueiroga.serverwatch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Exemplo de controller que demonstra o uso do tratamento de erros.
 * Mostra como utilizar a BusinessException para diferentes cenários.
 */
@Controller
@RequestMapping("/example-error")
public class ErrorHandlingExampleController {

    /**
     * Exemplo de validação que lança BusinessException
     */
    @GetMapping("/validate/{id}")
    public String validateExample(@PathVariable String id, Model model) {
        // Validação: ID deve ser numérico
        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new GlobalExceptionHandler.BusinessException(
                "ID inválido fornecido",
                "ERR_INVALID_ID_FORMAT",
                "O ID deve conter apenas números. Valor fornecido: " + id
            );
        }

        model.addAttribute("message", "ID validado com sucesso: " + id);
        return "example-success";
    }

    /**
     * Exemplo de validação de intervalo
     */
    @GetMapping("/port/{port}")
    public String validatePort(@PathVariable int port, Model model) {
        if (port < 1 || port > 65535) {
            throw new GlobalExceptionHandler.BusinessException(
                "Número de porta inválido",
                "ERR_INVALID_PORT_RANGE",
                "A porta deve estar entre 1 e 65535. Valor fornecido: " + port
            );
        }

        model.addAttribute("message", "Porta " + port + " é válida");
        return "example-success";
    }

    /**
     * Exemplo de verificação de recurso existente
     */
    @GetMapping("/server/{serverId}")
    public String getServer(@PathVariable Long serverId, Model model) {
        // Simulando verificação de existência
        if (serverId == 0 || serverId < 0) {
            throw new GlobalExceptionHandler.BusinessException(
                "Servidor não encontrado",
                "ERR_SERVER_NOT_FOUND",
                "Nenhum servidor encontrado com o ID: " + serverId
            );
        }

        model.addAttribute("serverId", serverId);
        model.addAttribute("message", "Servidor encontrado com sucesso");
        return "example-success";
    }

    /**
     * Exemplo de validação de dados duplicados
     */
    @PostMapping("/register")
    public String registerServer(
            @RequestParam String name,
            @RequestParam String hostname,
            Model model) {

        // Simulando verificação de duplicação
        if ("existing-server".equals(hostname)) {
            throw new GlobalExceptionHandler.BusinessException(
                "Servidor com este hostname já existe",
                "ERR_SERVER_DUPLICATE",
                "O hostname '" + hostname + "' já está registrado no sistema. Use um hostname único."
            );
        }

        if (name == null || name.trim().isEmpty()) {
            throw new GlobalExceptionHandler.BusinessException(
                "Nome do servidor obrigatório",
                "ERR_EMPTY_SERVER_NAME",
                "O nome do servidor não pode estar vazio."
            );
        }

        model.addAttribute("message", "Servidor '" + name + "' registrado com sucesso");
        return "example-success";
    }

    /**
     * Exemplo de validação de acesso
     */
    @GetMapping("/restricted/{resourceId}")
    public String accessRestricted(@PathVariable String resourceId, Model model) {
        // Simulando verificação de acesso
        if ("forbidden".equals(resourceId)) {
            throw new GlobalExceptionHandler.BusinessException(
                "Acesso a este recurso foi bloqueado",
                "ERR_RESOURCE_ACCESS_DENIED",
                "Você não tem permissão para acessar o recurso: " + resourceId
            );
        }

        model.addAttribute("message", "Acesso concedido ao recurso: " + resourceId);
        return "example-success";
    }

    /**
     * Exemplo de validação de intervalo de valores
     */
    @GetMapping("/range")
    public String validateRange(
            @RequestParam int min,
            @RequestParam int max,
            Model model) {

        if (min >= max) {
            throw new GlobalExceptionHandler.BusinessException(
                "Intervalo de valores inválido",
                "ERR_INVALID_VALUE_RANGE",
                "O valor mínimo (" + min + ") deve ser menor que o valor máximo (" + max + ")"
            );
        }

        model.addAttribute("message", "Intervalo válido: " + min + " a " + max);
        return "example-success";
    }

    /**
     * Exemplo que simula um erro não tratado
     */
    @GetMapping("/unhandled-error")
    public String simulateUnhandledError() {
        // Isso será capturado pelo GlobalExceptionHandler e retornará error/500
        throw new RuntimeException("Este é um erro não tratado que será capturado pelo tratador global");
    }
}
