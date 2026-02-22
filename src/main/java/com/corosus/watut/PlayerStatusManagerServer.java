package com.corosus.watut;

import com.corosus.coroutil.util.CULog;
import com.corosus.watut.config.ConfigCommon;
import com.corosus.watut.config.ConfigServerControlledSyncedToClient;
import com.corosus.watut.config.ConfigServerSyncHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class PlayerStatusManagerServer extends PlayerStatusManager {

    @Override
    public void tickPlayer(Player player) {
        getStatus(player).setTicksToMarkPlayerIdleSyncedForClient(ConfigCommon.ticksToMarkPlayerIdle);
        super.tickPlayer(player);

        /*if (player.level().getGameTime() % 20 == 0) {
            sendItemMove(player.level(), new ItemStack(Items.CHEST), player.getX(), player.getY() + 1, player.getZ(), player.getX() + 2, player.getY(), player.getZ());
        }*/
    }

    /**
     * receive data from client, inject the relevant player uuid, and send it right back to the rest of the relevant clients
     *
     * @param player
     * @param data
     */
    public void receiveAny(Player player, CompoundTag data) {
        //CULog.dbg("server side receive packet index " + data.getInt(WatutNetworking.NBTDataPlayerScreenCompressedPixelDataPacketIndex) + " of " + data.getInt(WatutNetworking.NBTDataPlayerScreenCompressedPixelDataPacketCount));
        //CULog.dbg("TEST server side receive packet index " + data.getInt("testIndex"));
        data.putString(WatutNetworking.NBTDataPlayerUUID, player.getUUID().toString());

        if (data.contains(WatutNetworking.NBTDataPlayerGuiStatus)) {
            PlayerStatus.PlayerGuiState playerGuiState = PlayerStatus.PlayerGuiState.get(data.getInt(WatutNetworking.NBTDataPlayerGuiStatus));
            getStatus(player).setPlayerGuiState(playerGuiState);
        }

        if (data.contains(WatutNetworking.NBTDataPlayerChatStatus)) {
            PlayerStatus.PlayerChatState state = PlayerStatus.PlayerChatState.get(data.getInt(WatutNetworking.NBTDataPlayerChatStatus));
            getStatus(player).setPlayerChatState(state);
        }

        if (data.contains(WatutNetworking.NBTDataPlayerIdleTicks)) {
            handleIdleState(player, data.getInt(WatutNetworking.NBTDataPlayerIdleTicks));
            //send latest config setting for ticks to go idle
            data.putInt(WatutNetworking.NBTDataPlayerTicksToGoIdle, ConfigCommon.ticksToMarkPlayerIdle);
        }

        if (data.contains(WatutNetworking.NBTDataPlayerMouseX)) {
            float x = data.getFloat(WatutNetworking.NBTDataPlayerMouseX);
            float y = data.getFloat(WatutNetworking.NBTDataPlayerMouseY);
            boolean pressed = data.getBoolean(WatutNetworking.NBTDataPlayerMousePressed);
            setMouse(player.getUUID(), x, y, pressed);
        }

        //update active snapshot with latest data
        getStatus(player).getNbtCache().merge(data);

        if (data.contains(WatutNetworking.NBTDataPlayerGuiStatus) || data.contains(WatutNetworking.NBTDataPlayerIdleTicks) || data.contains(WatutNetworking.NBTDataPlayerChatStatus)/* || data.contains(WatutNetworking.NBTDataPlayerScreenCompressedPixelData)*/) {
            WatutNetworking.instance().serverSendToClientAll(data);
        } else {
            WatutNetworking.instance().serverSendToClientNear(data, player.position(), ConfigServerControlledSyncedToClient.distanceRequiredToShowGUIInfo, player.level());
        }
    }

    public void handleIdleState(Player player, int idleTicks) {
        if (WatutMod.instance().getPlayerList() == null) return;
        PlayerStatus status = getStatus(player);
        if (WatutMod.instance().getPlayerList().getPlayerCount() > 1 || singleplayerTesting) {
            if (idleTicks > ConfigCommon.ticksToMarkPlayerIdle) {
                if (!status.isIdle()) {
                    broadcast(player.getDisplayName().getString() + " has gone idle");
                }
            } else {
                if (status.isIdle()) {
                    broadcast(player.getDisplayName().getString() + " is no longer idle");
                }
            }
        }
        status.setTicksSinceLastAction(idleTicks);
    }

    public void broadcast(String msg) {
        if (WatutMod.instance().getPlayerList() == null) return;
        if (ConfigCommon.announceIdleStatesInChat) {
            WatutMod.instance().getPlayerList().broadcastSystemMessage(Component.literal(msg), false);
        }
    }

    @Override
    public void playerLoggedIn(Player player) {
        super.playerLoggedIn(player);
    }

    public void syncServerConfigToAllPlayers() {
        if (WatutMod.instance().getPlayerList() == null) return;
        for (ServerPlayer serverPlayer : WatutMod.instance().getPlayerList().getPlayers()) {
            CULog.dbg("sending server config sync to " + serverPlayer.getName());
            WatutNetworking.instance().serverSendToClientPlayer(getServerConfigNBT(), serverPlayer);
        }
    }

    public CompoundTag getServerConfigNBT() {
        CompoundTag nbt = ConfigServerSyncHelper.getInstance().getSyncableConfigOnServer();
        nbt.putBoolean(WatutNetworking.NBTDataServerConfig, true);
        return nbt;
    }


    public void doClickPre(Player player) {
        //System.out.println("? " + pClickType);

        if (!ConfigServerControlledSyncedToClient.showItemsBeingTransferredBetweenPlayerAndContainer) return;
        if (FakePlayerHelper.isFakePlayer(player)) return;
        PlayerStatus playerStatus = getStatus(player);
        if (playerStatus.isPlayerGuiDontSendItemInfo()) return;
        if (playerStatus.getLastBlockOpened().equals(BlockPos.ZERO)) return;

        //we only handle pickup states in post, also only support pickup or swap
        /*if (pClickType != ClickType.SWAP) {
            return;
        }*/

    }

    public void useBlock(Player player, BlockPos pos) {
        if (!ConfigServerControlledSyncedToClient.showItemsBeingTransferredBetweenPlayerAndContainer) return;
        if (FakePlayerHelper.isFakePlayer(player)) return;
        PlayerStatus playerStatus = getStatus(player);
        if (playerStatus.isPlayerGuiDontSendItemInfo()) return;
        playerStatus.setLastBlockOpened(pos);
    }

    public void doClickPost(Player player) {
        if (!ConfigServerControlledSyncedToClient.showItemsBeingTransferredBetweenPlayerAndContainer) return;
        if (FakePlayerHelper.isFakePlayer(player)) return;
        PlayerStatus playerStatus = getStatus(player);
        if (playerStatus.isPlayerGuiDontSendItemInfo()) return;
        if (playerStatus.getLastBlockOpened().equals(BlockPos.ZERO)) return;

    }

}
