package org.motechproject.nms.api.web.contract;

public class LanguageRequest {
    private Long callingNumber;
    private Long callId;
    private Integer languageLocationCode;

    // Necessary for Jackson
    public LanguageRequest() { }

    // Used in ITs only
    public LanguageRequest(Long callingNumber, Long callId, Integer languageLocationCode) {
        this.callingNumber = callingNumber;
        this.callId = callId;
        this.languageLocationCode = languageLocationCode;
    }

    public Long getCallingNumber() {
        return callingNumber;
    }

    public void setCallingNumber(Long callingNumber) {
        this.callingNumber = callingNumber;
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public Integer getLanguageLocationCode() {
        return languageLocationCode;
    }

    public void setLanguageLocationCode(Integer languageLocationCode) {
        this.languageLocationCode = languageLocationCode;
    }
}
