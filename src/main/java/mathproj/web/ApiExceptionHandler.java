package mathproj.web;

import mathproj.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private ResponseEntity<ErrorResponse> err(HttpServletRequest req, HttpStatus status, String msg) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                HttpErrorMapper.fromStatus(status),
                msg,
                req.getRequestURI()
        );
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> invalidJson(HttpServletRequest req) {
        return err(req, HttpStatus.BAD_REQUEST, "Некорректный JSON");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(HttpServletRequest req, IllegalArgumentException e) {
        return err(req, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> conflict(HttpServletRequest req) {
        return err(req, HttpStatus.CONFLICT, "Пользователь уже существует");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> internal(HttpServletRequest req) {
        return err(req, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> notFound(HttpServletRequest req) {
        return err(req, HttpStatus.NOT_FOUND, "Не найдено");
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> forbidden(HttpServletRequest req) {
        return err(req, HttpStatus.FORBIDDEN, "Доступ запрещён");
    }
}


