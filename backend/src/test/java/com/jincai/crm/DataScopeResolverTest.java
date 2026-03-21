package com.jincai.crm;

import com.jincai.crm.common.DataScope;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.org.entity.Department;
import com.jincai.crm.org.repository.DepartmentRepository;
import com.jincai.crm.security.LoginUser;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataScopeResolverTest {

    @Test
    void shouldResolveDepartmentTree() {
        DepartmentRepository repository = Mockito.mock(DepartmentRepository.class);
        Department d1 = new Department();
        d1.setId(1L);
        d1.setParentId(null);
        Department d2 = new Department();
        d2.setId(2L);
        d2.setParentId(1L);
        Department d3 = new Department();
        d3.setId(3L);
        d3.setParentId(2L);

        Mockito.when(repository.findByDeletedFalse()).thenReturn(List.of(d1, d2, d3));
        DataScopeResolver resolver = new DataScopeResolver(repository);

        LoginUser user = new LoginUser(10L, 2L, DataScope.DEPARTMENT_TREE, "u", "p", true, List.of("SALES"));
        Set<Long> ids = resolver.resolveDepartmentIds(user);

        Assertions.assertEquals(Set.of(2L, 3L), ids);
    }
}

