package tilbud.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import tilbud.entity.Catalog;
import tilbud.repository.CatalogRepository;

import java.util.List;
import java.util.TreeSet;

@Service
public class CategoryService {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final CatalogRepository catalogRepository;

    public CategoryService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public List<String> findDistinctCategories() {
        TreeSet<String> categories = new TreeSet<>();
        for (Catalog catalog : catalogRepository.findAll()) {
            if (catalog.getCategoryIds() != null && !catalog.getCategoryIds().isBlank()) {
                try {
                    List<String> ids = mapper.readValue(catalog.getCategoryIds(), new TypeReference<List<String>>() {});
                    categories.addAll(ids);
                } catch (Exception e) {
                    // skip malformed JSON
                }
            }
        }
        return List.copyOf(categories);
    }
}
