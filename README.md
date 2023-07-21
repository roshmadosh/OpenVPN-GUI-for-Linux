# GUI for Linux OpenVPN Client  

Project depends on the JavaFX SDK and the JavaFX Graphics (for Linux) jar. Requires the OpenVPN profile configuration file (`.ovpn` or `.conf`) to be in a directory `$HOME/.openvpn3/autoload/`. The directory needs to have an `.autoload`  with the name matching your config file. This autoload file is a single JSON that contains your authentication details (see [docs](https://openvpn.net/blog/openvpn-3-linux-and-auth-user-pass/)).  

Features:  
- Connecting/disconnecting from the autoload config  
- Interface for adding a config file to the appropriate location (in progress)
- Dashboard of your connetion metrics (similar to the Windows OpenVPN GUI). (in progress)  



