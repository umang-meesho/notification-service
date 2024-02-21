package com.meesho.NotificationService.models;


import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document(collection = "blacklist")
public class BlacklistEntity implements Serializable {

    @Id
    String phoneNumber;

    @Override
    public String toString() {
        return "BlacklistEntity{" +
                "phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
