package kazdelpatch.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandStop;
import kazdelpatch.runtime.ShutdownReasonContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandStopWithReason extends CommandStop {

    private static final Logger LOGGER = LogManager.getLogger("KazdelPatch");

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
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
        } catch (Throwable t) {
            // Keep vanilla /stop functional even if shutdown reason context cannot be loaded.
            LOGGER.error("[KazdelPatch] Failed to set shutdown reason context; continuing with vanilla stop.", t);
        }
        super.processCommand(sender, args);
    }
}
