package zorvyn.assessment.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import zorvyn.assessment.DTOs.Response.RecordResponse;
import zorvyn.assessment.Model.Record;

@Mapper(componentModel = "spring")
public interface RecordResponseMapper {

    @Mapping(target = "createdBy", expression = "java(record.getCreatedBy().getName())")
    RecordResponse toRecordResponse(Record record);
}
