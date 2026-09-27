package snownee.jade.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.server.level.ServerPlayer;
import snownee.jade.util.JadeServerPlayer;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements JadeServerPlayer {

	@Unique
	private boolean jade$isConnected;

	@Override
	public boolean jade$isConnected() {
		return jade$isConnected;
	}

	@Override
	public void jade$setConnected(boolean connected) {
		this.jade$isConnected = connected;
	}
}
