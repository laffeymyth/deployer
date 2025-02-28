package net.laffeymyth.deployer;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.cli.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Deployer {

    public static void main(String[] args) {
        Deployer deployer = new Deployer();
        deployer.run(args);
    }

    private void run(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Deployer", options);
            return;
        }

        DeployConfig config = new DeployConfig(cmd);

        try {
            System.out.println("Establishing connection to the server");
            JSch jsch = new JSch();
            Session session = jsch.getSession(config.username, config.serverAddress, config.sshPort);

            if (config.useSshKey) {
                jsch.addIdentity(config.passwordOrKeyPath);
                System.out.println("SSH key set");
            } else {
                session.setPassword(config.passwordOrKeyPath);
                System.out.println("Password set");
            }

            Properties configProps = new Properties();
            configProps.put("StrictHostKeyChecking", "no");
            session.setConfig(configProps);

            session.connect();
            System.out.println("Connection successful");

            copyFileToRemote(session, config.pluginBuildPath, config.serverPluginsPath + "/" + config.pluginFilename);
            System.out.println("Plugin successfully transferred!");

            if (config.shouldReload && config.screenName != null) {
                String screenCommand = "screen -S " + config.screenName + " -X stuff \"" + config.reloadCommand + "\\r\"";
                System.out.println("Executing screen command: " + screenCommand);
                executeRemoteCommand(session, screenCommand);
            } else {
                System.out.println("The command to restart was not executed");
            }

            System.out.println("Deployment completed successfully!");
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder("sa")
                .longOpt("serverAddress")
                .desc("Server address")
                .hasArg()
                .required(true)
                .build());

        options.addOption(Option.builder("u")
                .longOpt("username")
                .desc("Username")
                .hasArg()
                .required(true)
                .build());

        options.addOption(Option.builder("sp")
                .longOpt("sshPort")
                .desc("SSH port")
                .hasArg()
                .required(false)
                .build());

        options.addOption(Option.builder("usk")
                .longOpt("useSshKey")
                .desc("Use SSH key (true/false)")
                .hasArg()
                .required(false)
                .build());

        options.addOption(Option.builder("pokp")
                .longOpt("passwordOrKeyPath")
                .desc("Password or path to SSH key")
                .hasArg()
                .required(true)
                .build());

        options.addOption(Option.builder("pbp")
                .longOpt("pluginBuildPath")
                .desc("Plugin build path")
                .hasArg()
                .required(true)
                .build());

        options.addOption(Option.builder("spp")
                .longOpt("serverPluginsPath")
                .desc("Server plugins path")
                .hasArg()
                .required(true)
                .build());

        options.addOption(Option.builder("pfn")
                .longOpt("pluginFilename")
                .desc("Plugin filename")
                .hasArg()
                .required(true)
                .build());

        options.addOption(Option.builder("sr")
                .longOpt("shouldReload")
                .desc("Should reload (true/false)")
                .hasArg()
                .required(false)
                .build());

        options.addOption(Option.builder("rc")
                .longOpt("reloadCommand")
                .desc("Reload command")
                .hasArg()
                .required(false)
                .build());

        options.addOption(Option.builder("sn")
                .longOpt("screenName")
                .desc("Screen name")
                .hasArg()
                .required(false)
                .build());

        return options;
    }

    private static void copyFileToRemote(Session session, String localFilePath, String remoteFilePath) throws Exception {
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        channelSftp.connect();

        try (FileInputStream fis = new FileInputStream(localFilePath)) {
            channelSftp.put(fis, remoteFilePath);
        }

        channelSftp.disconnect();
    }

    private static void executeRemoteCommand(Session session, String command) throws Exception {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command);
        channelExec.connect();

        try (InputStream in = channelExec.getInputStream()) {
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channelExec.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        }

        channelExec.disconnect();
    }

    private static class DeployConfig {
        final String serverAddress;
        final String username;
        final int sshPort;
        final boolean useSshKey;
        final String passwordOrKeyPath;
        final String pluginBuildPath;
        final String serverPluginsPath;
        final String pluginFilename;
        final boolean shouldReload;
        final String reloadCommand;
        final String screenName;

        DeployConfig(CommandLine cmd) {
            this.serverAddress = cmd.getOptionValue("serverAddress");
            this.username = cmd.getOptionValue("username");
            this.sshPort = Integer.parseInt(cmd.getOptionValue("sshPort", "22"));
            this.useSshKey = Boolean.parseBoolean(cmd.getOptionValue("useSshKey", "false"));
            this.passwordOrKeyPath = cmd.getOptionValue("passwordOrKeyPath");
            this.pluginBuildPath = cmd.getOptionValue("pluginBuildPath");
            this.serverPluginsPath = cmd.getOptionValue("serverPluginsPath");
            this.pluginFilename = cmd.getOptionValue("pluginFilename");
            this.shouldReload = Boolean.parseBoolean(cmd.getOptionValue("shouldReload", "false"));
            this.reloadCommand = cmd.getOptionValue("reloadCommand", "stop");
            this.screenName = cmd.getOptionValue("screenName");
        }
    }
}
