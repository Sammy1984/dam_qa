package ru.spice.at.api.authorization;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.api.dto.response.authorization.PermissionsResponse;

import java.util.List;

@Data
@Accessors(chain = true, fluent = true)
public class AuthorizationSettings {
    private PermissionsResponse administratorPermissions;
    private PermissionsResponse photoproductionPermissions;
    private PermissionsResponse contentProductionPermissions;
    private PermissionsResponse contentSupportPermissions;
    private PermissionsResponse photoproductionOutsourcePermissions;
    private PermissionsResponse contentProductionOutsourcePermissions;
    private String description;
    private List<String> keywords;
    private String received;
}
