package zorvyn.assessment.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import zorvyn.assessment.Enum.Role;
import zorvyn.assessment.Enum.UserStatus;
import zorvyn.assessment.Model.Users;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<Users> getSpecification(String name, String email, Role role, UserStatus status) {
        return new Specification<Users>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Users> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();

                if(name!=null){
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")), "%"+name.toLowerCase()+"%"));
                }
                if(email!=null){
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")), "%" +email.toLowerCase()+"%"));
                }
                if(role!=null){
                    predicates.add(criteriaBuilder.equal(root.get("role"), role));
                }
                if(status!=null){
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }
}
