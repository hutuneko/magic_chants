package io.github.hutuneko.magic_chants.api.magic;

public interface BaseMagic{
    void mainMagic(MagicContext ctx);
    void subMagic(MagicContext ctx);
}
