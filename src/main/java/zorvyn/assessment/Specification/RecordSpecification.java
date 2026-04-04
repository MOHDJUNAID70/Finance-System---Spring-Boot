package zorvyn.assessment.Specification;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import zorvyn.assessment.Enum.RecordType;
import zorvyn.assessment.Model.Record;
import java.util.ArrayList;
import java.util.List;

public class RecordSpecification {

    public static Specification<Record> getSpecification(Integer minAmount, Integer maxAmount,
                                     RecordType type, String category, boolean deleted)
    {
        return new Specification<Record>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Record> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();

                if(minAmount != null && maxAmount != null){
                    predicates.add(criteriaBuilder.and(
                            criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount),
                            criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount)
                    ));
                }
                if(type != null){
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                }
                if(category != null){
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("category")), "%"+ category.toLowerCase()+ "%"));
                }
                if(deleted){
                    predicates.add(criteriaBuilder.equal(root.get("deleted"), deleted));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
