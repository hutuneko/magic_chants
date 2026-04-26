package io.github.hutuneko.magic_chants.api.player.attribute.magic_power;

import io.github.hutuneko.magic_chants.MagicChants;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class MagicPowerProvider {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MagicChants.MODID);

    public static final Supplier<AttachmentType<MagicPower>> MAGIC_POWER =
            ATTACHMENT_TYPES.register("magic_power", () -> AttachmentType.serializable(MagicPower::new)
                    .copyOnDeath()
                    .build());
}