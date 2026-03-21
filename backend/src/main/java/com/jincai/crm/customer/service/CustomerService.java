package com.jincai.crm.customer.service;

import com.jincai.crm.customer.controller.*;
import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScope;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.org.entity.AppUser;
import com.jincai.crm.org.repository.AppUserRepository;
import com.jincai.crm.org.entity.Department;
import com.jincai.crm.org.repository.DepartmentRepository;
import com.jincai.crm.security.LoginUser;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TravelerRepository travelerRepository;
    private final DataScopeResolver dataScopeResolver;
    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public CustomerService(CustomerRepository customerRepository, TravelerRepository travelerRepository,
                           DataScopeResolver dataScopeResolver, AppUserRepository userRepository,
                           DepartmentRepository departmentRepository) {
        this.customerRepository = customerRepository;
        this.travelerRepository = travelerRepository;
        this.dataScopeResolver = dataScopeResolver;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<CustomerView> listVisible(LoginUser user) {
        if (user == null) {
            return List.of();
        }
        List<Customer> customers;
        if (user.getDataScope() == DataScope.ALL) {
            customers = customerRepository.findByDeletedFalse();
        } else if (user.getDataScope() == DataScope.SELF) {
            customers = customerRepository.findByOwnerUserIdAndDeletedFalse(user.getUserId());
        } else if (user.getDataScope() == DataScope.DEPARTMENT) {
            customers = customerRepository.findByOwnerDeptIdAndDeletedFalse(user.getDepartmentId());
        } else {
            Set<Long> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
            if (departmentIds.isEmpty()) {
                return List.of();
            }
            customers = customerRepository.findByOwnerDeptIdInAndDeletedFalse(departmentIds);
        }
        return toViews(customers);
    }

    public CustomerView create(CustomerRequest request, LoginUser user) {
        if (user == null) {
            throw new BusinessException("Unauthenticated");
        }
        Customer customer = new Customer();
        applyCustomer(customer, request);
        customer.setOwnerUserId(request.ownerUserId() == null ? user.getUserId() : request.ownerUserId());
        customer.setOwnerDeptId(request.ownerDeptId() == null ? user.getDepartmentId() : request.ownerDeptId());
        return toView(customerRepository.save(customer));
    }

    public CustomerView update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new BusinessException("Customer not found"));
        applyCustomer(customer, request);
        if (request.ownerUserId() != null) {
            customer.setOwnerUserId(request.ownerUserId());
        }
        if (request.ownerDeptId() != null) {
            customer.setOwnerDeptId(request.ownerDeptId());
        }
        return toView(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new BusinessException("Customer not found"));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }

    public List<Traveler> listTravelers(Long customerId) {
        return travelerRepository.findByCustomerIdAndDeletedFalse(customerId);
    }

    public List<Traveler> listTravelersVisible(Long customerId) {
        if (customerId != null) {
            return travelerRepository.findByCustomerIdAndDeletedFalse(customerId);
        }
        return travelerRepository.findByDeletedFalse();
    }

    public Traveler addTraveler(Long customerId, TravelerRequest request) {
        customerRepository.findById(customerId).orElseThrow(() -> new BusinessException("Customer not found"));
        Traveler traveler = new Traveler();
        traveler.setCustomerId(customerId);
        applyTraveler(traveler, request);
        return travelerRepository.save(traveler);
    }

    public Traveler updateTraveler(Long travelerId, TravelerRequest request) {
        Traveler traveler = travelerRepository.findById(travelerId)
            .orElseThrow(() -> new BusinessException("Traveler not found"));
        customerRepository.findById(traveler.getCustomerId()).orElseThrow(() -> new BusinessException("Customer not found"));
        applyTraveler(traveler, request);
        return travelerRepository.save(traveler);
    }

    public void deleteTraveler(Long travelerId) {
        Traveler traveler = travelerRepository.findById(travelerId)
            .orElseThrow(() -> new BusinessException("Traveler not found"));
        traveler.setDeleted(true);
        travelerRepository.save(traveler);
    }

    private void applyTraveler(Traveler traveler, TravelerRequest request) {
        traveler.setName(request.name());
        traveler.setIdType(request.idType());
        traveler.setIdNo(request.idNo());
        traveler.setPhone(request.phone());
        traveler.setEmergencyContact(request.emergencyContact());
        traveler.setPreferences(request.preferences());
        if (request.birthday() != null && !request.birthday().isBlank()) {
            try {
                traveler.setBirthday(LocalDate.parse(request.birthday(), DateTimeFormatter.ISO_DATE));
            } catch (DateTimeParseException ex) {
                throw new BusinessException("Invalid birthday format, expected yyyy-MM-dd");
            }
        }
    }

    private void applyCustomer(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setCustomerType(defaultValue(request.customerType(), "PERSONAL"));
        customer.setSource(defaultValue(request.source(), "MANUAL"));
        customer.setIntentionLevel(defaultValue(request.intentionLevel(), "MEDIUM"));
        customer.setStatus(defaultValue(request.status(), "ACTIVE"));
        customer.setLevel(defaultValue(request.level(), "B"));
        customer.setWechat(request.wechat());
        customer.setEmail(request.email());
        customer.setCity(request.city());
        customer.setTags(request.tags());
        customer.setRemark(request.remark());
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public ImportResult importCustomers(MultipartFile file, LoginUser user) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        try (var input = file.getInputStream(); var workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String name = formatter.formatCellValue(row.getCell(0)).trim();
                String phone = formatter.formatCellValue(row.getCell(1)).trim();
                String source = formatter.formatCellValue(row.getCell(2)).trim();
                String level = formatter.formatCellValue(row.getCell(3)).trim();
                if (name.isBlank() || phone.isBlank()) {
                    errors.add("Row " + (i + 1) + " missing name/phone");
                    continue;
                }
                Customer customer = new Customer();
                customer.setName(name);
                customer.setPhone(phone);
                customer.setSource(defaultValue(source, "MANUAL"));
                customer.setLevel(defaultValue(level, "B"));
                customer.setCustomerType("PERSONAL");
                customer.setIntentionLevel("MEDIUM");
                customer.setStatus("ACTIVE");
                customer.setOwnerUserId(user.getUserId());
                customer.setOwnerDeptId(user.getDepartmentId());
                customerRepository.save(customer);
                success++;
            }
        } catch (IOException ex) {
            throw new BusinessException("Failed to parse file: " + ex.getMessage());
        }
        return new ImportResult(success, errors.size(), errors);
    }

    private List<CustomerView> toViews(List<Customer> customers) {
        Map<Long, String> userNameMap = userRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(AppUser::getId, AppUser::getFullName, (a, b) -> a, LinkedHashMap::new));
        Map<Long, String> departmentNameMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a, LinkedHashMap::new));
        return customers.stream().map(customer -> toView(customer, userNameMap, departmentNameMap)).toList();
    }

    private CustomerView toView(Customer customer) {
        Map<Long, String> userNameMap = userRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(AppUser::getId, AppUser::getFullName, (a, b) -> a, LinkedHashMap::new));
        Map<Long, String> departmentNameMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a, LinkedHashMap::new));
        return toView(customer, userNameMap, departmentNameMap);
    }

    private CustomerView toView(Customer customer, Map<Long, String> userNameMap, Map<Long, String> departmentNameMap) {
        return new CustomerView(
            customer.getId(),
            customer.getName(),
            customer.getPhone(),
            customer.getCustomerType(),
            customer.getSource(),
            customer.getIntentionLevel(),
            customer.getStatus(),
            customer.getLevel(),
            customer.getWechat(),
            customer.getEmail(),
            customer.getCity(),
            customer.getTags(),
            customer.getRemark(),
            customer.getOwnerUserId(),
            userNameMap.get(customer.getOwnerUserId()),
            customer.getOwnerDeptId(),
            departmentNameMap.get(customer.getOwnerDeptId()),
            customer.getCreatedBy(),
            customer.getUpdatedBy(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
