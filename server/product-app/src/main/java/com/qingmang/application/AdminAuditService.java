package com.qingmang.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditService {
    private final JdbcTemplate jdbc;

    public AdminAuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void success(Long actor, String module, String action, Long target, String detail, String ip) {
        insert(actor, module, action, target, detail, ip, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failure(Long actor, String module, String action, Long target, String detail, String ip) {
        insert(actor, module, action, target, detail, ip, false);
    }

    private void insert(Long actor, String module, String action, Long target, String detail, String ip,
                        boolean success) {
        jdbc.update("INSERT INTO operation_log(operator_id,operator_name,module,action,target_type,target_id,"
                        + "detail,success,ip) VALUES(?,(SELECT nickname FROM `user` WHERE id=?),?,?,?,?,?,?,?)",
                actor, actor, module, action, module, target, limit(detail, 1000), success, limit(ip, 45));
    }

    private String limit(String value, int max) {
        return value == null ? null : value.substring(0, Math.min(value.length(), max));
    }
}
