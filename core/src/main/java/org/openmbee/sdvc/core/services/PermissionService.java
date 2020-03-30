package org.openmbee.sdvc.core.services;

import java.util.Set;
import org.openmbee.sdvc.core.objects.PermissionResponse;
import org.openmbee.sdvc.core.objects.PermissionUpdateRequest;
import org.openmbee.sdvc.core.objects.PermissionUpdateResponse;
import org.openmbee.sdvc.core.objects.PermissionUpdatesResponse;

public interface PermissionService {

    void initOrgPerms(String orgId, String creator);

    void initProjectPerms(String projectId, boolean inherit, String creator);

    void initBranchPerms(String projectId, String branchId, boolean inherit, String creator);

    PermissionUpdateResponse updateOrgUserPerms(PermissionUpdateRequest req, String orgId);

    PermissionUpdateResponse updateOrgGroupPerms(PermissionUpdateRequest req, String orgId);

    PermissionUpdateResponse updateProjectUserPerms(PermissionUpdateRequest req, String projectId);

    PermissionUpdateResponse updateProjectGroupPerms(PermissionUpdateRequest req, String projectId);

    PermissionUpdateResponse updateBranchUserPerms(PermissionUpdateRequest req, String projectId, String branchId);

    PermissionUpdateResponse updateBranchGroupPerms(PermissionUpdateRequest req, String projectId, String branchId);

    PermissionUpdatesResponse setProjectInherit(boolean isInherit, String projectId);

    PermissionUpdatesResponse setBranchInherit(boolean isInherit, String projectId, String branchId);

    boolean setOrgPublic(boolean isPublic, String orgId);

    boolean setProjectPublic(boolean isPublic, String projectId);

    boolean hasOrgPrivilege(String privilege, String user, Set<String> groups, String orgId);

    boolean hasProjectPrivilege(String privilege, String user, Set<String> groups, String projectId);

    boolean hasBranchPrivilege(String privilege, String user, Set<String> groups, String projectId, String branchId);

    boolean isProjectInherit(String projectId);

    boolean isBranchInherit(String projectId, String branchId);

    boolean isOrgPublic(String orgId);

    boolean isProjectPublic(String projectId);

    PermissionResponse getOrgGroupRoles(String orgId);

    PermissionResponse getOrgUserRoles(String orgId);

    PermissionResponse getProjectGroupRoles(String projectId);

    PermissionResponse getProjectUserRoles(String projectId);

    PermissionResponse getBranchGroupRoles(String projectId, String branchId);

    PermissionResponse getBranchUserRoles(String projectId, String branchId);
}
