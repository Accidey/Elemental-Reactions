package com.xulai.elementalcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PersistentSparkParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
    private final float startSize;

    protected PersistentSparkParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites) {

        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.get(level.getRandom()));

        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0;
        this.lifetime = 60;
        this.startSize = this.quadSize;
    }

    protected PersistentSparkParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites,
            int lifetime) {

        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.get(level.getRandom()));

        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0;
        this.lifetime = lifetime;
        this.startSize = this.quadSize;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.setSpriteFromAge(this.sprites);

        float lifeFraction = (float) this.age / this.lifetime;
        float fade = 1.0f - lifeFraction * lifeFraction;

        this.quadSize = this.startSize * Math.max(fade, 0.2f);

        float brightness = Math.max(fade, 0.15f);
        this.rCol = brightness;
        this.gCol = brightness;
        this.bCol = brightness;

        if (this.age >= this.lifetime - 4) {
            this.alpha -= 0.25f;
            if (this.alpha < 0) this.alpha = 0;
        }
    }

    @Override
    public int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed, RandomSource random) {

            return new PersistentSparkParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ShortLivedFactory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public ShortLivedFactory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed, RandomSource random) {

            return new PersistentSparkParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, 5 + level.getRandom().nextInt(5));
        }
    }
}
