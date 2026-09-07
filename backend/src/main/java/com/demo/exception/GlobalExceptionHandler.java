package com.demo.exception;

import com.demo.dto.ErrorResponseDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. معالجة إيرورز الـ Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "فشل التحقق من صحة البيانات المدخلة", fieldErrors)
        );
    }

    // 2. معالجة فشل تحديث التوكن / توكن منتهي الصلاحية (403 Forbidden)
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponseDto> handleTokenRefreshException(TokenRefreshException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), ex.getMessage())
        );
    }

    // 3. معالجة تكرار الموارد المخصصة (409 Conflict)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateResource(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponseDto(HttpStatus.CONFLICT.value(), ex.getMessage())
        );
    }

    // 4. معالجة عدم وجود العنصر (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponseDto(HttpStatus.NOT_FOUND.value(), ex.getMessage())
        );
    }

    // 5. معالجة ResponseStatusException
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(
                new ErrorResponseDto(ex.getStatusCode().value(), ex.getReason())
        );
    }

    // 6. معالجة خطأ قراءة الـ JSON Payload (400 Bad Request)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "تنسيق البيانات المدخلة (JSON) غير صحيح.")
        );
    }

    // 7. معالجة اختلاف نوع البيانات في الـ URL مثل /tasks/abc (400 Bad Request)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), "نوع المعامل في الرابط غير صحيح: " + ex.getName())
        );
    }

    // 8. معالجة عدم وجود الصلاحيات الكافية (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), "عفواً، ليس لديك الصلاحية الكافية للوصول لهذا المورد.")
        );
    }

    // 9. معالجة فشل تسجيل الدخول أو التوكن غير المعتمد (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "فشل التحقق من الهوية: اسم المستخدم أو كلمة المرور غير صحيحة.")
        );
    }

    // 10. معالجة تكرار القيود الفريدة في الداتابيز كالإيميل المكرر (409 Conflict)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponseDto(HttpStatus.CONFLICT.value(), "هذه البيانات مسجلة بالفعل في النظام.")
        );
    }

    // 11. حائط الدفاع الأخير لأي خطأ غير متوقع (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "حدث خطأ غير متوقع في السيرفر: " + ex.getMessage())
        );
    }
}