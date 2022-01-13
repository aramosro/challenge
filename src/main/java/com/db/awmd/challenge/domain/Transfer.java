package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * THE TRANSFER REQUEST BODY OBJECT
 */
@Data
public class Transfer {

    @NotNull
    @NotEmpty
    private final String accountFromId;

    @NotNull
    @NotEmpty
    private final String accountToId;

    @NotNull
    @Min(value = 1, message = "Transfer must be greater than 0.")
    private BigDecimal amount;

    public Transfer(String accountFromId, String  accountToId){
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = BigDecimal.ZERO;
    }

    @JsonCreator
    public Transfer(@JsonProperty("accountFromId") String accountFromId,
                    @JsonProperty("accountToId") String accountToId,
                    @JsonProperty("amount") BigDecimal amount) {
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }
}
