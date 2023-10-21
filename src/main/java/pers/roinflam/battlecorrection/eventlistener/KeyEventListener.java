package pers.roinflam.battlecorrection.eventlistener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pers.roinflam.battlecorrection.BattleCorrection;
import pers.roinflam.battlecorrection.config.ConfigBattle;
import pers.roinflam.battlecorrection.utils.Reference;

@Mod.EventBusSubscriber
public class KeyEventListener {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (BattleCorrection.keyOpenModList.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
            GuiScreen gui = new GuiModList(Minecraft.getMinecraft().currentScreen);
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
        if (BattleCorrection.keyOpenConfigList.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
            GuiScreen guiParent = new GuiModList(Minecraft.getMinecraft().currentScreen);
            GuiScreen gui = new GuiConfig(guiParent, ConfigElement.from(ConfigBattle.class).getChildElements(), Reference.MOD_ID, false, true, "Battle Correction", "For RPG!");
            Minecraft.getMinecraft().displayGuiScreen(gui);
        }
    }

}