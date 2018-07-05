package org.jumpmind.pos.trans.model;

import org.jumpmind.pos.persist.Column;
import org.jumpmind.pos.persist.Entity;

abstract public class TransactionEntity extends Entity {

    @Column(primaryKey = true)
    private Long sequenceNumber;
    
    @Column(primaryKey = true, size="8")
    private String businessDate;
    
    @Column(primaryKey = true)
    private String deviceId;
    
}
