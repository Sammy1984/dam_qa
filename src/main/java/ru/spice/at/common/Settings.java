package ru.spice.at.common;

import org.aeonbits.owner.Config;

/**
 * Сеттинги передаваемые при запуске
 * @author Aleksandr Osokin
 */
public interface Settings extends Config {

    @Key("standProperties")
    @DefaultValue("spiceDamStand")
    String standProperties();

    @Key("standName")
    @DefaultValue("stand-stable")
    String standName();

    @Key("remote")
    @DefaultValue("true")
    boolean remote();

    @Key("version")
    @DefaultValue("125.0")
    String version();

    @Key("seleniumTimeout")
    @DefaultValue("20")
    Integer seleniumTimeout();

    @Key("restTimeout")
    @DefaultValue("10")
    Integer restTimeout();

    @Key("remoteConnectionTimeout")
    @DefaultValue("300")
    Integer remoteConnectionTimeout();

    @Key("driver")
    String driver();

    @Key("checkJsonScheme")
    @DefaultValue("true")
    boolean checkJsonScheme();

    @Key("encodedKey")
    String encodedKey();
}