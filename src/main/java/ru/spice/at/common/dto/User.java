package ru.spice.at.common.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.spice.at.common.emuns.Role;

/**
 * ДТО для подгрузки данных пользователей
 * @author Aleksandr Osokin
 */
@Data
@Accessors(chain = true, fluent = true)
public class User {
    private String username;
    private String password;
    private String name;
    private Role role;
}
