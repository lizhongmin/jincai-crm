package com.jincai.crm;

import com.jincai.crm.common.BusinessException;
import com.jincai.crm.common.DataScopeResolver;
import com.jincai.crm.common.I18nService;
import com.jincai.crm.customer.dto.TravelerDocumentRequest;
import com.jincai.crm.customer.dto.TravelerRequest;
import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.repository.CustomerRepository;
import com.jincai.crm.customer.repository.TravelerRepository;
import com.jincai.crm.customer.service.CustomerService;
import com.jincai.crm.system.repository.AppUserRepository;
import com.jincai.crm.system.repository.DepartmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CustomerServiceTest {

    @Test
    void shouldExtractBirthdayFromIdCardWhenSavingTraveler() {
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);

        Customer customer = new Customer();
        customer.setId(1L);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Mockito.when(travelerRepository.save(Mockito.any(Traveler.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerService customerService = new CustomerService(
            customerRepository,
            travelerRepository,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AppUserRepository.class),
            Mockito.mock(DepartmentRepository.class),
            Mockito.mock(I18nService.class)
        );

        Traveler traveler = customerService.addTraveler(1L, new TravelerRequest(
            "张三",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "13800000000",
            null,
            null,
            null,
            List.of(new TravelerDocumentRequest("ID_CARD", "370921199001012345"))
        ));

        Assertions.assertEquals(LocalDate.of(1990, 1, 1), traveler.getBirthday());
        Assertions.assertEquals("ID_CARD", traveler.getIdType());
        Assertions.assertEquals("370921199001012345", traveler.getIdNo());
    }

    @Test
    void shouldRejectTravelerWithoutIdCardDocument() {
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);

        Customer customer = new Customer();
        customer.setId(1L);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerService customerService = new CustomerService(
            customerRepository,
            travelerRepository,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AppUserRepository.class),
            Mockito.mock(DepartmentRepository.class),
            Mockito.mock(I18nService.class)
        );

        TravelerRequest request = new TravelerRequest(
            "李四",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "13800000001",
            null,
            null,
            null,
            List.of(new TravelerDocumentRequest("PASSPORT", "E12345678"))
        );

        Assertions.assertThrows(BusinessException.class, () -> customerService.addTraveler(1L, request));
    }

    @Test
    void shouldAcceptLegacyChineseIdCardType() {
        CustomerRepository customerRepository = Mockito.mock(CustomerRepository.class);
        TravelerRepository travelerRepository = Mockito.mock(TravelerRepository.class);

        Customer customer = new Customer();
        customer.setId(1L);
        Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Mockito.when(travelerRepository.save(Mockito.any(Traveler.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerService customerService = new CustomerService(
            customerRepository,
            travelerRepository,
            Mockito.mock(DataScopeResolver.class),
            Mockito.mock(AppUserRepository.class),
            Mockito.mock(DepartmentRepository.class),
            Mockito.mock(I18nService.class)
        );

        Traveler traveler = customerService.addTraveler(1L, new TravelerRequest(
            "王五",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "13800000002",
            null,
            null,
            null,
            List.of(new TravelerDocumentRequest("身份证", "370921199001012345"))
        ));

        Assertions.assertEquals("ID_CARD", traveler.getIdType());
        Assertions.assertEquals(LocalDate.of(1990, 1, 1), traveler.getBirthday());
    }
}
