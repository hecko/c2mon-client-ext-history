package cern.c2mon.client.ext.history.supervision;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Manuel Bouzas Reguera
 */
@Entity
@Table(name = "SUPERVISION_LOG",indexes = {@Index(name="SUL_ID_IDX",columnList = "SUL_ID"), @Index(name = "SUL_DATE_IDX", columnList="SUL_DATE")})
@Data
public class ServerSupervisionEvent {

    @Id
    @Column(name = "SUL_ID")
    private Long id;


    @Column(name = "SUL_ENTITY")
    private String sul_entity;


    @Column(name = "SUL_DATE")
    private Date eventTime;


    @Column(name = "SUL_STATUS")
    private String status;


    @Column(name = "SUL_MESSAGE")
    private String sul_message;


}
