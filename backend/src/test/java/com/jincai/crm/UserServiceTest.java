package com.jincai.crm;

import com.jincai.crm.common.DataScope;
import com.jincai.crm.system.dto.UserUpsertRequest;
import com.jincai.crm.system.entity.AppUser;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.repository.RoleRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import com.jincai.crm.system.service.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    @Test
    void shouldNormalizeBlankEmployeeNoToNullOnUpdate() {
        AppUserRepository userRepository = Mockito.mock(AppUserRepository.class);
        DepartmentRepository departmentRepository = Mockito.mock(DepartmentRepository.class);
        UserRoleRepository userRoleRepository = Mockito.mock(UserRoleRepository.class);
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        AppUser existing = new AppUser();
        existing.setId(100L);
        existing.setUsername("sales01");
        existing.setPassword("encoded-old");
        existing.setFullName("Sales 01");
        existing.setPhone("13800138000");
        existing.setDepartmentId(1L);
        existing.setDataScope(DataScope.SELF);
        existing.setEnabled(true);

        Mockito.when(userRepository.findById(100L)).thenReturn(Optional.of(existing));
        Mockito.when(userRepository.existsByPhoneAndDeletedFalseAndIdNot("13800138000", 100L)).thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(departmentRepository.findByDeletedFalse()).thenReturn(List.of());
        Mockito.when(roleRepository.findByDeletedFalse()).thenReturn(List.of());
        Mockito.when(userRoleRepository.findByUserIdAndDeletedFalse(100L)).thenReturn(List.of());

        UserService service = new UserService(
            userRepository,
            departmentRepository,
            userRoleRepository,
            roleRepository,
            passwordEncoder
        );

        UserUpsertRequest request = new UserUpsertRequest(
            100L,
            "sales01",
            null,
            "Sales 01",
            "13800138000",
            "",
            "",
            "MALE",
            "Consultant",
            "2026-01-01",
            "",
            1L,
            DataScope.SELF,
            true,
            List.of()
        );

        service.update(100L, request);

        ArgumentCaptor<AppUser> savedCaptor = ArgumentCaptor.forClass(AppUser.class);
        Mockito.verify(userRepository).save(savedCaptor.capture());
        Assertions.assertNull(savedCaptor.getValue().getEmployeeNo());
    }
}
