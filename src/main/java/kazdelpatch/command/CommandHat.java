package kazdelpatch.command;

import kazdelpatch.hat.HatFeature;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public final class CommandHat extends CommandBase {

    @Override
    public String getCommandName() {
        return "hat";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/hat";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayer) {
            HatFeature.trySwapHeldWithHelmet((EntityPlayer) sender);
        }
    }
}
