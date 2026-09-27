package com.xulai.elementalcraft.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToxicBlastParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
    private final float startSize;
    private final int fadeOutStart;

    protected ToxicBlastParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites) {

        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.get(level.getRandom()));

        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0;

        this.lifetime = 90 + this.random.nextInt(21);
        this.fadeOutStart = (int) (this.lifetime * 0.7);

this.startSize = 0.375f + this.random.nextFloat() * 0.25f;
        this.quadSize = this.startSize;

this.rCol = 0.3f + this.random.nextFloat() * 0.2f;
        this.gCol = 0.6f + this.random.nextFloat() * 0.2f;
        this.bCol = 0.1f + this.random.nextFloat() * 0.1f;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

this.alpha = 0.8f;
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

this.xd *= 0.92;
        this.yd *= 0.92;
        this.zd *= 0.92;

this.yd += 0.005;

if (this.age >= this.fadeOutStart) {
            float progress = (float) (this.age - this.fadeOutStart) / (this.lifetime - this.fadeOutStart);
            this.alpha = 0.8f * (1.0f - progress);
        }

float ageRatio = (float) this.age / this.lifetime;
        this.quadSize = this.startSize * (1.0f + ageRatio * 0.5f);

        this.move(this.xd, this.yd, this.zd);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public net.minecraft.client.particle.Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ToxicBlastParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
