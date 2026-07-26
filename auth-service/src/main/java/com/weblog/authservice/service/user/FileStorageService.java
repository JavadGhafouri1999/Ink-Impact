package com.weblog.authservice.service.user;

import com.weblog.authservice.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@RefreshScope
public class FileStorageService {

  // NOTE: uploadDir is technically @RefreshScope-eligible, but changing it
  // live creates a split-brain with FileStorageConfig's resource handler,
  // which is registered once at startup and never refreshes. Changing
  // app.storage.auth-images-dir requires a full service restart.
  @Value("${app.storage.auth-images-dir}")
  private String uploadDir;

  // 5MB — safe to refresh live
  @Value("${app.storage.max-file-size:5242880}")
  private long maxFileSize;

  private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "gif"};
  private static final String[] ALLOWED_MIME_TYPES = {"image/jpeg", "image/png", "image/webp", "image/gif"};

  public String saveAvatar(MultipartFile file) {
	validateFile(file);

	String filename  = generateSecureFileName(file.getOriginalFilename());
	Path   avatarDir = Paths.get(uploadDir, "avatars");

	log.info("AVATAR WILL BE SAVED TO: {}", avatarDir.toAbsolutePath());

	try {
	  Files.createDirectories(avatarDir);
	  Path target = avatarDir.resolve(filename);
	  file.transferTo(target);
	  log.info("FILE SAVED TO: {}", avatarDir.toAbsolutePath());

	  return "/uploads/auth/avatars/" + filename;
	} catch (IOException e) {
	  throw new FileStorageException("Failed to save avatar: " + filename, e);
	}
  }

  public void deleteAvatar(String avatarUrl) {
	if (avatarUrl == null || avatarUrl.isBlank()) {
	  return;
	}

	String filename = Paths.get(avatarUrl).getFileName().toString();

	try {
	  Files.deleteIfExists(
			  Paths.get(uploadDir, "avatars", filename));
	  log.debug("Avatar deleted successfully: {} ", avatarUrl);
	} catch (IOException e) {
	  throw new RuntimeException("Failed to save avatar", e);
	}
  }

  private void validateFile(MultipartFile file) {

	if (file.isEmpty()) {
	  throw new FileStorageException("File is empty");
	}

	if (file.getSize() > maxFileSize) {
	  throw new FileStorageException("The maximum allowed file size is " + maxFileSize + " bytes");
	}

	String originalFilename = file.getOriginalFilename();

	if (originalFilename == null || originalFilename.isBlank()) {
	  throw new FileStorageException("Filename is missing");
	}

	String extension =
			getFileExtension(originalFilename)
					.toLowerCase();

	boolean validExtension =
			Arrays.asList(ALLOWED_EXTENSIONS).contains(extension);

	if (!validExtension) {
	  throw new FileStorageException(
			  "Unsupported extension: " + extension
	  );
	}

	String  contentType   = file.getContentType();
	boolean validMimeType = false;
	for (String mime : ALLOWED_MIME_TYPES) {
	  if (mime.equals(contentType)) {
		validMimeType = true;
		break;
	  }
	}

	if (!validMimeType) {
	  throw new FileStorageException("The content type is not supported: " + contentType);
	}
  }

  private String generateSecureFileName(String originalFilename) {
	String extension = getFileExtension(originalFilename);
	return UUID.randomUUID() + "." + extension;
  }

  private String getFileExtension(String filename) {

	if (filename == null || filename.isBlank()) {
	  return "";
	}

	int lastDot = filename.lastIndexOf('.');

	return lastDot > 0
	       ? filename.substring(lastDot + 1)
	       : "";
  }
}
