package io.magic_chants.api.event;

import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.minecraftforge.eventbus.api.Event;

public class MagicCastEvent extends Event{
    private MagicContext ctx;
    private final MagicCast.Step step;
    public MagicCastEvent(MagicContext ctx, MagicCast.Step step) {
        this.ctx = ctx;
        this.step = step;
    }
    public MagicContext getContext(){
        return ctx;
    }
    public MagicCast.Step getStep(){
        return step;
    }
    public void setContext(MagicContext ctx){
        this.ctx = ctx;
    }
}
