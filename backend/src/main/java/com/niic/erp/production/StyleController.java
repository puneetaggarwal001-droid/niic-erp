package com.niic.erp.production;

import com.niic.erp.production.dto.StyleDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/styles")
public class StyleController {

    private final StyleRepository styleRepository;

    public StyleController(StyleRepository styleRepository) {
        this.styleRepository = styleRepository;
    }

    @GetMapping
    public List<StyleDto> list() {
        return styleRepository.findAll().stream().map(StyleDto::from).toList();
    }
}
