package com.victorqueiroga.serverwatch.controller;

import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.OperationSystem;
import com.victorqueiroga.serverwatch.service.ServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciamento de servidores monitorados
 */
@Slf4j
@Controller
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    /**
     * Lista todos os servidores com paginação
     */
    @GetMapping
    public String listServers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search,
            Model model) {
        
        log.debug("Listando servidores - página: {}, tamanho: {}, ordenação: {} {}", 
                 page, size, sort, direction);

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<Server> servers;
        if (search != null && !search.trim().isEmpty()) {
            // Se há busca, usar filtro (sem paginação por simplicidade)
            List<Server> filteredServers = serverService.findByNameContaining(search);
            servers = new org.springframework.data.domain.PageImpl<>(filteredServers, pageable, filteredServers.size());
            model.addAttribute("search", search);
        } else {
            servers = serverService.findAll(pageable);
        }

        model.addAttribute("servers", servers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servers.getTotalPages());
        model.addAttribute("totalElements", servers.getTotalElements());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        return "pages/servers/list";
    }

    /**
     * Exibe formulário para criar novo servidor
     */
    @GetMapping("/new")
    public String newServer(Model model) {
        log.debug("Exibindo formulário para novo servidor");
        
        model.addAttribute("server", new Server());
        model.addAttribute("operationSystems", serverService.findAllOperationSystems());
        model.addAttribute("pageTitle", "Novo Servidor");
        
        return "pages/servers/form";
    }

    /**
     * Exibe detalhes de um servidor
     */
    @GetMapping("/{id}")
    public String viewServer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Visualizando servidor com ID: {}", id);
        
        Optional<Server> serverOpt = serverService.findById(id);
        if (serverOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Servidor não encontrado!");
            return "redirect:/servers";
        }

        model.addAttribute("server", serverOpt.get());
        return "pages/servers/view";
    }

    /**
     * Exibe formulário para editar servidor
     */
    @GetMapping("/{id}/edit")
    public String editServer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.debug("Editando servidor com ID: {}", id);
        
        Optional<Server> serverOpt = serverService.findById(id);
        if (serverOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Servidor não encontrado!");
            return "redirect:/servers";
        }

        model.addAttribute("server", serverOpt.get());
        model.addAttribute("operationSystems", serverService.findAllOperationSystems());
        model.addAttribute("pageTitle", "Editar Servidor");
        
        return "pages/servers/form";
    }

    /**
     * Salva servidor (criar ou atualizar)
     */
    @PostMapping
    public String saveServer(@Valid @ModelAttribute Server server, 
                           BindingResult bindingResult,
                           Model model, 
                           RedirectAttributes redirectAttributes) {
        
        log.debug("Salvando servidor: {}", server.getName());

        if (bindingResult.hasErrors()) {
            log.warn("Erro de validação ao salvar servidor: {}", bindingResult.getAllErrors());
            model.addAttribute("operationSystems", serverService.findAllOperationSystems());
            model.addAttribute("pageTitle", server.getId() == null ? "Novo Servidor" : "Editar Servidor");
            return "pages/servers/form";
        }

        try {
            Server savedServer = serverService.save(server);
            String message = server.getId() == null 
                ? "Servidor criado com sucesso!" 
                : "Servidor atualizado com sucesso!";
            
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/servers/" + savedServer.getId();
            
        } catch (Exception e) {
            log.error("Erro ao salvar servidor: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("operationSystems", serverService.findAllOperationSystems());
            model.addAttribute("pageTitle", server.getId() == null ? "Novo Servidor" : "Editar Servidor");
            return "pages/servers/form";
        }
    }

    /**
     * Deleta servidor
     */
    @PostMapping("/{id}/delete")
    public String deleteServer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("Deletando servidor com ID: {}", id);
        
        try {
            Optional<Server> server = serverService.findById(id);
            if (server.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Servidor não encontrado!");
                return "redirect:/servers";
            }

            serverService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", 
                "Servidor '" + server.get().getName() + "' foi deletado com sucesso!");
            
        } catch (Exception e) {
            log.error("Erro ao deletar servidor: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao deletar servidor: " + e.getMessage());
        }
        
        return "redirect:/servers";
    }

    // ==================== API REST Endpoints ====================

    /**
     * API: Lista todos os servidores
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<Server>> getAllServersApi() {
        log.debug("API: Buscando todos os servidores");
        List<Server> servers = serverService.findAll();
        return ResponseEntity.ok(servers);
    }

    /**
     * API: Busca servidor por ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Server> getServerByIdApi(@PathVariable Long id) {
        log.debug("API: Buscando servidor por ID: {}", id);
        
        Optional<Server> server = serverService.findById(id);
        return server.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * API: Cria novo servidor
     */
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Server> createServerApi(@Valid @RequestBody Server server) {
        log.debug("API: Criando novo servidor: {}", server.getName());
        
        try {
            Server savedServer = serverService.save(server);
            return ResponseEntity.ok(savedServer);
        } catch (Exception e) {
            log.error("API: Erro ao criar servidor: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Atualiza servidor existente
     */
    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Server> updateServerApi(@PathVariable Long id, @Valid @RequestBody Server server) {
        log.debug("API: Atualizando servidor ID: {}", id);
        
        if (!serverService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            server.setId(id);
            Server updatedServer = serverService.save(server);
            return ResponseEntity.ok(updatedServer);
        } catch (Exception e) {
            log.error("API: Erro ao atualizar servidor: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Deleta servidor
     */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteServerApi(@PathVariable Long id) {
        log.debug("API: Deletando servidor ID: {}", id);
        
        if (!serverService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            serverService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("API: Erro ao deletar servidor: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * API: Busca servidores por filtros
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Server>> searchServersApi(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Long operationSystemId) {
        
        log.debug("API: Buscando servidores com filtros - name: {}, ip: {}, osId: {}", 
                 name, ipAddress, operationSystemId);
        
        List<Server> servers = serverService.findByNameContaining(name != null ? name : "");
        return ResponseEntity.ok(servers);
    }

    /**
     * API: Lista sistemas operacionais disponíveis
     */
    @GetMapping("/api/operation-systems")
    @ResponseBody
    public ResponseEntity<List<OperationSystem>> getOperationSystemsApi() {
        log.debug("API: Buscando sistemas operacionais");
        List<OperationSystem> operationSystems = serverService.findAllOperationSystems();
        return ResponseEntity.ok(operationSystems);
    }
}