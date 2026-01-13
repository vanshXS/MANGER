package com.vansh.manger.Manger.Controller;

import com.vansh.manger.Manger.DTO.GlobalSearchResponseDTO;
import com.vansh.manger.Manger.Service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
public class AdminSearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<GlobalSearchResponseDTO> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(searchService.performGlobalSearch(query));
    }
}
