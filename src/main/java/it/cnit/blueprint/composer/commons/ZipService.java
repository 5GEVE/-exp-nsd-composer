package it.cnit.blueprint.composer.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ZipService {

  public ResponseEntity<InputStreamResource> getZipResponse(List<File> files) throws IOException {
    File zipFile = Files.createTempFile("enc-output-", ".zip").toFile();
    ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
    for (File f : files) {
      zipOut.putNextEntry(new ZipEntry(f.getName()));
      FileInputStream fis = new FileInputStream(f);
      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
      fis.close();
    }
    zipOut.close();
    ResponseEntity<InputStreamResource> response = ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(zipFile.length())
        .body(new InputStreamResource(new FileInputStream(zipFile)));
    //noinspection ResultOfMethodCallIgnored
    zipFile.delete();
    return response;
  }

}
