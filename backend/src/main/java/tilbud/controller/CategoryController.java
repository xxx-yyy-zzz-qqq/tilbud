package tilbud.controller;

import org.springframework.web.bind.annotation.*;
import tilbud.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<String> list() {
        return categoryService.findDistinctCategories();
    }
}
