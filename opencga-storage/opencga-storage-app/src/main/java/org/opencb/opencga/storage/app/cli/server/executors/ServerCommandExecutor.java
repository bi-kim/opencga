package org.opencb.opencga.storage.app.cli.server.executors;

import org.opencb.opencga.storage.app.cli.CommandExecutor;
import org.opencb.opencga.storage.app.cli.server.options.ServerCommandOptions;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by joaquin on 2/2/17.
 */
public class ServerCommandExecutor extends CommandExecutor {

    private static ServerCommandOptions serverCommandOptions;

    public ServerCommandExecutor(ServerCommandOptions serverCommandOptions) {
        super(serverCommandOptions.commonCommandOptions);
        this.serverCommandOptions = serverCommandOptions;
    }

    @Override
    public void execute() throws Exception {
        String subCommandString = getParsedSubCommand(serverCommandOptions.jCommander);
        switch (subCommandString) {
            case "rest":
                rest();
                break;
            case "grpc":
                grpc();
                break;
            default:
                logger.error("Subcommand not valid");
                break;
        }
    }

    /**
     * rest support: start, stop, status...
     */
    void rest() throws Exception {
        RestCommandExecutor restCommandExecutor = new RestCommandExecutor(serverCommandOptions.restServerCommandOptions,
                configuration, logger);

        if (serverCommandOptions.restServerCommandOptions.start) {
            restCommandExecutor.start();
        } else if (serverCommandOptions.restServerCommandOptions.stop) {
            restCommandExecutor.stop();
        } else {
            logger.error(usage("REST"));
        }
    }

    /**
     * gRPC support: start, stop, status...
     */
    void grpc() throws Exception {
        GrpcCommandExecutor grpcCommandExecutor = new GrpcCommandExecutor(serverCommandOptions.grpcServerCommandOptions,
                configuration, logger);

        if (serverCommandOptions.grpcServerCommandOptions.start) {
            grpcCommandExecutor.start();
        } else if (serverCommandOptions.grpcServerCommandOptions.stop) {
            grpcCommandExecutor.stop();
        } else {
            logger.error(usage("gRPC"));
        }
    }

    /**
     * Error message when no action was specified.
     *
     * @param type  Type of server: REST, GRPC
     * @return      Error message
     */
    private String usage(String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("None action for the ").append(type).append(" server.\n");
        sb.append("\tTo start the server, use --start.\n");
        sb.append("\tTo stop the server, use --stop.\n");
        sb.append("For additional parameters, use -h or --help.\n");
        return sb.toString();
    }
}
