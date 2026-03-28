package com.jincai.crm.bootstrap;

import com.jincai.crm.customer.entity.Customer;
import com.jincai.crm.customer.entity.Traveler;
import com.jincai.crm.customer.repository.CustomerRepository;
import com.jincai.crm.customer.repository.TravelerRepository;
import com.jincai.crm.system.entity.Department;
import com.jincai.crm.system.entity.OrgUser;
import com.jincai.crm.system.repository.DepartmentRepository;
import com.jincai.crm.system.repository.OrgUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Order(2) // Run after SeedDataRunner
public class TestDataGenerator implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final TravelerRepository travelerRepository;
    private final DepartmentRepository departmentRepository;
    private final OrgUserRepository userRepository;

    public TestDataGenerator(CustomerRepository customerRepository,
                           TravelerRepository travelerRepository,
                           DepartmentRepository departmentRepository,
                           OrgUserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.travelerRepository = travelerRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Check if we already have enough data
        long customerCount = customerRepository.count();
        long travelerCount = travelerRepository.count();

        // Only generate data if we have less than 50 customers (to avoid re-generating on restart)
        if (customerCount < 50) {
            System.out.println("Generating test data...");
            generateTestData();
            System.out.println("Test data generation completed.");
        } else {
            System.out.println("Test data already exists. Skipping generation.");
        }
    }

    private void generateTestData() {
        // Get existing departments and users
        List<Department> departments = departmentRepository.findByDeletedFalse();
        List<OrgUser> users = userRepository.findByDeletedFalse();

        if (departments.isEmpty() || users.isEmpty()) {
            System.out.println("No departments or users found. Skipping test data generation.");
            return;
        }

        // Generate 100 customers
        System.out.println("Generating 100 customers...");
        Random random = new Random();
        int generatedCount = 0;
        int attemptCount = 0;
        final int maxAttempts = 200; // Prevent infinite loop

        while (generatedCount < 100 && attemptCount < maxAttempts) {
            attemptCount++;
            Customer customer = new Customer();
            customer.setName(generateCustomerName(random));
            customer.setPhone(generatePhoneNumber(random, attemptCount));
            customer.setCustomerType(generateCustomerType(random));
            customer.setSource(generateSource(random));
            customer.setIntentionLevel(generateIntentionLevel(random));
            customer.setStatus(generateCustomerStatus(random));

            // Check if phone already exists
            if (customerRepository.existsByPhoneAndDeletedFalse(customer.getPhone())) {
                continue; // Skip this customer and try again
            }

            // Assign to random user and department
            OrgUser randomUser = users.get(random.nextInt(users.size()));
            customer.setOwnerUserId(randomUser.getId());
            customer.setOwnerDeptId(randomUser.getDepartmentId());

            customer.setLevel(generateCustomerLevel(random));
            customer.setWechat("wechat_" + attemptCount);
            customer.setEmail("customer" + attemptCount + "@example.com");
            customer.setCity(generateCity(random));
            customer.setTags(generateTags(random));
            customer.setRemark("Test customer #" + attemptCount);

            try {
                customerRepository.save(customer);
                generatedCount++;

                // Generate 1-3 travelers for each customer
                int travelerCount = random.nextInt(3) + 1;
                for (int j = 1; j <= travelerCount; j++) {
                    Traveler traveler = new Traveler();
                    traveler.setCustomerId(customer.getId());
                    traveler.setName(generateTravelerName(random));
                    traveler.setIdType(generateIdType(random));
                    traveler.setIdNo(generateIdNumber(random, attemptCount, j));
                    traveler.setBirthday(generateBirthday(random));
                    traveler.setGender(generateGender(random));
                    traveler.setEthnicity(generateEthnicity(random));
                    traveler.setNationality("中国");
                    traveler.setAddress("测试地址 " + attemptCount + "-" + j);
                    traveler.setPhone(generatePhoneNumber(random, attemptCount * 10 + j));
                    traveler.setEmergencyContact("紧急联系人 " + j);
                    traveler.setEmergencyPhone(generatePhoneNumber(random, attemptCount * 100 + j));
                    traveler.setPreferences(generatePreferences(random));

                    travelerRepository.save(traveler);
                }

                if (generatedCount % 20 == 0) {
                    System.out.println("Generated " + generatedCount + " customers...");
                }
            } catch (Exception e) {
                System.out.println("Failed to save customer: " + e.getMessage());
                // Continue with next attempt
            }
        }

        System.out.println("Generated " + generatedCount + " customers with associated travelers.");
    }

    private String generateCustomerName(Random random) {
        String[] surnames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] names = {"伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋",
                         "勇", "艳", "杰", "娟", "涛", "明", "超", "秀英", "霞", "平"};
        return surnames[random.nextInt(surnames.length)] + names[random.nextInt(names.length)];
    }

    private String generatePhoneNumber(Random random, int uniqueSeed) {
        return "138" + String.format("%08d", (uniqueSeed % 100000000));
    }

    private String generateCustomerType(Random random) {
        String[] types = {"PERSONAL", "ENTERPRISE"};
        return types[random.nextInt(types.length)];
    }

    private String generateSource(Random random) {
        String[] sources = {"MANUAL", "REFERRAL", "ONLINE", "PHONE", "WALK_IN"};
        return sources[random.nextInt(sources.length)];
    }

    private String generateIntentionLevel(Random random) {
        String[] levels = {"HIGH", "MEDIUM", "LOW"};
        return levels[random.nextInt(levels.length)];
    }

    private String generateCustomerStatus(Random random) {
        String[] statuses = {"ACTIVE", "INACTIVE", "BLACKLIST"};
        return statuses[random.nextInt(statuses.length)];
    }

    private String generateCustomerLevel(Random random) {
        String[] levels = {"A", "B", "C"};
        return levels[random.nextInt(levels.length)];
    }

    private String generateCity(Random random) {
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉", "西安", "长沙"};
        return cities[random.nextInt(cities.length)];
    }

    private String generateTags(Random random) {
        String[] tags = {"VIP客户", "潜在客户", "老客户", "企业客户", "个人客户", "高意向", "需跟进"};
        int count = random.nextInt(3) + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(tags[random.nextInt(tags.length)]);
        }
        return sb.toString();
    }

    private String generateTravelerName(Random random) {
        String[] surnames = {"张", "李", "王", "刘", "陈", "杨", "赵", "黄", "周", "吴"};
        String[] names = {"小明", "小红", "小华", "小丽", "小强", "小芳", "小军", "小敏", "小勇", "小霞",
                         "子涵", "浩然", "欣怡", "梓涵", "俊杰", "思雨", "宇轩", "佳怡", "浩宇", "梦琪"};
        return surnames[random.nextInt(surnames.length)] + names[random.nextInt(names.length)];
    }

    private String generateIdType(Random random) {
        String[] types = {"ID_CARD", "PASSPORT", "HK_MACAO_PASS", "TAIWAN_PASS", "MILITARY_ID", "OTHER"};
        return types[random.nextInt(types.length)];
    }

    private String generateIdNumber(Random random, int customerSeed, int travelerIndex) {
        return "ID" + String.format("%012d", (Math.abs((long)customerSeed * 10 + travelerIndex) % 1000000000000L));
    }

    private LocalDate generateBirthday(Random random) {
        int year = 1970 + random.nextInt(50);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28); // Simplified to avoid month boundary issues
        return LocalDate.of(year, month, day);
    }

    private String generateGender(Random random) {
        String[] genders = {"MALE", "FEMALE", "OTHER"};
        return genders[random.nextInt(genders.length)];
    }

    private String generateEthnicity(Random random) {
        String[] ethnicities = {"汉族", "满族", "回族", "藏族", "壮族", "苗族", "维吾尔族", "彝族", "布依族", "朝鲜族"};
        return ethnicities[random.nextInt(ethnicities.length)];
    }

    private String generatePreferences(Random random) {
        String[] preferences = {"无特殊要求", "需要无烟房", "需要婴儿床", "需要轮椅通道", "需要素食", "需要清真餐"};
        return preferences[random.nextInt(preferences.length)];
    }
}