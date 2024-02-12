package de.mrjulsen.crn.client.gui.widgets;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.crn.ModMain;
import de.mrjulsen.mcdragonlib.client.gui.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public class NavigatorToast implements Toast {

    private static final long DISPLAY_TIME = 5000L;
    private static final int MAX_LINE_SIZE = 200;

    private static final ResourceLocation MOD_ICON = new ResourceLocation(ModMain.MOD_ID, "textures/mod_icon.png");

    private static final int COLOR_BORDER = 0xFF000000;
    private static final int COLOR_INNER_BORDER = 0xFF286485;
    private static final int COLOR_CANVAS = 0xFF082C4C;

    private Component title;
    private List<FormattedCharSequence> messageLines;
    private long lastChanged;
    private boolean changed;
    private final int width;

    @SuppressWarnings("resource")
    public NavigatorToast(Component pTitle, @Nullable Component pMessage) {
        this(pTitle, nullToEmpty(pMessage), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(pTitle), pMessage == null ? 0 : Minecraft.getInstance().font.width(pMessage))));
    }

    @SuppressWarnings("resource")
    public static NavigatorToast multiline(Component pTitle, Component pMessage) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> list = font.split(pMessage, MAX_LINE_SIZE);
        int lineWidth = Math.max(Math.max(MAX_LINE_SIZE, font.width(pTitle)), list.stream().mapToInt(font::width).max().orElse(MAX_LINE_SIZE));
        return new NavigatorToast(pTitle, list, lineWidth + 48);
    }

    private NavigatorToast(Component pTitle, List<FormattedCharSequence> pMessageLines, int pWidth) {
        this.title = pTitle;
        this.messageLines = pMessageLines;
        this.width = pWidth;
    }

    private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component pMessage) {
        return pMessage == null ? ImmutableList.of() : ImmutableList.of(pMessage.getVisualOrderText());
    }

    public int width() {
        return this.width;
    }

    /**
     * 
     * @param pTimeSinceLastVisible time in milliseconds
     */
    @SuppressWarnings("resource")
    public Toast.Visibility render(PoseStack pPoseStack, ToastComponent pToastComponent, long pTimeSinceLastVisible) {
        if (this.changed) {
            this.lastChanged = pTimeSinceLastVisible;
            this.changed = false;
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int lineWidth = width;
        int lineHeight = 10;
        
        int toastHeight = this.height() + Math.max(0, this.messageLines.size() - 1) * lineHeight + 3;
        GuiComponent.fill(pPoseStack, 0, 0, lineWidth, toastHeight, COLOR_BORDER);
        GuiComponent.fill(pPoseStack, 1, 1, lineWidth - 1, toastHeight - 1, COLOR_INNER_BORDER);
        GuiComponent.fill(pPoseStack, 3, 3, lineWidth - 3, toastHeight - 3, COLOR_CANVAS);

        GuiUtils.blit(MOD_ICON, pPoseStack, 4, this.messageLines == null || this.messageLines.size() <= 1 ? 0 : 4, 32, 32, 0, 0, 64, 64, 64, 64);

        if (this.messageLines == null) {
            pToastComponent.getMinecraft().font.draw(pPoseStack, this.title, 40, lineHeight, -256);
        } else {
            pToastComponent.getMinecraft().font.draw(pPoseStack, this.title, 40, 7.0F, -256);

            for (int i = 0; i < this.messageLines.size(); ++i) {
                pToastComponent.getMinecraft().font.draw(pPoseStack, this.messageLines.get(i), 40, (float) (20 + i * lineHeight), -1);
            }
        }

        return pTimeSinceLastVisible - this.lastChanged < DISPLAY_TIME ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }
}
