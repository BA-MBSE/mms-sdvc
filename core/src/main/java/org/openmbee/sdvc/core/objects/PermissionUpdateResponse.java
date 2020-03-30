package org.openmbee.sdvc.core.objects;

import java.util.List;

public class PermissionUpdateResponse {

    public enum Action {ADD, REMOVE}

    private List<PermissionUpdate> permissionUpdates;

    public static class PermissionUpdate {
        private Action action;

        private String name;

        private String role;

        private String orgId;

        private String orgName;

        private String projectId;

        private String projectName;

        private String branchId;

        private String branchName;

        private boolean inherited;

        public PermissionUpdate() {}

        public PermissionUpdate(Action action, String name, String role, String orgId, String orgName,
                                String projectId, String projectName, String branchId, String branchName, boolean inherited) {
            this.action = action;
            this.name = name;
            this.role = role;
            this.orgId = orgId;
            this.orgName = orgName;
            this.projectId = projectId;
            this.projectName = projectName;
            this.branchId = branchId;
            this.branchName = branchName;
            this.inherited = inherited;
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getOrgId() {
            return orgId;
        }

        public void setOrgId(String orgId) {
            this.orgId = orgId;
        }

        public String getOrgName() {
            return orgName;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getBranchId() {
            return branchId;
        }

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public boolean isInherited() {
            return inherited;
        }

        public void setInherited(boolean inherited) {
            this.inherited = inherited;
        }
    }

    public List<PermissionUpdate> getPermissionUpdates() {
        return permissionUpdates;
    }

    public void setPermissionUpdates(List<PermissionUpdate> permissionUpdates) {
        this.permissionUpdates = permissionUpdates;
    }

}
