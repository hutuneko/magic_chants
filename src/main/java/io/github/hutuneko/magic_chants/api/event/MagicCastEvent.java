package io.github.hutuneko.magic_chants.api.event;

import io.github.hutuneko.magic_chants.api.magic.MagicCast;
import io.github.hutuneko.magic_chants.api.magic.MagicContext;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class MagicCastEvent extends Event implements ICancellableEvent {
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
