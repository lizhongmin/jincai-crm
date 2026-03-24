package com.jincai.crm.customer.service;

import com.jincai.crm.customer.dto.*;
import com.jincai.crm.customer.entity.*;
import com.jincai.crm.customer.repository.*;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScope;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.common.I18nService;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.security.LoginUser;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.repository.AppUserRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TravelerRepository travelerRepository;
    private final DataScopeResolver dataScopeResolver;
    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final I18nService i18nService;

    public CustomerService(CustomerRepository customerRepository, TravelerRepository travelerRepository,
                           DataScopeResolver dataScopeResolver, AppUserRepository userRepository,
                           DepartmentRepository departmentRepository, I18nService i18nService) {
        this.customerRepository = customerRepository;
        this.travelerRepository = travelerRepository;
        this.dataScopeResolver = dataScopeResolver;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.i18nService = i18nService;
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

    public PageResult<CustomerView> pageVisible(LoginUser user, int page, int size, String keyword, String tab, String ownerScope) {
        if (user == null) {
            return new PageResult<>(List.of(), 0, normalizePage(page), normalizeSize(size));
        }

        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        Specification<Customer> spec = buildCustomerSpec(user, keyword, tab, ownerScope);
        Page<Customer> result = customerRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        return new PageResult<>(toViews(result.getContent()), result.getTotalElements(), normalizedPage, normalizedSize);
    }

    public CustomerView create(CustomerRequest request, LoginUser user) {
        if (user == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        Customer customer = new Customer();
        applyCustomer(customer, request);
        OwnerAssignment ownerAssignment = resolveOwnerByUserId(
            request.ownerUserId() == null ? user.getUserId() : request.ownerUserId(),
            user
        );
        customer.setOwnerUserId(ownerAssignment.ownerUserId());
        customer.setOwnerDeptId(ownerAssignment.ownerDeptId());
        return toView(customerRepository.save(customer));
    }

    public CustomerView update(Long id, CustomerRequest request, LoginUser user) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new BusinessException("error.customer.notFound"));
        applyCustomer(customer, request);
        if (request.ownerUserId() != null) {
            OwnerAssignment ownerAssignment = resolveOwnerByUserId(request.ownerUserId(), user);
            customer.setOwnerUserId(ownerAssignment.ownerUserId());
            customer.setOwnerDeptId(ownerAssignment.ownerDeptId());
        }
        return toView(customerRepository.save(customer));
    }

    public List<CustomerOwnerOptionView> listOwnerOptions(LoginUser user) {
        Map<Long, Department> departmentMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, department -> department, (a, b) -> a, LinkedHashMap::new));
        return listAssignableUsers(user).stream()
            .filter(candidate -> Boolean.TRUE.equals(candidate.getEnabled()))
            .sorted(Comparator.comparing(AppUser::getFullName, String.CASE_INSENSITIVE_ORDER))
            .map(candidate -> new CustomerOwnerOptionView(
                candidate.getId(),
                candidate.getUsername(),
                candidate.getFullName(),
                candidate.getDepartmentId(),
                buildDepartmentPath(candidate.getDepartmentId(), departmentMap)
            ))
            .toList();
    }

    public void delete(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new BusinessException("error.customer.notFound"));
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
        customerRepository.findById(customerId).orElseThrow(() -> new BusinessException("error.customer.notFound"));
        Traveler traveler = new Traveler();
        traveler.setCustomerId(customerId);
        applyTraveler(traveler, request);
        return travelerRepository.save(traveler);
    }

    public Traveler updateTraveler(Long travelerId, TravelerRequest request) {
        Traveler traveler = travelerRepository.findById(travelerId)
            .orElseThrow(() -> new BusinessException("error.traveler.notFound"));
        customerRepository.findById(traveler.getCustomerId()).orElseThrow(() -> new BusinessException("error.customer.notFound"));
        applyTraveler(traveler, request);
        return travelerRepository.save(traveler);
    }

    public void deleteTraveler(Long travelerId) {
        Traveler traveler = travelerRepository.findById(travelerId)
            .orElseThrow(() -> new BusinessException("error.traveler.notFound"));
        traveler.setDeleted(true);
        travelerRepository.save(traveler);
    }

    private void applyTraveler(Traveler traveler, TravelerRequest request) {
        traveler.setName(request.name());
        traveler.setGender(blankToNull(request.gender()));
        traveler.setEthnicity(blankToNull(request.ethnicity()));
        traveler.setNationality(blankToNull(request.nationality()));
        traveler.setAddress(blankToNull(request.address()));
        traveler.setPhone(request.phone());
        traveler.setEmergencyContact(blankToNull(request.emergencyContact()));
        traveler.setEmergencyPhone(blankToNull(request.emergencyPhone()));
        traveler.setPreferences(blankToNull(request.preferences()));

        List<TravelerDocument> documents = normalizeDocuments(traveler, request);
        traveler.getDocuments().clear();
        traveler.getDocuments().addAll(documents);
        if (!traveler.getDocuments().isEmpty()) {
            TravelerDocument primary = traveler.getDocuments().get(0);
            traveler.setIdType(primary.getDocType());
            traveler.setIdNo(primary.getDocNo());
        } else {
            traveler.setIdType(blankToNull(request.idType()));
            traveler.setIdNo(blankToNull(request.idNo()));
        }

        TravelerDocument idCardDocument = traveler.getDocuments().stream()
            .filter(document -> "ID_CARD".equals(document.getDocType()))
            .findFirst()
            .orElseThrow(() -> new BusinessException("error.traveler.idCard.required"));
        traveler.setBirthday(extractBirthdayFromIdCard(idCardDocument.getDocNo()));
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private List<TravelerDocument> normalizeDocuments(Traveler traveler, TravelerRequest request) {
        List<TravelerDocument> documents = new ArrayList<>();
        if (request.documents() != null) {
            for (TravelerDocumentRequest documentRequest : request.documents()) {
                if (documentRequest == null) {
                    continue;
                }
                String docType = blankToNull(documentRequest.docType());
                String docNo = blankToNull(documentRequest.docNo());
                if (docType == null || docNo == null) {
                    continue;
                }
                TravelerDocument document = new TravelerDocument();
                document.setTraveler(traveler);
                document.setDocType(normalizeDocType(docType));
                document.setDocNo(docNo);
                documents.add(document);
            }
        }
        if (!documents.isEmpty()) {
            return documents;
        }
        String legacyIdType = blankToNull(request.idType());
        String legacyIdNo = blankToNull(request.idNo());
        if (legacyIdType == null || legacyIdNo == null) {
            return documents;
        }
        TravelerDocument document = new TravelerDocument();
        document.setTraveler(traveler);
        document.setDocType(normalizeDocType(legacyIdType));
        document.setDocNo(legacyIdNo);
        documents.add(document);
        return documents;
    }

    private String normalizeDocType(String rawDocType) {
        String value = rawDocType == null ? "" : rawDocType.trim();
        String upper = value.toUpperCase();
        if ("ID_CARD".equals(upper) || value.contains("身份证")) {
            return "ID_CARD";
        }
        if ("PASSPORT".equals(upper) || value.contains("护照")) {
            return "PASSPORT";
        }
        if ("HK_MACAO_PASS".equals(upper) || value.contains("港澳")) {
            return "HK_MACAO_PASS";
        }
        if ("TAIWAN_PASS".equals(upper) || value.contains("台胞") || value.contains("台湾")) {
            return "TAIWAN_PASS";
        }
        if ("MILITARY_ID".equals(upper) || value.contains("军官")) {
            return "MILITARY_ID";
        }
        return upper;
    }

    private LocalDate extractBirthdayFromIdCard(String idNo) {
        if (idNo == null) {
            throw new BusinessException("error.traveler.idCard.noRequired");
        }
        String normalized = idNo.trim().toUpperCase();
        String datePart;
        if (normalized.matches("^\\d{17}[\\dX]$")) {
            datePart = normalized.substring(6, 14);
        } else if (normalized.matches("^\\d{15}$")) {
            datePart = "19" + normalized.substring(6, 12);
        } else {
            throw new BusinessException("error.traveler.idCard.invalidFormat");
        }
        try {
            return LocalDate.parse(datePart, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("error.traveler.idCard.invalidBirthday");
        }
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
                    errors.add(i18nService.getMessage("error.import.rowMissingNamePhone", i + 1));
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
            throw new BusinessException("error.file.parseFailed", ex.getMessage());
        }
        return new ImportResult(success, errors.size(), errors);
    }

    private List<AppUser> listAssignableUsers(LoginUser user) {
        if (user == null) {
            return List.of();
        }
        if (user.getDataScope() == DataScope.ALL) {
            return userRepository.findByDeletedFalse();
        }
        if (user.getDataScope() == DataScope.SELF) {
            return userRepository.findByIdAndDeletedFalse(user.getUserId()).stream().toList();
        }
        if (user.getDataScope() == DataScope.DEPARTMENT) {
            if (user.getDepartmentId() == null) {
                return List.of();
            }
            return userRepository.findByDepartmentIdAndDeletedFalse(user.getDepartmentId());
        }
        Set<Long> departmentIds = dataScopeResolver.resolveDepartmentIds(user);
        if (departmentIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findByDepartmentIdInAndDeletedFalse(departmentIds);
    }

    private OwnerAssignment resolveOwnerByUserId(Long ownerUserId, LoginUser operator) {
        if (operator == null) {
            throw new BusinessException("error.auth.unauthenticated");
        }
        AppUser owner = userRepository.findByIdAndDeletedFalse(ownerUserId)
            .orElseThrow(() -> new BusinessException("error.user.notFound"));
        if (!Boolean.TRUE.equals(owner.getEnabled())) {
            throw new BusinessException("error.customer.owner.disabled");
        }
        if (!canAssignOwner(operator, owner)) {
            throw new BusinessException("error.customer.owner.outOfScope");
        }
        return new OwnerAssignment(owner.getId(), owner.getDepartmentId());
    }

    private boolean canAssignOwner(LoginUser operator, AppUser owner) {
        if (operator.getDataScope() == DataScope.ALL) {
            return true;
        }
        if (operator.getDataScope() == DataScope.SELF) {
            return Objects.equals(operator.getUserId(), owner.getId());
        }
        if (operator.getDataScope() == DataScope.DEPARTMENT) {
            return Objects.equals(operator.getDepartmentId(), owner.getDepartmentId());
        }
        Set<Long> departmentIds = dataScopeResolver.resolveDepartmentIds(operator);
        return owner.getDepartmentId() != null && departmentIds.contains(owner.getDepartmentId());
    }

    private String buildDepartmentPath(Long departmentId, Map<Long, Department> departmentMap) {
        if (departmentId == null) {
            return null;
        }
        Department department = departmentMap.get(departmentId);
        if (department == null) {
            return null;
        }
        List<String> names = new ArrayList<>();
        String treePath = department.getTreePath();
        if (treePath != null && !treePath.isBlank()) {
            for (String nodeIdText : treePath.split("/")) {
                if (nodeIdText == null || nodeIdText.isBlank()) {
                    continue;
                }
                try {
                    Long nodeId = Long.parseLong(nodeIdText);
                    Department node = departmentMap.get(nodeId);
                    if (node != null) {
                        names.add(node.getName());
                    }
                } catch (NumberFormatException ignored) {
                    // ignore invalid tree path node
                }
            }
        }
        names.add(department.getName());
        return String.join(" / ", names);
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

    private record OwnerAssignment(Long ownerUserId, Long ownerDeptId) {
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private Specification<Customer> buildCustomerSpec(LoginUser user, String keyword, String tab, String ownerScope) {
        Set<Long> departmentIds = null;
        if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
            departmentIds = dataScopeResolver.resolveDepartmentIds(user);
            if (departmentIds.isEmpty()) {
                return (root, query, cb) -> cb.disjunction();
            }
        }

        final Set<Long> scopedDepartmentIds = departmentIds;
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedTab = tab == null ? "" : tab.trim().toLowerCase(Locale.ROOT);
        String normalizedOwnerScope = ownerScope == null ? "" : ownerScope.trim().toLowerCase(Locale.ROOT);

        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (user.getDataScope() == DataScope.SELF) {
                predicates.add(cb.equal(root.get("ownerUserId"), user.getUserId()));
            } else if (user.getDataScope() == DataScope.DEPARTMENT) {
                predicates.add(cb.equal(root.get("ownerDeptId"), user.getDepartmentId()));
            } else if (user.getDataScope() == DataScope.DEPARTMENT_TREE) {
                predicates.add(root.get("ownerDeptId").in(scopedDepartmentIds));
            }

            if ("pool".equals(normalizedTab)) {
                predicates.add(root.get("status").in(List.of("INACTIVE", "BLACKLIST")));
            } else if ("customer".equals(normalizedTab)) {
                predicates.add(cb.not(root.get("status").in(List.of("INACTIVE", "BLACKLIST"))));
                if ("mine".equals(normalizedOwnerScope)) {
                    predicates.add(cb.equal(root.get("ownerUserId"), user.getUserId()));
                } else if ("cooperate".equals(normalizedOwnerScope)) {
                    predicates.add(cb.notEqual(root.get("ownerUserId"), user.getUserId()));
                }
            }

            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                List<jakarta.persistence.criteria.Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(cb.like(cb.lower(root.get("name")), likeValue));
                keywordPredicates.add(cb.like(cb.lower(root.get("phone")), likeValue));
                keywordPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("city"), "")), likeValue));
                keywordPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("tags"), "")), likeValue));
                predicates.add(cb.or(keywordPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

