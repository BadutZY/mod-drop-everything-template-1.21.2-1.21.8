package com.example.evokerdrop.config;

import com.example.evokerdrop.util.MinecraftRegistryHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectionScreen extends Screen {
    private final Screen parent;
    private final String title;
    private final List<String> allOptions;
    private List<String> filteredOptions;
    private final Consumer<String> onSelect;

    private TextFieldWidget searchField;
    private int scrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 15;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 2;

    public SelectionScreen(Screen parent, String title, List<String> options, Consumer<String> onSelect) {
        super(Text.literal(title));
        this.parent = parent;
        this.title = title;
        this.allOptions = options;
        this.filteredOptions = new ArrayList<>(options);
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        // Search field
        searchField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 150,
                20,
                300,
                20,
                Text.literal("Search")
        );
        searchField.setPlaceholder(Text.literal("Type to search..."));
        searchField.setChangedListener(this::onSearchChanged);
        this.addSelectableChild(searchField);

        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Cancel"),
                        button -> this.close()
                )
                .dimensions(this.width / 2 - 155, this.height - 30, 150, 20)
                .build());

        // Clear search button
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Clear"),
                        button -> {
                            searchField.setText("");
                            onSearchChanged("");
                        }
                )
                .dimensions(this.width / 2 + 5, this.height - 30, 150, 20)
                .build());

        updateButtons();
    }

    private void onSearchChanged(String search) {
        filteredOptions.clear();
        String lowerSearch = search.toLowerCase();

        for (String option : allOptions) {
            if (option.toLowerCase().contains(lowerSearch)) {
                filteredOptions.add(option);
            }
        }

        scrollOffset = 0;
        updateButtons();
    }

    private void updateButtons() {
        // Remove old selection buttons
        this.clearChildren();
        this.addSelectableChild(searchField);

        // Re-add cancel and clear buttons
        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Cancel"),
                        button -> this.close()
                )
                .dimensions(this.width / 2 - 155, this.height - 30, 150, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                        Text.literal("Clear"),
                        button -> {
                            searchField.setText("");
                            onSearchChanged("");
                        }
                )
                .dimensions(this.width / 2 + 5, this.height - 30, 150, 20)
                .build());

        // Add selection buttons
        int startY = 50;
        int buttonWidth = 300;
        int visibleCount = Math.min(ITEMS_PER_PAGE, filteredOptions.size() - scrollOffset);

        for (int i = 0; i < visibleCount; i++) {
            int index = i + scrollOffset;
            if (index >= filteredOptions.size()) break;

            String option = filteredOptions.get(index);
            int yPos = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);

            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal(formatOption(option)),
                            button -> {
                                onSelect.accept(option);
                                this.close();
                            }
                    )
                    .dimensions(this.width / 2 - buttonWidth / 2, yPos, buttonWidth, BUTTON_HEIGHT)
                    .build());
        }

        // Scroll buttons
        if (scrollOffset > 0) {
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("▲ Up"),
                            button -> {
                                scrollOffset = Math.max(0, scrollOffset - 1);
                                updateButtons();
                            }
                    )
                    .dimensions(this.width / 2 - 355, 50, 60, 20)
                    .build());
        }

        if (scrollOffset + ITEMS_PER_PAGE < filteredOptions.size()) {
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal("▼ Down"),
                            button -> {
                                scrollOffset = Math.min(filteredOptions.size() - ITEMS_PER_PAGE, scrollOffset + 1);
                                updateButtons();
                            }
                    )
                    .dimensions(this.width / 2 - 355, 80, 60, 20)
                    .build());
        }
    }

    private String formatOption(String option) {
        if (option == null || option.isEmpty()) return "Unknown";
        String[] parts = option.split(":");
        if (parts.length == 2) {
            String name = parts[1].replace("_", " ");
            // Capitalize first letter of each word
            String[] words = name.split(" ");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return result.toString().trim() + " (" + option + ")";
        }
        return option;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                5,
                0xFFFFFF
        );

        // Search field
        searchField.render(context, mouseX, mouseY, delta);

        // Results count
        String resultsText = "Showing " + Math.min(ITEMS_PER_PAGE, filteredOptions.size() - scrollOffset)
                + " of " + filteredOptions.size() + " results";
        context.drawTextWithShadow(
                this.textRenderer,
                resultsText,
                this.width / 2 - this.textRenderer.getWidth(resultsText) / 2,
                this.height - 45,
                0xAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) {
            // Scroll up
            scrollOffset = Math.max(0, scrollOffset - 1);
            updateButtons();
            return true;
        } else if (verticalAmount < 0) {
            // Scroll down
            scrollOffset = Math.min(filteredOptions.size() - ITEMS_PER_PAGE, scrollOffset + 1);
            updateButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Static factory methods
    public static SelectionScreen createMobSelection(Screen parent, Consumer<String> onSelect) {
        List<String> mobs = MinecraftRegistryHelper.getAllMobIds();
        return new SelectionScreen(parent, "Select Mob", mobs, onSelect);
    }

    public static SelectionScreen createItemSelection(Screen parent, Consumer<String> onSelect) {
        List<String> items = MinecraftRegistryHelper.getAllItemIds();
        return new SelectionScreen(parent, "Select Item", items, onSelect);
    }
}