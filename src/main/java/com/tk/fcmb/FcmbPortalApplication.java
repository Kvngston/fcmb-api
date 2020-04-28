package com.tk.fcmb;

import com.tk.fcmb.Entities.Role;
import com.tk.fcmb.Enums.RoleType;
import com.tk.fcmb.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class FcmbPortalApplication implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    public static void main(String[] args) {
        SpringApplication.run(FcmbPortalApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {


        if (roleRepository.count() == 0) {
            Role superAdmin = new Role();
            superAdmin.setRoleName(RoleType.SUPER_ADMIN.name());
            superAdmin.setRoleType(RoleType.SUPER_ADMIN);

            List<GrantedAuthority> superAdminAuthorities = RoleType.SUPER_ADMIN.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            superAdminAuthorities.add(new SimpleGrantedAuthority(superAdmin.getRoleName()));

            superAdmin.setAuthorities(superAdminAuthorities);
            roleRepository.save(superAdmin);

            System.out.println(superAdmin);

            Role admin = new Role();
            admin.setRoleName(RoleType.ADMIN.name());
            admin.setRoleType(RoleType.ADMIN);

            List<GrantedAuthority> adminAuthorities = RoleType.ADMIN.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            adminAuthorities.add(new SimpleGrantedAuthority(admin.getRoleName()));

            admin.setAuthorities(adminAuthorities);
            roleRepository.save(admin);

            Role itControl = new Role();
            itControl.setRoleName(RoleType.IT_CONTROL.name());
            itControl.setRoleType(RoleType.IT_CONTROL);

            List<GrantedAuthority> itControlAuthorities = RoleType.IT_CONTROL.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            itControlAuthorities.add(new SimpleGrantedAuthority(itControl.getRoleName()));

            itControl.setAuthorities(itControlAuthorities);
            roleRepository.save(itControl);

            Role agent = new Role();
            agent.setRoleName(RoleType.AGENT.name());
            agent.setRoleType(RoleType.AGENT);

            List<GrantedAuthority> agentAuthorities = RoleType.AGENT.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toList());
            agentAuthorities.add(new SimpleGrantedAuthority(agent.getRoleName()));

            agent.setAuthorities(agentAuthorities);
            roleRepository.save(agent);
        }
    }
}
