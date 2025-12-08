package com.codeit_team01.sb07_hrbank_team01.backup.controller;

import com.codeit_team01.sb07_hrbank_team01.backup.dto.request.BackupRequestDto;
import com.codeit_team01.sb07_hrbank_team01.backup.dto.response.BackupResponseDto;
import com.codeit_team01.sb07_hrbank_team01.backup.entity.BackupStatus;
import com.codeit_team01.sb07_hrbank_team01.backup.service.BackupService;
import com.codeit_team01.sb07_hrbank_team01.common.doc.BackupControllerDocs;
import com.codeit_team01.sb07_hrbank_team01.common.dto.response.PageResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
public class BackupController implements BackupControllerDocs {

  private final BackupService backupService;

  @GetMapping
  public ResponseEntity<PageResponseDto<BackupResponseDto>> getBackupsByCursor(
      @RequestParam(required = false) String worker,
      @RequestParam(required = false) BackupStatus status,
      @RequestParam(required = false) Instant startedAtFrom,
      @RequestParam(required = false) Instant startedAtTo,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "10") Integer size,
      @RequestParam(required = false, defaultValue = "startedAt") String sortField,
      @RequestParam(required = false, defaultValue = "DESC") String sortDirection
  ) {
    BackupRequestDto backupRequestDto =
        new BackupRequestDto(worker, status, startedAtFrom, startedAtTo, idAfter, cursor, size, sortField, sortDirection);
    PageResponseDto<BackupResponseDto> backupPageList
        = backupService.getBackupPageList(backupRequestDto);
    return ResponseEntity.status(HttpStatus.OK).body(backupPageList);
  }

  @PostMapping
  public ResponseEntity<BackupResponseDto> createBackup(HttpServletRequest request) {
    String worker = (String) request.getAttribute("clientIp");

    if (worker == null) {
      // IP 추출에 실패했거나 인터셉터가 실행되지 않았을 경우, 적절히 처리 (예: Unknown)
      worker = "Unknown IP";
    }

    BackupResponseDto backupResponseDto = backupService.createBackup(worker);
    return ResponseEntity.status(HttpStatus.CREATED).body(backupResponseDto);
  }

  @GetMapping("/latest")
  public ResponseEntity<BackupResponseDto> getLatestBackup(
      @RequestParam(defaultValue = "COMPLETED") BackupStatus status) {
    BackupResponseDto backupResponseDto = backupService.getLatestBackup(status);
    return ResponseEntity.status(HttpStatus.OK).body(backupResponseDto);
  }
}
