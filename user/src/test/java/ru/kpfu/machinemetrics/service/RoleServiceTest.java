package ru.kpfu.machinemetrics.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.kpfu.machinemetrics.config.MessageSourceConfig;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest({"keycloak.realm=realm", "keycloak.auth-server-url=http://localhost:8080"})
@ImportAutoConfiguration(MessageSourceConfig.class)
public class RoleServiceTest {

    @MockBean
    private Keycloak keycloakMock;

    @Autowired
    private RoleService roleService;

    @Test
    public void findAll() {
        // given
        RoleRepresentation role1 = new RoleRepresentation();
        role1.setName("ADMIN");

        RoleRepresentation role2 = new RoleRepresentation();
        role2.setName("USER");

        RoleRepresentation role3 = new RoleRepresentation();
        role3.setName("Name 2");

        List<RoleRepresentation> roleList = List.of(role1, role2, role3);

        RolesResource rolesResourceMock = mock(RolesResource.class);
        when(rolesResourceMock.list()).thenReturn(roleList);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.roles()).thenReturn(rolesResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        List<RoleRepresentation> result = roleService.findAll();

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.size()).isEqualTo(2);
        softly.assertThat(result.get(0).getName()).isEqualTo(role1.getName());
        softly.assertThat(result.get(1).getName()).isEqualTo(role2.getName());
        softly.assertAll();
    }

    @Test
    public void findByName() {
        // given
        String roleName = "admin";

        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);

        RoleResource roleResourceMock = mock(RoleResource.class);
        when(roleResourceMock.toRepresentation()).thenReturn(role);

        RolesResource rolesResourceMock = mock(RolesResource.class);
        when(rolesResourceMock.get(roleName)).thenReturn(roleResourceMock);

        RealmResource realmResourceMock = mock(RealmResource.class);
        when(realmResourceMock.roles()).thenReturn(rolesResourceMock);

        when(keycloakMock.realm("realm")).thenReturn(realmResourceMock);

        // when
        RoleRepresentation result = roleService.findByName(roleName);

        // then
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(result.getName()).isEqualTo(role.getName());
        softly.assertAll();
    }
}
