package kazdelpatch.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandStop;
import kazdelpatch.runtime.ShutdownReasonContext;

public class CommandStopWithReason extends CommandStop {

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args != null && args.length > 0) {
            String reason = CommandBase.func_82360_a(sender, args, 0).trim();
            if (!reason.isEmpty()) {
                ShutdownReasonContext.setReason(reason);
            } else {
                ShutdownReasonContext.clear();
            }
        } else {
            ShutdownReasonContext.clear();
        }
        super.processCommand(sender, args);
    }
}
