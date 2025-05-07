package fr.jessee.classicAPI.feature;

import fr.jessee.classicAPI.ClassicAPI;
import fr.jessee.classicAPI.util.JsonWebToken;
import fr.jessee.classicAPI.database.Connection;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.*;
import java.util.UUID;

public class Server {
    private final ClassicAPI classicAPI;
    private Javalin app;

    public Server(ClassicAPI classicAPI, String port) throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        this.classicAPI = classicAPI;
        this.ensureUserTableExists();
        this.start(port);
    }

    // Méthode pour démarrer le serveur Javalin
    private void start(String PORT) {
        int p = Integer.parseInt(PORT);
        app = Javalin.create(config -> {
            config.jetty.addConnector((server, _) -> {
                ServerConnector sslConnector = new ServerConnector(server, getSslContextFactory());
                sslConnector.setPort(p);
                return sslConnector;
            });
        }).start(p);

        app.post("/api/handleGrantPermission", this::handleGrantPermission);
    }

    // Configuration SSL pour le serveur
    private static SslContextFactory.Server getSslContextFactory() {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(ClassicAPI.getInstance().getDataFolder().getPath() + "/" + ClassicAPI.getYamlConfig().server.keyStoreName);
        sslContextFactory.setKeyStorePassword(ClassicAPI.getYamlConfig().server.keyStorePassword);
        return sslContextFactory;
    }

    // Arrête l'application Javalin
    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private void handleGrantPermission(Context ctx) {
        try {
            this.authenticate(ctx);
            String uuid = ctx.formParam("uuid");
            String duration = ctx.formParam("duration");
            String permission = ctx.formParam("permission");

            if (uuid == null || duration == null || permission == null) {
                ctx.status(400).result("uuid, duration, permission are required.");
                return;
            }


            // Récupération du joueur
            User user = classicAPI.getLuckPerms().getUserManager().loadUser(UUID.fromString(uuid)).join();

            // Création du node temporaire
            PermissionNode node = (PermissionNode) Node.builder(permission)
                    .expiry(Long.parseLong(duration))
                    .build();

            // Ajoute la permission temporaire
            user.data().add(node);

            // Sauvegarde
            classicAPI.getLuckPerms().getUserManager().saveUser(user);
            ctx.status(200);
        } catch (UnauthorizedResponse e) {
            ctx.status(401).result(e.getMessage());
        }
    }

    // Authentifie une requête en vérifiant le token JWT
    private void authenticate(Context ctx) {
        String authHeader = ctx.header("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Unauthorized.");
        }

        String token = authHeader.substring(7);
        if (!JsonWebToken.isTokenValid(token)) {
            throw new UnauthorizedResponse("Unauthorized.");
        }

        var claims = JsonWebToken.getClaimsFromToken(token);
        String subject = claims.getSubject();

        // Si c’est une requête back-end autorisée
        if ("site_web".equals(subject)) {
            return; // autorisé
        }

        // Sinon, vérifier que c’est un utilisateur connu
        if (!isKnownUser(subject)) {
            throw new UnauthorizedResponse("Unauthorized.");
        }
    }

    private boolean isKnownUser(String username) {
        try (Connection dbConnection = new Connection(classicAPI.getCredentials());
             java.sql.Connection conn = dbConnection.getConnection()) {

            String query = "SELECT 1 FROM api_user WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void ensureUserTableExists() {
        try (Statement stmt = classicAPI.getConnection().getConnection().createStatement()) {
            // Crée la table uniquement si elle n'existe pas
            stmt.execute("CREATE TABLE IF NOT EXISTS api_user (\n" +
                    "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    username VARCHAR(50) NOT NULL UNIQUE,\n" +
                    "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" +
                    "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
                    ");");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification/création de la table 'api_auth' : ", e);
        }
    }
}
