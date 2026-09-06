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
public class SteamCloudParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
    private final float startSize;
    private final int fadeInEnd;
    private final int fadeOutStart;

    protected SteamCloudParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites) {

        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.get(level.getRandom()));

        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0;
        this.lifetime = 50 + this.random.nextInt(31);
        this.fadeInEnd = (int) (this.lifetime * 0.2);
        this.fadeOutStart = (int) (this.lifetime * 0.6);

        this.startSize = 0.2f + this.random.nextFloat() * 0.2f;
        this.quadSize = this.startSize;

this.rCol = 0.85f + this.random.nextFloat() * 0.1f;
        this.gCol = 0.88f + this.random.nextFloat() * 0.1f;
        this.bCol = 0.95f + this.random.nextFloat() * 0.05f;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.alpha = 0.0f;
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

if (this.age < this.fadeInEnd) {
            this.alpha = (float) this.age / this.fadeInEnd;
        }

        else if (this.age >= this.fadeOutStart) {
            float progress = (float) (this.age - this.fadeOutStart) / (this.lifetime - this.fadeOutStart);
            this.alpha = 1.0f - progress;
        } else {
            this.alpha = 1.0f;
        }

        this.quadSize = this.startSize;

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
            return new SteamCloudParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
