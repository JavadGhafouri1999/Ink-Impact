package com.weblog.postservice.controller;

import com.weblog.postservice.dto.response.ImageUploadResponse;
import com.weblog.postservice.exception.UnauthorizedException;
import com.weblog.postservice.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

  private final FileStorageService fileStorageService;

  @PostMapping("/images")
  public ResponseEntity <ImageUploadResponse> uplaodEditorImage(
		  @RequestPart MultipartFile image,
		  @RequestHeader("X-User-Id") String userId,
		  @RequestHeader("X-User-Role") String role) {

	if(!role.equals("ADMIN") && !role.equals("AUTHOR") && !role.equals("OWNER")) {
	  throw new UnauthorizedException("You are not authorized to perform this operation");
	}

	String url = fileStorageService.uploadFile(image, "content");

	return ResponseEntity.ok(new ImageUploadResponse(url));
  }
}
