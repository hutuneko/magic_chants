package io.github.hutuneko.magic_chants.api.chat;

import io.github.hutuneko.magic_chants.api.chat.net.C2SCommitMagicPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.UUID;

// クライアント専用
public class MagicChatScreen extends ChatScreen {
    private static final String PREFIX = "#magic ";
    private final UUID itemUuid;
    private final ItemStack itemStack;
    private boolean closeSent = false;

    public MagicChatScreen(UUID itemUuid, ItemStack itemStack) {
        super("",true);
        this.itemUuid = itemUuid;
        this.itemStack = itemStack;
    }
    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == 257 || keyCode == 335) { // Enter
            this.handleChatInput(this.input.getValue(), true);
            this.input.setValue("");
            return true;
        }
        return super.keyPressed(event);
    }
    @Override
    public void handleChatInput(String text, boolean addToHistory) {
        // 送信直前に必ず #magic.json を付与
        if (!text.startsWith(PREFIX)) text = PREFIX + text;

        // ↓ super(handleChatInput) を呼ぶと画面が閉じるので呼ばない！

        // 履歴に残す（↑第二引数 addToHistory は自前で扱う）
        if (addToHistory) {
            this.minecraft.gui.getChat().addRecentChat(text);
        }

        // 送信：コマンド or チャット
        ClientPacketListener connection;
        connection = this.minecraft.getConnection();
        if (connection != null) {
            if (text.startsWith("/")) {
                // 先頭の / を外して sendCommand（1.20.1）
                connection.sendCommand(text.substring(1));
            } else {
                connection.sendChat(text);
            }
        }

        // 画面は閉じない。入力欄をクリアして続けて打てるようにする
        this.input.setValue("");
        this.input.setResponder(_ -> {}); // （任意）サジェストをリセットしたい場合
        this.setFocused(this.input);
    }
    @Override
    public void removed() {
        // 画面が閉じられた時に一度だけ通知
        if (!closeSent && Minecraft.getInstance().player != null) {
            ClientPacketDistributor.sendToServer(new C2SCommitMagicPacket(itemUuid.toString(),itemStack));
            closeSent = true;
        }
        super.removed();
    }
}
