package zorvyn.assessment.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import zorvyn.assessment.DTOs.Response.UserResponse;
import zorvyn.assessment.Model.Users;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {

    @Mapping(target = "id", source = "id")
    UserResponse toUserResponse(Users user);
}
