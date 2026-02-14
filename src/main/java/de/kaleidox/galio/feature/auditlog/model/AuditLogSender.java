package de.kaleidox.galio.feature.auditlog.model;

import de.kaleidox.galio.feature.auditlog.AuditLogService;

import static de.kaleidox.galio.util.ApplicationContextProvider.*;

public interface AuditLogSender {
    default String getAuditSourceName() {
        return getClass().getSimpleName();
    }

    default AuditLogService audit() {
        return bean(AuditLogService.class);
    }
}
