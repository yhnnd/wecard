package com.impte.wecard.domain.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Group implements Serializable {

    private static final long serialVersionUID = 8083050430790988798L;

    private String id;
    private Timestamp groupCreateTime;
    private String groupName;
    private Integer friendNum;
    private User user;
    private Set<Friend> friends;

}
