package com.jincai.crm.system.service;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.PageResult;
import com.jincai.crm.system.dto.AppUserView;
import com.jincai.crm.system.dto.ResetPasswordRequest;
import com.jincai.crm.system.dto.UserStatusRequest;
import com.jincai.crm.system.dto.UserUpsertRequest;
import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.entity.Role;
import com.jincai.crm.system.entity.UserRole;
import com.jincai.crm.system.repository.RoleRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String ADMIN_USERNAME = "admin";

    private final AppUserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, DepartmentRepository departmentRepository,
                       UserRoleRepository userRoleRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUserView> list() {
        List<Department> departments = departmentRepository.findByDeletedFalse();
        Map<Long, Department> departmentMap = departments.stream()
            .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a, LinkedHashMap::new));
        Map<Long, String> roleNamesById = roleRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Role::getId, Role::getName, (a, b) -> a, LinkedHashMap::new));
        return toViews(userRepository.findByDeletedFalse(), departmentMap, roleNamesById);
    }

    public PageResult<AppUserView> page(int page, int size, String keyword, Long departmentId, Long roleId) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

        Set<Long> userIdsByRole = Set.of();
        if (roleId != null) {
            userIdsByRole = userRoleRepository.findByRoleIdAndDeletedFalse(roleId).stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toSet());
            if (userIdsByRole.isEmpty()) {
                return new PageResult<>(List.of(), 0, normalizedPage, normalizedSize);
            }
        }

        final Set<Long> scopedUserIds = userIdsByRole;
        Specification<AppUser> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("departmentId"), departmentId));
            }
            if (roleId != null) {
                predicates.add(root.get("id").in(scopedUserIds));
            }
            if (!normalizedKeyword.isBlank()) {
                String likeValue = "%" + normalizedKeyword + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("fullName"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("username"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("email"), "")), likeValue),
                    cb.like(cb.lower(cb.coalesce(root.get("employeeNo"), "")), likeValue)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<AppUser> result = userRepository.findAll(
            spec,
            PageRequest.of(
                normalizedPage - 1,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
            )
        );
        Map<Long, Department> departmentMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a, LinkedHashMap::new));
        Map<Long, String> roleNamesById = roleRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Role::getId, Role::getName, (a, b) -> a, LinkedHashMap::new));
        List<AppUserView> items = toViews(result.getContent(), departmentMap, roleNamesById);
        return new PageResult<>(items, result.getTotalElements(), normalizedPage, normalizedSize);
    }

    @Transactional
    public AppUserView create(UserUpsertRequest request) {
        String normalizedPhone = normalizeRequired(request.phone(), "error.user.phone.required");
        String normalizedEmployeeNo = normalizeNullable(request.employeeNo());
        validatePhone(normalizedPhone, null);
        validateEmployeeNo(normalizedEmployeeNo, null);
        AppUser user = new AppUser();
        user.setUsername(normalizeRequired(request.username(), "error.user.username.required"));
        user.setPassword(passwordEncoder.encode(request.password() == null ? "123456" : request.password()));
        applyUserFields(user, request, normalizedPhone, normalizedEmployeeNo);
        AppUser saved = userRepository.save(user);
        saveRoles(saved.getId(), request.roleIds());
        return toView(saved);
    }

    @Transactional
    public AppUserView update(Long id, UserUpsertRequest request) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("error.user.notFound"));
        ensureNotAdminAccount(user);
        String normalizedPhone = normalizeRequired(request.phone(), "error.user.phone.required");
        String normalizedEmployeeNo = normalizeNullable(request.employeeNo());
        validatePhone(normalizedPhone, id);
        validateEmployeeNo(normalizedEmployeeNo, id);

        applyUserFields(user, request, normalizedPhone, normalizedEmployeeNo);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        AppUser saved = userRepository.save(user);
        saveRoles(saved.getId(), request.roleIds());
        return toView(saved);
    }

    @Transactional
    public AppUserView changeStatus(Long id, UserStatusRequest request) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("error.user.notFound"));
        ensureNotAdminAccount(user);
        user.setEnabled(request.enabled());
        AppUser saved = userRepository.save(user);
        return toView(saved);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("error.user.notFound"));
        ensureNotAdminAccount(user);
        String password = request == null || request.password() == null || request.password().isBlank()
            ? "123456"
            : request.password();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    public void delete(Long id) {
        AppUser user = userRepository.findById(id).orElseThrow(() -> new BusinessException("error.user.notFound"));
        ensureNotAdminAccount(user);
        user.setDeleted(true);
        userRepository.save(user);
    }

    private void applyUserFields(AppUser user, UserUpsertRequest request, String normalizedPhone, String normalizedEmployeeNo) {
        user.setFullName(normalizeRequired(request.fullName(), "error.user.fullName.required"));
        user.setPhone(normalizedPhone);
        user.setEmployeeNo(normalizedEmployeeNo);
        user.setEmail(normalizeNullable(request.email()));
        user.setGender(normalizeNullable(request.gender()));
        user.setTitle(normalizeNullable(request.title()));
        user.setEmergencyPhone(normalizeNullable(request.emergencyPhone()));
        user.setDepartmentId(request.departmentId());
        user.setDataScope(request.dataScope());
        user.setEnabled(request.enabled());
        user.setHireDate(parseDate(request.hireDate()));
    }

    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateText, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException ex) {
            throw new BusinessException("error.user.hireDate.invalid");
        }
    }

    private void saveRoles(Long userId, List<Long> roleIds) {
        userRoleRepository.deleteByUserId(userId);
        if (roleIds == null) {
            return;
        }
        roleIds.stream().distinct().forEach(roleId -> {
            UserRole mapping = new UserRole();
            mapping.setUserId(userId);
            mapping.setRoleId(roleId);
            userRoleRepository.save(mapping);
        });
    }

    private AppUserView toView(AppUser user) {
        Map<Long, Department> departmentMap = departmentRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Department::getId, d -> d, (a, b) -> a, LinkedHashMap::new));
        Map<Long, String> roleNamesById = roleRepository.findByDeletedFalse().stream()
            .collect(Collectors.toMap(Role::getId, Role::getName, (a, b) -> a, LinkedHashMap::new));
        return toView(user, departmentMap, roleNamesById);
    }

    private AppUserView toView(AppUser user, Map<Long, Department> departmentMap, Map<Long, String> roleNamesById) {
        List<Long> roleIds = userRoleRepository.findByUserIdAndDeletedFalse(user.getId()).stream()
            .map(UserRole::getRoleId)
            .toList();
        return toView(user, departmentMap, roleNamesById, roleIds);
    }

    private AppUserView toView(AppUser user, Map<Long, Department> departmentMap, Map<Long, String> roleNamesById,
                               List<Long> roleIds) {
        List<String> roleNames = roleIds.stream()
            .map(roleNamesById::get)
            .filter(Objects::nonNull)
            .toList();
        Department department = departmentMap.get(user.getDepartmentId());
        return new AppUserView(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getPhone(),
            user.getEmployeeNo(),
            user.getEmail(),
            user.getGender(),
            user.getTitle(),
            user.getHireDate() == null ? null : user.getHireDate().toString(),
            user.getEmergencyPhone(),
            user.getDepartmentId(),
            department == null ? null : department.getName(),
            buildDepartmentPath(user.getDepartmentId(), departmentMap),
            user.getDataScope(),
            user.getEnabled(),
            roleIds,
            roleNames,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    private List<AppUserView> toViews(List<AppUser> users, Map<Long, Department> departmentMap, Map<Long, String> roleNamesById) {
        if (users == null || users.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = users.stream().map(AppUser::getId).collect(Collectors.toSet());
        Map<Long, List<Long>> roleIdsByUserId = new LinkedHashMap<>();
        for (UserRole mapping : userRoleRepository.findByUserIdInAndDeletedFalse(userIds)) {
            roleIdsByUserId.computeIfAbsent(mapping.getUserId(), key -> new ArrayList<>()).add(mapping.getRoleId());
        }
        return users.stream()
            .map(user -> toView(user, departmentMap, roleNamesById, roleIdsByUserId.getOrDefault(user.getId(), List.of())))
            .toList();
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
        String path = department.getTreePath();
        if (path != null && !path.isBlank()) {
            for (String nodeIdText : path.split("/")) {
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

    private void validatePhone(String phone, Long id) {
        boolean exists = id == null
            ? userRepository.existsByPhoneAndDeletedFalse(phone)
            : userRepository.existsByPhoneAndDeletedFalseAndIdNot(phone, id);
        if (exists) {
            throw new BusinessException("error.user.phone.exists");
        }
    }

    private void validateEmployeeNo(String employeeNo, Long id) {
        if (employeeNo == null || employeeNo.isBlank()) {
            return;
        }
        boolean exists = id == null
            ? userRepository.existsByEmployeeNoAndDeletedFalse(employeeNo)
            : userRepository.existsByEmployeeNoAndDeletedFalseAndIdNot(employeeNo, id);
        if (exists) {
            throw new BusinessException("error.user.employeeNo.exists");
        }
    }

    private void ensureNotAdminAccount(AppUser user) {
        if (ADMIN_USERNAME.equalsIgnoreCase(user.getUsername())) {
            throw new BusinessException("error.user.admin.modifyDenied");
        }
    }

    private String normalizeRequired(String value, String messageKey) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new BusinessException(messageKey);
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
}

