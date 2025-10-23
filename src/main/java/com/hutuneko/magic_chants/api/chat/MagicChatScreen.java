package com.hutuneko.magic_chants.api.chat;

import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;

// クライアント専用
public class MagicChatScreen extends ChatScreen {
    private static final String PREFIX = "#magic.json ";

    public MagicChatScreen() {
        super("");
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter
            this.handleChatInput(this.input.getValue(), true);
            this.input.setValue("");
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    @Override
    public boolean handleChatInput(String text, boolean addToHistory) {
        // 送信直前に必ず #magic.json を付与
        if (!text.startsWith(PREFIX)) text = PREFIX + text;

        // ↓ super(handleChatInput) を呼ぶと画面が閉じるので呼ばない！

        // 履歴に残す（↑第二引数 addToHistory は自前で扱う）
        if (addToHistory) {
            if (this.minecraft != null) {
                this.minecraft.gui.getChat().addRecentChat(text);
            }
        }

        // 送信：コマンド or チャット
        ClientPacketListener connection = null;
        if (this.minecraft != null) {
            connection = this.minecraft.getConnection();
        }
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
        this.input.setResponder(s -> {}); // （任意）サジェストをリセットしたい場合
        this.setFocused(this.input);
        return addToHistory;
    }
}
