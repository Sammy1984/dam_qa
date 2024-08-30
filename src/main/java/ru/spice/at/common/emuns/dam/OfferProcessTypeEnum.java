package ru.spice.at.common.emuns.dam;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OfferProcessTypeEnum {
    CANCEL_TASK,
    REMOVE_FROM_TASK,
    FINALYZE_TASK,
    CREATE_TASK,
    ADD_TO_TASK,
    CONTENT_AUTOGENERATION
}
