package id.my.agungdh.resource;

import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import id.my.agungdh.dto.ErrorResponse;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.ConflictException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ExceptionMappers {

    @ServerExceptionMapper
    public Response mapNotFound(NotFoundException exception) {
        return error(Response.Status.NOT_FOUND, exception.getMessage());
    }

    @ServerExceptionMapper
    public Response mapConflict(ConflictException exception) {
        return error(Response.Status.CONFLICT, exception.getMessage());
    }

    @ServerExceptionMapper
    public Response mapBadRequest(BadRequestException exception) {
        return error(Response.Status.BAD_REQUEST, exception.getMessage());
    }

    @ServerExceptionMapper
    public Response mapValidation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining(", "));

        return error(Response.Status.BAD_REQUEST, message.isBlank() ? "Data tidak valid." : message);
    }

    private Response error(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse(message == null ? status.getReasonPhrase() : message))
                .build();
    }
}
