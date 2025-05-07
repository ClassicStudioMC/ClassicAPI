package fr.jessee.classicAPI;

import com.esotericsoftware.yamlbeans.YamlReader;
import fr.jessee.classicAPI.database.Connection;
import fr.jessee.classicAPI.database.Credentials;
import fr.jessee.classicAPI.feature.Server;
import fr.jessee.classicAPI.util.YamlConfig;
import io.github.cdimascio.dotenv.Dotenv;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public final class ClassicAPI extends JavaPlugin {
    private static Plugin instance;
    private static Dotenv env;
    private Server server;
    private static YamlConfig config;
    private Connection connection;
    private Credentials credentials;
    private LuckPerms luckPerms;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        env = Dotenv.configure().load();
        saveDefaultConfig();

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            Bukkit.getPluginManager().disablePlugin(this);
        }

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("LuckPerms API initialisée avec succès !");
        } else {
            getLogger().severe("Impossible d'obtenir l'API LuckPerms !");
            getServer().getPluginManager().disablePlugin(this);
        }

        try {
            File file = new File(this.getDataFolder(), "config.yml");
            YamlReader reader = new YamlReader(new FileReader(file));
            config = reader.read(YamlConfig.class);
            credentials = new Credentials(
                    config.database.host,
                    config.database.user,
                    config.database.pass,
                    config.database.dbName,
                    config.database.port
            );
            connection = new Connection(credentials);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            server = new Server(
                    this,
                    config.server.port
            );
        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (server != null) {
            server.stop();
        }
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static Dotenv getEnv() {
        return env;
    }

    public static YamlConfig getYamlConfig() {
        return config;
    }

    public Connection getConnection() {
        return connection;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
