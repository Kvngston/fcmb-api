package com.tk.fcmb;

import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Enums.RequestStatus;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Repositories.RoleRepository;
import com.tk.fcmb.Repositories.TransactionGraphDataRepository;
import com.tk.fcmb.utils.GraphDbPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class FcmbPortalApplication implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GraphDbPopulator dbPopulator;

    @Autowired
    private TransactionGraphDataRepository transactionGraphDataRepository;

    public static void main(String[] args) {
        SpringApplication.run(FcmbPortalApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {


        if (roleRepository.count() == 0) {
            Role superAdmin = new Role();
            superAdmin.setRoleName(RoleType.SUPER_ADMIN.name());
            superAdmin.setRoleType(RoleType.SUPER_ADMIN);
            superAdmin.setApproved(true);

            List<GrantedAuthority> superAdminAuthorities = RoleType.SUPER_ADMIN.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            superAdminAuthorities.add(new SimpleGrantedAuthority(superAdmin.getRoleName()));

            superAdmin.setAuthorities(superAdminAuthorities);
            superAdmin.setRequestStatus(RequestStatus.APPROVED);
            roleRepository.save(superAdmin);


            Role admin = new Role();
            admin.setRoleName(RoleType.ADMIN.name());
            admin.setRoleType(RoleType.ADMIN);
            admin.setApproved(true);
            admin.setRequestStatus(RequestStatus.APPROVED);

            List<GrantedAuthority> adminAuthorities = RoleType.ADMIN.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            adminAuthorities.add(new SimpleGrantedAuthority(admin.getRoleName()));

            admin.setAuthorities(adminAuthorities);
            roleRepository.save(admin);

            Role itControl = new Role();
            itControl.setRoleName(RoleType.IT_CONTROL.name());
            itControl.setRoleType(RoleType.IT_CONTROL);
            itControl.setApproved(true);
            itControl.setRequestStatus(RequestStatus.APPROVED);

            List<GrantedAuthority> itControlAuthorities = RoleType.IT_CONTROL.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            itControlAuthorities.add(new SimpleGrantedAuthority(itControl.getRoleName()));

            itControl.setAuthorities(itControlAuthorities);
            roleRepository.save(itControl);

            Role agent = new Role();
            agent.setRoleName(RoleType.AGENT.name());
            agent.setRoleType(RoleType.AGENT);
            agent.setApproved(true);
            agent.setRequestStatus(RequestStatus.APPROVED);

            List<GrantedAuthority> agentAuthorities = RoleType.AGENT.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            agentAuthorities.add(new SimpleGrantedAuthority(agent.getRoleName()));

            agent.setAuthorities(agentAuthorities);
            roleRepository.save(agent);
        }


        if (transactionGraphDataRepository.count() == 0){
            dbPopulator.dailyGraphPopulator();
            dbPopulator.monthlyGraphPopulator();
            dbPopulator.weeklyGraphPopulator();
            dbPopulator.yearlyGraphPopulator();
            dbPopulator.userDailyGraphPopulator();
            dbPopulator.usersMonthlyGraphPopulator();
            dbPopulator.usersYearlyGraphPopulator();
            dbPopulator.userWeeklyGraphPopulator();
            dbPopulator.usersDaysInWeeksGraphPopulator();
        }

    }

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
