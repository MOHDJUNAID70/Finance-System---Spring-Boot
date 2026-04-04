package zorvyn.assessment.Repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import zorvyn.assessment.Model.Users;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer>, JpaSpecificationExecutor<Users> {

     Optional<Users> findByEmail(@NonNull String email);
}
