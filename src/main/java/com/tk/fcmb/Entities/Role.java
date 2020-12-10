package com.tk.fcmb.Entities;

import com.tk.fcmb.Enums.RequestStatus;
import com.tk.fcmb.Enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String roleName;

    private RoleType roleType;

    @ElementCollection(targetClass = GrantedAuthority.class, fetch = FetchType.EAGER)
    private List<GrantedAuthority> authorities;

    private RequestStatus requestStatus = RequestStatus.PENDING;

    private boolean approved = false;

    private String ticketNumber;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return roleType == role.roleType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleType);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleName='" + roleName + '\'' +
                ", roleType=" + roleType +
                ", authorities=" + authorities +
                '}';
    }
}
