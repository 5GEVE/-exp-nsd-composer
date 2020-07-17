package it.cnit.blueprint.composer.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/*
{
  "timestamp": "2020-07-17T08:56:13.475+0000",
  "status": 400,
  "error": "Bad Request",
  "message": "JSON parse error: Unrecognized field"
  "path":
 */

@Data
public class ApiError {

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDateTime timestamp;
  private HttpStatus status;
  private String error;
  private String message;
  private String path;

  public ApiError(HttpStatus status, String error, String message, String path) {
    this.timestamp = LocalDateTime.now();
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
  }
}
