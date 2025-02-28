# Deployer Usage

Utility for deploying plugins on Minecraft servers

## Command Example

Here is an example command to run the `Deployer`:

```sh
java -jar deployer.jar \
    --serverAddress <server_address> \
    --username <username> \
    --sshPort <ssh_port> \
    --useSshKey <true/false> \
    --passwordOrKeyPath "<path_to_ssh_key>" \
    --pluginBuildPath "<path_to_plugin_build>" \
    --serverPluginsPath "<remote_plugins_path>" \
    --pluginFilename <plugin_filename> \
    --shouldReload <true/false> \
    --reloadCommand "<reload_command>" \
    --screenName <screen_name>
```

## Arguments Table

| Argument              | Description                                                                 | Required | Default Value |
|-----------------------|-----------------------------------------------------------------------------|----------|---------------|
| `--serverAddress`     | The address of the server to which the plugin will be deployed.             | Yes      | -             |
| `--username`          | The username for the SSH connection.                                        | Yes      | -             |
| `--sshPort`           | The SSH port to use for the connection.                                     | No       | 22            |
| `--useSshKey`         | Whether to use an SSH key for authentication (true/false).                  | No       | false         |
| `--passwordOrKeyPath` | The password or the path to the SSH key for authentication.                 | Yes      | -             |
| `--pluginBuildPath`   | The local path to the plugin build file.                                    | Yes      | -             |
| `--serverPluginsPath` | The remote path on the server where the plugin will be copied.              | Yes      | -             |
| `--pluginFilename`    | The filename of the plugin.                                                 | Yes      | -             |
| `--shouldReload`      | Whether to reload the server after deploying the plugin (true/false).       | No       | false         |
| `--reloadCommand`     | The command to reload the server.                                           | No       | stop          |
| `--screenName`        | The name of the screen session to send the reload command to.               | No       | -             |

## Additional Notes

- Ensure that the SSH key or password provided has the necessary permissions to access the remote server.
- The `reloadCommand` should be a valid command that the server can execute to reload the plugin.
- The `screenName` should match the name of an active screen session on the server.
