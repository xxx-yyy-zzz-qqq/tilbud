package tilbud.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final EntityManager entityManager;

    public CategoryService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<String> findDistinctCategories() {
        String jpql = "SELECT DISTINCT catIds FROM Catalog c JOIN c.categoryIds catIds ORDER BY catIds";
        TypedQuery<String> query = entityManager.createQuery(jpql, String.class);
        return query.getResultList();
    }
}
