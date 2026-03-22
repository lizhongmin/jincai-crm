package com.jincai.crm;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.I18nService;
import com.jincai.crm.notification.repository.NotificationRepository;
import com.jincai.crm.order.entity.TravelOrder;
import com.jincai.crm.product.repository.DepartureRepository;
import com.jincai.crm.product.repository.RouteProductRepository;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.repository.RoleRepository;
import com.jincai.crm.system.repository.UserRoleRepository;
import com.jincai.crm.workflow.repository.WorkflowInstanceNodeRepository;
import com.jincai.crm.workflow.repository.WorkflowInstanceRepository;
import com.jincai.crm.workflow.service.WorkflowService;
import com.jincai.crm.workflow.entity.WorkflowTemplate;
import com.jincai.crm.workflow.repository.WorkflowTemplateNodeRepository;
import com.jincai.crm.workflow.repository.WorkflowTemplateRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkflowServiceTest {

    @Test
    void shouldMatchTemplateByTypeCategoryAndAmount() {
        WorkflowTemplateRepository templateRepository = Mockito.mock(WorkflowTemplateRepository.class);

        WorkflowTemplate template = new WorkflowTemplate();
        template.setOrderType("GROUP");
        template.setProductCategory("DOMESTIC");
        template.setMinAmount(new BigDecimal("1000"));
        template.setMaxAmount(new BigDecimal("5000"));
        template.setActive(true);

        Mockito.when(templateRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of(template));

        WorkflowService service = new WorkflowService(
            templateRepository,
            Mockito.mock(WorkflowTemplateNodeRepository.class),
            Mockito.mock(WorkflowInstanceRepository.class),
            Mockito.mock(WorkflowInstanceNodeRepository.class),
            Mockito.mock(AppUserRepository.class),
            Mockito.mock(UserRoleRepository.class),
            Mockito.mock(RoleRepository.class),
            Mockito.mock(RouteProductRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(NotificationRepository.class),
            Mockito.mock(I18nService.class)
        );

        TravelOrder order = new TravelOrder();
        order.setOrderType("GROUP");
        order.setProductCategory("DOMESTIC");
        order.setTotalAmount(new BigDecimal("3000"));

        WorkflowTemplate matched = service.matchTemplate(order);
        Assertions.assertNotNull(matched);
    }

    @Test
    void shouldThrowWhenNoTemplateMatched() {
        WorkflowTemplateRepository templateRepository = Mockito.mock(WorkflowTemplateRepository.class);
        Mockito.when(templateRepository.findByActiveTrueAndDeletedFalse()).thenReturn(List.of());

        WorkflowService service = new WorkflowService(
            templateRepository,
            Mockito.mock(WorkflowTemplateNodeRepository.class),
            Mockito.mock(WorkflowInstanceRepository.class),
            Mockito.mock(WorkflowInstanceNodeRepository.class),
            Mockito.mock(AppUserRepository.class),
            Mockito.mock(UserRoleRepository.class),
            Mockito.mock(RoleRepository.class),
            Mockito.mock(RouteProductRepository.class),
            Mockito.mock(DepartureRepository.class),
            Mockito.mock(NotificationRepository.class),
            Mockito.mock(I18nService.class)
        );

        TravelOrder order = new TravelOrder();
        order.setOrderType("GROUP");
        order.setProductCategory("DOMESTIC");
        order.setTotalAmount(new BigDecimal("3000"));

        Assertions.assertThrows(BusinessException.class, () -> service.matchTemplate(order));
    }
}
