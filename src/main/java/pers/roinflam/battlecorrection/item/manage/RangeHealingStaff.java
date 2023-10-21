package pers.roinflam.battlecorrection.item.manage;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pers.roinflam.battlecorrection.item.ItemStaff;
import pers.roinflam.battlecorrection.utils.util.EntityUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RangeHealingStaff extends ItemStaff {

    private final int radiu = 64;

    public RangeHealingStaff(@Nonnull String name, @Nonnull CreativeTabs creativeTabs) {
        super(name, creativeTabs);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onItemTooltip(@Nonnull ItemTooltipEvent evt) {
        ItemStack itemStack = evt.getItemStack();
        Item item = itemStack.getItem();
        if (item instanceof RangeHealingStaff) {
            evt.getToolTip().add(1, TextFormatting.DARK_GRAY + String.valueOf(TextFormatting.ITALIC) + I18n.format("item.range_healing_staff.tooltip"));
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, @Nonnull EnumHand hand) {
        if (hand.equals(EnumHand.MAIN_HAND)) {
            @Nonnull List<EntityLivingBase> entities = EntityUtil.getNearbyEntities(
                    EntityLivingBase.class,
                    target,
                    radiu
            );
            BlockPos playerPos = playerIn.getPosition();
            int minX = playerPos.getX() - radiu;//计算边界
            int maxX = playerPos.getX() + radiu;
            int minZ = playerPos.getZ() - radiu;
            int maxZ = playerPos.getZ() + radiu;
            double ran = Math.random();
            for (int x = minX; x <= maxX; x++) {//字面意思遍历判断在不在圆内圆上然后生成
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, playerPos.getY(), z);
                    if (isWithinCircle(playerPos, pos, radiu) && playerIn.world.isAirBlock(pos) && playerIn.world.isBlockLoaded(pos) && ran <= 0.25)//范围内圆上（面积内）
                        generateTheParticles(playerIn.world, pos, EnumParticleTypes.VILLAGER_HAPPY, 3, 0.3);
                    //if (isOnCircle(playerPos, pos, 10) && playerIn.world.isAirBlock(pos) && playerIn.world.isBlockLoaded(pos) && ran <= 0.5) {//范围内圈上（圆周上）
                    //    generateTheParticles(playerIn.world, pos, EnumParticleTypes.HEART, 5, 1.5);}
                    ran = Math.random();
                }
            }
            for (@Nonnull EntityLivingBase entityLivingBase : entities)
                generateTheParticles(playerIn.world, entityLivingBase.getPosition(), EnumParticleTypes.HEART, 8, 0);
        }

        if (hand.equals(EnumHand.MAIN_HAND) && !playerIn.world.isRemote) {

            @Nonnull List<EntityLivingBase> entities = EntityUtil.getNearbyEntities(
                    EntityLivingBase.class,
                    target,
                    radiu
            );

            TextComponentTranslation textComponentStringA = new TextComponentTranslation("message.range_healing_staff.recoverfrom", playerIn.getDisplayName());
            textComponentStringA.getStyle().setColor(TextFormatting.RED);//玩家被谁治愈了的输出message
            List<String> entitiesNameList = new ArrayList<>();//一个用来保存被治愈生物的名字的list
            for (@Nonnull EntityLivingBase entityLivingBase : entities) {
                entitiesNameList.add(entityLivingBase.getName());//遍历添加名字
                entityLivingBase.setHealth(entityLivingBase.getMaxHealth());
                generateTheParticles(playerIn.world, entityLivingBase.getPosition(), EnumParticleTypes.HEART, 2, 1);
                if (entityLivingBase instanceof EntityPlayer)
                    entityLivingBase.sendMessage(textComponentStringA);//”玩家被谁治愈了“的输出message
            }
            TextComponentTranslation textComponentStringB = new TextComponentTranslation("message.range_healing_staff.recoversuccess", entitiesNameList);
            textComponentStringB.getStyle().setColor(TextFormatting.RED);//治愈了哪些生物的message
            playerIn.sendMessage(textComponentStringB);

            playerIn.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.7F, 1.0F);

            playerIn.getCooldownTracker().setCooldown(this, 20);
        }
        return true;
    }

    //判断是否在圆内方法
    public boolean isWithinCircle(BlockPos center, BlockPos pos, int radius) {
        int dx = center.getX() - pos.getX();
        int dz = center.getZ() - pos.getZ();
        return (dx * dx + dz * dz) <= (radius * radius);
    }

    //判断是否在圆上方法
    public boolean isOnCircle(BlockPos center, BlockPos pos, int radius) {
        int dx = center.getX() - pos.getX();
        int dz = center.getZ() - pos.getZ();
        return ((dx * dx + dz * dz) >= (radius * radius) - 1 && (dx * dx + dz * dz) <= (radius * radius) + 1);
    }

    //生成粒子方法，参数依次半径，世界，位置，粒子类型，每格随机粒子数目的最大值-2
    public void generateTheParticles(World world, BlockPos pos, EnumParticleTypes enumPar, int boundNum, double ySpeed) {
        int numParticles = world.rand.nextInt(boundNum) + 2; //每格的粒子数：2-4
        for (int i = 0; i < numParticles; i++) {
            numParticles = world.rand.nextInt(boundNum) + 2;
            double offsetX = world.rand.nextDouble();
            double offsetY = world.rand.nextDouble();
            double offsetZ = world.rand.nextDouble();
            /*
            System.out.printf(":A@((" + offsetX + ":" + offsetY + ":" + offsetZ + ":" + numParticles + ":");
            System.out.printf(":" + (pos.getX() + 0.5 + offsetX) + ":" + (pos.getY() + 1 + offsetY) + ":" + (pos.getZ() + 0.5 + offsetZ) + ":" + "))@A:\n");
            */
            world.spawnParticle(enumPar, pos.getX() + 0.5 + offsetX, pos.getY() + 0.2 + offsetY, pos.getZ() + 0.5 + offsetZ, 0, ySpeed, 0);
        }
    }
}
