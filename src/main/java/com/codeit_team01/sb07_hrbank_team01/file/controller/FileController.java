package com.codeit_team01.sb07_hrbank_team01.file.controller;

import com.codeit_team01.sb07_hrbank_team01.common.doc.FileControllerDocs;
import com.codeit_team01.sb07_hrbank_team01.file.dto.FileResponseDto;
import com.codeit_team01.sb07_hrbank_team01.file.service.MetaFileService;
import com.codeit_team01.sb07_hrbank_team01.file.storage.FileLocalStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController implements FileControllerDocs {

    private final MetaFileService metaFileService;
    private final FileLocalStorage fileLocalStorage;

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileResponseDto file = metaFileService.findById(id);
        return fileLocalStorage.download(file);
    }
}
