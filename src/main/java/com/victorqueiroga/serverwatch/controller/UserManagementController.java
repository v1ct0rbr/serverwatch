package com.victorqueiroga.serverwatch.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.victorqueiroga.serverwatch.model.User;
import com.victorqueiroga.serverwatch.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para gerenciamento administrativo de usuários
 */
@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {

    private final UserService userService;

    /**
     * Página de gerenciamento de usuários
     */
    @GetMapping
    public String usersPage(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(defaultValue = "username") String sortBy,
                           @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            // Configurar paginação e ordenação
            Sort sort = Sort.by(sortDir.equals("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Buscar usuários ativos
            List<User> users = userService.findActiveUsers();
            
            // Estatísticas
            UserService.UserStats stats = userService.getUserStats();
            
            model.addAttribute("title", "Gerenciamento de Usuários");
            model.addAttribute("users", users);
            model.addAttribute("userStats", stats);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", (users.size() + size - 1) / size);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            
            return "admin/users";
            
        } catch (Exception e) {
            log.error("Erro ao carregar página de usuários", e);
            model.addAttribute("error", "Erro ao carregar usuários");
            return "error/500";
        }
    }

    /**
     * API REST - Listar usuários ativos
     */
    @GetMapping("/api")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getActiveUsers() {
        try {
            List<User> users = userService.findActiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Erro ao buscar usuários", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API REST - Obter estatísticas de usuários
     */
    @GetMapping("/api/stats")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserService.UserStats> getUserStats() {
        try {
            UserService.UserStats stats = userService.getUserStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API REST - Desativar usuário
     */
    @PostMapping("/api/{keycloakId}/deactivate")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@PathVariable String keycloakId) {
        try {
            userService.deactivateUser(keycloakId);
            return ResponseEntity.ok("Usuário desativado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao desativar usuário: {}", keycloakId, e);
            return ResponseEntity.internalServerError().body("Erro interno do servidor");
        }
    }

    /**
     * API REST - Reativar usuário
     */
    @PostMapping("/api/{keycloakId}/activate")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(@PathVariable String keycloakId) {
        try {
            userService.activateUser(keycloakId);
            return ResponseEntity.ok("Usuário reativado com sucesso");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao reativar usuário: {}", keycloakId, e);
            return ResponseEntity.internalServerError().body("Erro interno do servidor");
        }
    }

    /**
     * API REST - Buscar usuário por username
     */
    @GetMapping("/api/search")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> findUserByUsername(@RequestParam String username) {
        try {
            return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Erro ao buscar usuário: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}