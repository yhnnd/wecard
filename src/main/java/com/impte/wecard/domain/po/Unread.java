package com.impte.wecard.domain.po;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unread implements Serializable {

    private static final long serialVersionUID = 3984261249413573375L;

    private String id;
    private String messageId;
    private String chatItemId;

}
