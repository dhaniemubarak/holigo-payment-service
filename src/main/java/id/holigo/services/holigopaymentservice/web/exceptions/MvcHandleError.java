package id.holigo.services.holigopaymentservice.web.exceptions;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolationException;

// import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MvcHandleError {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<String>> constraintViolationException(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>(ex.getConstraintViolations().size());
        ex.getConstraintViolations().forEach(exception -> {
            errors.add(exception.getPropertyPath() + ":" + exception.getMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<List<ObjectError>> handleBindException(BindException bind) {
        return new ResponseEntity<List<ObjectError>>(bind.getAllErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

}