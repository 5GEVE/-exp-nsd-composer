package it.cnit.blueprint.composer.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
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
  private int status;
  private String error;
  private String message;

  public ApiError(HttpStatus status, String message) {
    this.timestamp = LocalDateTime.now();
    this.status = status.value();
    this.error = status.getReasonPhrase();
    this.message = message;
  }
}
