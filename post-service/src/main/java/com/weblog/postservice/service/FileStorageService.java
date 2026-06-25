package com.weblog.postservice.service;

import com.weblog.postservice.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

  @Value("${app.storage.post-images-dir:uploads/posts}")
  private String postImagesDir;

  @Value("${app.storage.max-file-size:5242880}") // 5MB
  private long maxFileSize;

  private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "gif"};
  private static final String[] ALLOWED_MIME_TYPES = {
		  "image/jpeg", "image/png", "image/webp", "image/gif"
  };

  public String uploadFile(MultipartFile file, String subdirectory) {
	validateFile(file);
	String filename = generateSecureFileName(file.getOriginalFilename());
	Path targetPath = Paths.get(postImagesDir, subdirectory).resolve(filename);

	log.info("FILE WILL BE SAVED TO: {}", targetPath.toAbsolutePath());

	try {
	  Files.createDirectories(targetPath.getParent());
	  file.transferTo(targetPath.toFile());
	  log.debug("File successfully uploaded to {}/{}", subdirectory, filename);
	  log.info("FILE SAVED TO: {}", targetPath.toAbsolutePath());

	  return "/uploads/posts/" + subdirectory + "/" + filename;
	} catch(IOException e) {
	  throw new FileStorageException("Failed to upload file: " + filename, e);
	}
  }

  public void deletePostImage(String imageUrl) {
	if(imageUrl == null || imageUrl.isBlank()) { return; }

	String relativePath =
			imageUrl.replace("/uploads/posts/", "");

	Path filePath =
			Paths.get(postImagesDir)
				 .resolve(relativePath);

	try {
	  Files.deleteIfExists(filePath);
	  log.debug("Image deleted successfully: {} ", imageUrl);

	} catch(IOException e) {
	  throw new FileStorageException("Failed to delete file: " + imageUrl, e);
	}
  }

  private void validateFile(MultipartFile file) {

	if(file.isEmpty()) {
	  throw new FileStorageException("File is empty");
	}

	if(file.getSize() > maxFileSize) {
	  throw new FileStorageException("The maximum allowed file size is " + maxFileSize + " bytes");
	}

	String originalFilename = file.getOriginalFilename();

	if(originalFilename == null || originalFilename.isBlank()) {
	  throw new FileStorageException("Filename is missing");
	}

	String extension =
			getFileExtension(originalFilename)
					.toLowerCase();

	boolean validExtension =
			Arrays.asList(ALLOWED_EXTENSIONS).contains(extension);

	if(!validExtension) {
	  throw new FileStorageException(
			  "Unsupported extension: " + extension
	  );
	}

	String contentType = file.getContentType();
	boolean validMimeType = false;
	for(String mime : ALLOWED_MIME_TYPES) {
	  if(mime.equals(contentType)) {
		validMimeType = true;
		break;
	  }
	}

	if(!validMimeType) {
	  throw new FileStorageException("The content type is not supported: " + contentType);
	}
  }

  private String generateSecureFileName(String originalFilename) {
	String extension = getFileExtension(originalFilename);
	return UUID.randomUUID() + "." + extension;
  }

  private String getFileExtension(String filename) {

	if(filename == null || filename.isBlank()) {
	  return "";
	}

	int lastDot = filename.lastIndexOf('.');

	return lastDot > 0
		   ? filename.substring(lastDot + 1)
		   : "";
  }
}
