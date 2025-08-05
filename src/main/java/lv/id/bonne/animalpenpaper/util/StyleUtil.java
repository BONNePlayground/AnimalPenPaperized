//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.util;


import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class StyleUtil
{
    @NotNull
    public static final Style WHITE = Style.style().
        decoration(TextDecoration.ITALIC, false).
        color(TextColor.color(0xffffff)).build();

    @NotNull
    public static final Style GRAY = Style.style().
        decoration(TextDecoration.ITALIC, false).
        color(TextColor.color(11184810)).build();

    @NotNull
    public static final Style GREEN_COLOR = Style.style().
        decoration(TextDecoration.ITALIC, false).
        color(TextColor.color(5635925)).build();

    @NotNull
    public static final Style RED_COLOR = Style.style().
        decoration(TextDecoration.ITALIC, false).
        color(TextColor.color(16733525)).build();
}
