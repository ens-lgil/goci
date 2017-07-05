package uk.ac.ebi.spot.goci.repository;


import org.springframework.data.jpa.domain.Specification;
import uk.ac.ebi.spot.goci.model.Study;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.StaticMetamodel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cinzia on 04/07/2017.
 */

public class StudySpecifications implements Specification<Study> {

    private final Study example;

    public StudySpecifications(Study example) {
        this.example = example;
        }

    @Override
    public Predicate toPredicate(Root<Study> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        Class clazz = cq.getResultType();
        if (clazz.equals(Long.class) || clazz.equals(long.class))
            return null;

        //building the desired query
        root.fetch("diseaseTrait", JoinType.LEFT);
        root.fetch("housekeeping", JoinType.LEFT);
        cq.distinct(true);


        if (example.getTitle() != null) {
            //predicates.add(cb.like(cb.lower(root.get(Study_.title)), example.getTitle().toLowerCase() + "%"));
            predicates.add(cb.like(cb.lower(root.get("title")), example.getTitle().toLowerCase() + "%"));
            System.out.println("predicato");
        }

        if (example.getAuthor() != null) {
            //predicates.add(cb.like(cb.lower(root.get(Study_.title)), example.getTitle().toLowerCase() + "%"));
            predicates.add(cb.like(cb.lower(root.get("author")), example.getAuthor().toLowerCase() + "%"));
            System.out.println("predicato");
        }

        return andTogether(predicates, cb);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
