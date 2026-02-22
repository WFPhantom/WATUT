package com.corosus.watut;

import net.minecraft.client.renderer.texture.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class ParticleRegistry {

    public static SpriteInfo chat_idle;
    public static SpriteInfo chat_typing;
    public static SpriteInfo idle;
    public static List<SpriteInfo> particles = new ArrayList<>();

    static {
        chat_idle = add("chat_idle_", 2, 6);
        chat_typing = add("chat_typing_", 6, 2);
        idle = add("idle");
    }

    public static SpriteInfo add(String name) {
        return add(name, 0, 0);
    }

    public static SpriteInfo add(String name, int frames, int tickDelay) {
        SpriteInfo spriteInfo = new SpriteInfo(name, frames, tickDelay);
        particles.add(spriteInfo);
        return spriteInfo;
    }

    public static void textureAtlasUpload(TextureAtlas textureAtlas) {
        if (!textureAtlas.location().equals(TextureAtlas.LOCATION_PARTICLES)) return;
        for (SpriteInfo info : ParticleRegistry.particles) {
            info.setupSprites(textureAtlas);
        }
    }
}
