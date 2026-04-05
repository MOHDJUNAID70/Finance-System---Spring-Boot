package zorvyn.assessment.Service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import zorvyn.assessment.DTOs.Request.RecordRequest;
import zorvyn.assessment.DTOs.Response.RecordResponse;
import zorvyn.assessment.Enum.RecordType;
import zorvyn.assessment.Enum.Role;
import zorvyn.assessment.Exception.CustomException;
import zorvyn.assessment.Idempotency.IdempotencyKey;
import zorvyn.assessment.Idempotency.IdempotencyRepository;
import zorvyn.assessment.Mapper.RecordResponseMapper;
import zorvyn.assessment.Model.Record;
import zorvyn.assessment.Model.Users;
import zorvyn.assessment.Repository.RecordRepository;
import zorvyn.assessment.Repository.UserRepository;
import zorvyn.assessment.Specification.RecordSpecification;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class RecordService {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecordResponseMapper recordResponseMapper;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    public List<RecordResponse> getAllRecord(String email) {

        Users user=userRepository.findByEmail(email).orElseThrow(
                ()-> new CustomException("Please register yourself first to see your activity"));

        if(user.getRole() == Role.ANALYST || user.getRole() == Role.VIEWER){
            List<Record> records=recordRepository.findByCreatedBy(user);

            if(records.isEmpty()) {
                throw new CustomException("No records found");
            }
            return records.stream().map(recordResponseMapper::toRecordResponse).toList();
        }
        List<Record> records = recordRepository.findAll();

        if(records.isEmpty()) {
            throw new CustomException("No records found");
        }

        return records.stream().map(recordResponseMapper::toRecordResponse).toList();
    }

    @Transactional
    public ResponseEntity<String> createRecordWithIdempotencyCheck(String key, @Valid RecordRequest recordRequest, Authentication authentication) {

        String requesthash=generateHash(recordRequest, authentication);

        IdempotencyKey existingKey=idempotencyRepository.findByIdempotencyKeyAndExpiresAtAfter(key, LocalDateTime.now());

        if(existingKey!=null) {
            if(!existingKey.getRequestHash().equals(requesthash)) {
                throw new CustomException("Idempotency key already used with different request data");
            }
            return ResponseEntity.ok(existingKey.getBody());
        }

        createRecord(recordRequest, authentication);

        IdempotencyKey newKey=new IdempotencyKey();
        newKey.setRequestHash(requesthash);
        newKey.setIdempotencyKey(key);
        newKey.setCreatedAt(LocalDateTime.now());
        newKey.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        newKey.setBody("Your Record created successfully");
        idempotencyRepository.save(newKey);
        return new ResponseEntity<>(newKey.getBody(), HttpStatus.CREATED);
    }

    private String generateHash(RecordRequest recordRequest,  Authentication authentication) {
        return DigestUtils.md5DigestAsHex((
                        recordRequest.getAmount().toString()+"|"+
                        recordRequest.getCategory()+"|"+
                        recordRequest.getDate().toString()+"|"+
                        recordRequest.getType().toString()+"|"+
                        (recordRequest.getDescription()!=null?recordRequest.getDescription():"") +"|"+
                        authentication.getName()).getBytes(StandardCharsets.UTF_8)
        );
    }

    @Transactional
    public void createRecord(RecordRequest recordRequest, Authentication authentication) {

        Users user=userRepository.findByEmail(authentication.getName())
                .orElseThrow(()-> new CustomException("You are not authorized to perform this operation"));

        Record records=recordRepository.findByAmountAndTypeAndCategoryAndDateAndCreatedByAndDeletedFalse(
                recordRequest.getAmount(),
                recordRequest.getType(),
                recordRequest.getCategory(),
                recordRequest.getDate(),
                user
        );
         if(records!=null) {
             throw new CustomException("Duplicate record exists with same amount, type, category, date and user");
         }

        Record record = new Record();
        record.setAmount(recordRequest.getAmount());
        record.setType(recordRequest.getType());
        record.setDescription(recordRequest.getDescription());
        record.setDate(recordRequest.getDate());
        record.setCreatedBy(user);
        record.setCategory(recordRequest.getCategory());
        recordRepository.save(record);
    }

    public RecordResponse getRecordById(Integer id, Authentication  authentication) {
        Record record=recordRepository.findById(id)
                .orElseThrow(()-> new CustomException("Record not exists with this ID "+id));

        if(!Objects.equals(record.getCreatedBy().getEmail(), authentication.getName())) {
            throw new CustomException("This record does not belong to the you");
        }

        return recordResponseMapper.toRecordResponse(record);
    }

    public List<RecordResponse> getAllRecordByUser(Integer id) {
        Users user=userRepository.findById(id)
                .orElseThrow(()-> new CustomException("User with id "+id+" not found"));
        List<Record> records=recordRepository.findByCreatedBy(user);
        if(records.isEmpty()) {
            throw new CustomException("No records found for user with id "+id);
        }
        return records.stream().map(recordResponseMapper::toRecordResponse).toList();
    }

    @Transactional
    public void updateRecord(Integer id, RecordRequest recordRequest, String name) {
        Record record=recordRepository.findById(id)
                .orElseThrow(()->new  CustomException("No record exists with this ID "+id));

        Users currentUser=userRepository.findByEmail(name)
                .orElseThrow(()-> new CustomException("You are not authorized to perform this operation"));

        if(currentUser.getRole() == Role.ANALYST && !Objects.equals(record.getCreatedBy().getEmail(), name)){
            throw new CustomException("You can only update your own records");
        }

        if(record.isDeleted()){
            throw new CustomException("This record is deleted and cannot be updated");
        }
        record.setAmount(recordRequest.getAmount());
        record.setType(recordRequest.getType());
        record.setDescription(recordRequest.getDescription());
        record.setDate(recordRequest.getDate());
        record.setCategory(recordRequest.getCategory());
        recordRepository.save(record);
    }

    @Transactional
    public void deleteRecordById(Integer id) {
        Record record=recordRepository.findById(id)
                .orElseThrow(()-> new CustomException("Record not exists with this ID "+id));
        if(record.isDeleted()){
            throw new CustomException("Record with id "+id+" is already deleted");
        }
        record.setDeleted(true);
        recordRepository.save(record);
    }

    public Page<RecordResponse> getAllRecordWithPaginationAndFilter(Pageable pageable,Integer minAmount,
                    Integer maxAmount, RecordType type, String category, boolean deleted)
    {
        Specification<Record> specification=RecordSpecification.getSpecification(
                                                                    minAmount,
                                                                    maxAmount,
                                                                    type,
                                                                    category,
                                                                    deleted);

        return recordRepository.findAll(specification, pageable).map(recordResponseMapper::toRecordResponse);
    }
}
