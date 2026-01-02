package om.tanish.saas.tenant;

public class UpdateTenantStatusRequest {

    TenantStatus tenantStatus;

    public TenantStatus getTenantStatus() {
        return tenantStatus;
    }

    public void setTenantStatus(TenantStatus tenantStatus) {
        this.tenantStatus = tenantStatus;
    }
}
