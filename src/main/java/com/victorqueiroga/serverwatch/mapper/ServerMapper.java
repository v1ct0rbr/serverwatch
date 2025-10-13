package com.victorqueiroga.serverwatch.mapper;

import com.victorqueiroga.serverwatch.dto.ServerDTO;
import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.OperationSystem;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversões entre Server e ServerDTO
 */
@Component
public class ServerMapper {

    /**
     * Converte Server para ServerDTO
     */
    public ServerDTO toDTO(Server server) {
        if (server == null) {
            return null;
        }

        return ServerDTO.builder()
                .id(server.getId())
                .name(server.getName())
                .ipAddress(server.getIpAddress())
                .operationSystemId(server.getOperationSystem() != null ? server.getOperationSystem().getId() : null)
                .operationSystemName(server.getOperationSystem() != null ? server.getOperationSystem().getName() : null)
                .description(server.getDescription())
                .port(server.getPort())
                .active(server.getActive())
                .status(server.getStatus() != null ? server.getStatus().name() : null)
                .build();
    }

    /**
     * Converte ServerDTO para Server
     */
    public Server toEntity(ServerDTO dto) {
        if (dto == null) {
            return null;
        }

        Server server = new Server();
        server.setId(dto.getId());
        server.setName(dto.getName());
        server.setIpAddress(dto.getIpAddress());
        server.setDescription(dto.getDescription());
        server.setPort(dto.getPort());
        server.setActive(dto.getActive());

        // O sistema operacional deve ser definido separadamente
        if (dto.getOperationSystemId() != null) {
            OperationSystem os = new OperationSystem();
            os.setId(dto.getOperationSystemId());
            server.setOperationSystem(os);
        }

        // Status deve ser convertido de string
        if (dto.getStatus() != null) {
            try {
                server.setStatus(Server.ServerStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                server.setStatus(Server.ServerStatus.UNKNOWN);
            }
        }

        return server;
    }

    /**
     * Atualiza uma entidade Server existente com dados do DTO
     */
    public void updateEntity(Server server, ServerDTO dto) {
        if (server == null || dto == null) {
            return;
        }

        server.setName(dto.getName());
        server.setIpAddress(dto.getIpAddress());
        server.setDescription(dto.getDescription());
        server.setPort(dto.getPort());
        server.setActive(dto.getActive());

        // O sistema operacional deve ser definido separadamente
        if (dto.getOperationSystemId() != null) {
            OperationSystem os = new OperationSystem();
            os.setId(dto.getOperationSystemId());
            server.setOperationSystem(os);
        }

        // Status deve ser convertido de string
        if (dto.getStatus() != null) {
            try {
                server.setStatus(Server.ServerStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                server.setStatus(Server.ServerStatus.UNKNOWN);
            }
        }
    }

    /**
     * Converte Server para ServerDTO.Summary
     */
    public ServerDTO.Summary toSummaryDTO(Server server) {
        if (server == null) {
            return null;
        }

        return ServerDTO.Summary.builder()
                .id(server.getId())
                .name(server.getName())
                .ipAddress(server.getIpAddress())
                .operationSystemName(server.getOperationSystem() != null ? server.getOperationSystem().getName() : null)
                .status(server.getStatus() != null ? server.getStatus().getDisplayName() : "Desconhecido")
                .active(server.getActive())
                .lastResponseTime(server.getLastResponseTime())
                .build();
    }
}