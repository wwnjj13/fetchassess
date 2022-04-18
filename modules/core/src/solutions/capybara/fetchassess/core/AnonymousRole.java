package solutions.capybara.fetchassess.core;

import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
import com.haulmont.cuba.security.app.role.annotation.Role;
import com.haulmont.cuba.security.role.EntityAttributePermissionsContainer;
import com.haulmont.cuba.security.role.EntityPermissionsContainer;

@Role(name = AnonymousRole.NAME, isDefault = true, isSuper = true)
public class AnonymousRole extends AnnotatedRoleDefinition {
    public final static String NAME = "Anonymous";

    @Override
    public EntityPermissionsContainer entityPermissions() {
        return super.entityPermissions();
    }

    @Override
    public EntityAttributePermissionsContainer entityAttributePermissions() {
        return super.entityAttributePermissions();
    }

    /*@ScreenAccess(screenIds = {"application-demo", "demo_Customer.browse", "demo_Customer.edit"})
    @Override
    public ScreenPermissionsContainer screenPermissions() {
        return super.screenPermissions();
    }*/
}
