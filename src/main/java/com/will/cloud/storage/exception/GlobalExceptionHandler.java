package com.will.cloud.storage.exception;

import com.will.cloud.storage.dto.ApiErrorDto;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDto> handleException(
            DataIntegrityViolationException e, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ApiErrorDto errorDto =
                buildApiErrorDto("Data Conflict Detected", e.getMessage(), request, httpStatus);
        return logAndRespond(httpStatus, errorDto);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleNotFoundException(
            ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ApiErrorDto errorDto =
                buildApiErrorDto("Resource not found.", e.getMessage(), request, httpStatus);
        return logAndRespond(httpStatus, errorDto);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorDto> handleBadRequestException(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ApiErrorDto errorDto =
                buildApiErrorDto("Bad request.", e.getMessage(), request, httpStatus);
        return logAndRespond(httpStatus, errorDto);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DirectoryAlreadyExistsException.class)
    public ResponseEntity<ApiErrorDto> handleDirectoryAlreadyExistsException(
            DirectoryAlreadyExistsException e, HttpServletRequest request) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ApiErrorDto errorDto =
                buildApiErrorDto("Already exist.", e.getMessage(), request, httpStatus);
        return logAndRespond(httpStatus, errorDto);
    }

    private static ApiErrorDto buildApiErrorDto(
            String title, String errorDetail, HttpServletRequest request, HttpStatus status) {
        return ApiErrorDto.builder()
                .title(title)
                .detail(errorDetail)
                .status(status.value())
                .uri(request.getRequestURI())
                .build();
    }

    private ResponseEntity<ApiErrorDto> logAndRespond(HttpStatus status, ApiErrorDto apiError) {
        log.error("{}\n{}", apiError.title(), apiError);
        return new ResponseEntity<>(apiError, status);
    }
}
