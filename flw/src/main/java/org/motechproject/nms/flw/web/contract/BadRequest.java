package org.motechproject.nms.flw.web.contract;

public class BadRequest {
    private String failureReason;

    public BadRequest(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}


