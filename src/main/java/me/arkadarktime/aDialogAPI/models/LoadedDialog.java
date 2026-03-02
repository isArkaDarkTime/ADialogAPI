package me.arkadarktime.aDialogAPI.models;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LoadedDialog {

    private final Dialog dialog;
    private final DialogMeta meta;
    private final File file;
    private final Map<String, List<ButtonAction>> buttonActions;
    private final Map<String, String> inputs;
    private final Map<String, InputMeta> inputMetas;

    public LoadedDialog(Dialog dialog, DialogMeta meta, File file,
                        Map<String, List<ButtonAction>> buttonActions,
                        Map<String, String> inputs,
                        Map<String, InputMeta> inputMetas) {
        this.dialog = dialog;
        this.meta = meta;
        this.file = file;
        this.buttonActions = Collections.unmodifiableMap(buttonActions);
        this.inputs = Collections.unmodifiableMap(inputs);
        this.inputMetas = Collections.unmodifiableMap(inputMetas);
    }

    // Getters

    public Dialog getDialog() {
        return dialog;
    }

    public DialogMeta getMeta() {
        return meta;
    }

    public File getFile() {
        return file;
    }

    public List<ButtonAction> getActions(String buttonId) {
        return buttonActions.getOrDefault(buttonId.toLowerCase(), List.of());
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public Map<String, InputMeta> getInputMetas() {
        return inputMetas;
    }

    public String getName() {
        return meta.name();
    }

    public String getOpenCommand() {
        return meta.openCommand();
    }

    public String getOpenPermission() {
        return meta.openPermission();
    }

    public boolean hasOpenCommand() {
        return !meta.openCommand().isEmpty();
    }

    @SuppressWarnings("UnstableApiUsage")
    public Map<String, String> resolveInputsFromEvent(PlayerCustomClickEvent event) {
        var view = event.getDialogResponseView();
        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, InputMeta> entry : inputMetas.entrySet()) {
            String key = entry.getKey();
            InputMeta meta = entry.getValue();

            String resolved;
            if (view == null) {
                resolved = meta.defaultValue();
            } else {
                resolved = switch (meta.type()) {
                    case "bool" -> {
                        Boolean b = view.getBoolean(key);
                        yield b != null ? (b ? meta.onTrue() : meta.onFalse()) : meta.defaultValue();
                    }
                    case "number_range" -> {
                        Float f = view.getFloat(key);
                        if (f == null) yield meta.defaultValue();
                        yield (f == f.intValue()) ? String.valueOf(f.intValue()) : String.valueOf(f);
                    }
                    default -> {
                        String s = view.getText(key);
                        yield s != null ? s : meta.defaultValue();
                    }
                };
            }

            result.put(key, resolved);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public String toString() {
        return "LoadedDialog{name='" + meta.name() + "', type='" + meta.type() + "', inputs=" + inputs.keySet() + "}";
    }
}