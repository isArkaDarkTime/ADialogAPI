package me.arkadarktime.aDialogAPI.models;

import java.util.Map;

public record ParsedInputs(
        Map<String, String> defaults,
        Map<String, InputMeta> metas
) {
}

