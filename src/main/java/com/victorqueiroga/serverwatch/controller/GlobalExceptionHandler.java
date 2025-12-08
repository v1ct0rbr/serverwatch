package com.victorqueiroga.serverwatch.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Tratador global de exceções da aplicação.
 * Captura e trata exceções em toda a aplicação, retornando templates
 * apropriados.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Trata exceções de recurso não encontrado (404)
     * Inclui handler para NoHandlerFoundException (URLs) e NoResourceFoundException
     * (templates)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("Recurso não encontrado: {} {}", request.getMethod(), ex.getRequestURL());

        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("status", 404);
        mav.addObject("message", "A página que você procura não existe");
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", "Verifique a URL e tente novamente.");

        return mav;
    }

    /**
     * Trata exceções de recurso estático/template não encontrado (404)
     * NoResourceFoundException é lançada quando um template Thymeleaf não existe
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        logger.warn("Recurso não encontrado (template/static): {}", ex.getResourcePath());

        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("status", 404);
        mav.addObject("message", "Recurso não encontrado");
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", "O template ou recurso solicitado não foi encontrado. Verifique a configuração.");

        return mav;
    }

    /**
     * Trata exceções de acesso negado (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Acesso negado para: {}", request.getRequestURI());

        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("status", 403);
        mav.addObject("message", "Acesso negado");
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", "Você não possui permissão para acessar este recurso.");

        return mav;
    }

    /**
     * Trata exceções de argumento inválido (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.error("Argumento inválido enviado para: {}", request.getRequestURI());

        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("status", 400);
        mav.addObject("message", "Dados inválidos");
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", "Os dados enviados contêm erros de validação. Verifique os campos e tente novamente.");
        mav.addObject("errors", ex.getBindingResult().getFieldErrors());

        return mav;
    }

    /**
     * Trata exceções de tipo de argumento inválido (400)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        logger.error("Tipo de argumento inválido: {} para {}", ex.getName(), ex.getValue());

        ModelAndView mav = new ModelAndView("error/400");
        mav.addObject("status", 400);
        mav.addObject("message", "Tipo de dados inválido");
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", String.format("O parâmetro '%s' deve ser do tipo %s", ex.getName(),
                ex.getRequiredType().getSimpleName()));

        return mav;
    }

    /**
     * Trata exceções de negócio customizadas
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.error("Erro de negócio: {}", ex.getMessage());

        ModelAndView mav = new ModelAndView("error/business");
        mav.addObject("status", 400);
        mav.addObject("message", ex.getMessage());
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", ex.getDetail());
        mav.addObject("errorCode", ex.getErrorCode());

        return mav;
    }

    /**
     * Trata exceções gerais não tratadas (500)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception ex, HttpServletRequest request) {
        logger.error("Erro interno do servidor", ex);

        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("status", 500);
        mav.addObject("message", "Erro interno do servidor");
        mav.addObject("uri", request.getRequestURI());
        mav.addObject("timestamp", LocalDateTime.now());
        mav.addObject("detail", "Um erro inesperado ocorreu. Nossa equipe foi notificada.");
        mav.addObject("exceptionMessage", ex.getMessage());

        return mav;
    }

    /**
     * Exceção customizada para erros de negócio
     */
    public static class BusinessException extends RuntimeException {
        private final String errorCode;
        private final String detail;

        public BusinessException(String message, String errorCode, String detail) {
            super(message);
            this.errorCode = errorCode;
            this.detail = detail;
        }

        public BusinessException(String message, String errorCode) {
            this(message, errorCode, "");
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getDetail() {
            return detail;
        }
    }
}
