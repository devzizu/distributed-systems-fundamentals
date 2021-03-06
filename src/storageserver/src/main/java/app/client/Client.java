
package app.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;

import app.Chrono;
import app.Config;
import app.client.api.StorageAPI;
import app.client.handler.ClientService;

public class Client {

    private static Logger LOGGER = Logger.getLogger(Client.class);

    private static int CLIENT_ID;
    private static int CLIENT_PORT;

    public static void main(String[] args) {

        System.out.print("\033[H\033[2J");

        LOGGER.info("Client test class started");

        // ---------------------------------------------------------------------------------------------------
        // reading arguments, with format = "<id>"

        try {

            String congif = "config.toml";

            CLIENT_ID = Integer.parseInt(args[0]);
            Config.read("../" + congif);

            LOGGER.info("Configuration file \"" + congif + "\"");

        } catch (Exception e) {

            LOGGER.error("Error configuring client service port, exiting...");
            return;
        }

        CLIENT_PORT = Config.init_client_port + CLIENT_ID;

        LOGGER.info("Messaging service running @ port = " + CLIENT_PORT);

        // ---------------------------------------------------------------------------------------------------

        FutureResponses responses = new FutureResponses();
        ClientService cli_service = new ClientService(CLIENT_ID, CLIENT_PORT, responses);
        cli_service.start();

        StorageAPI API = new StorageAPI(cli_service);

        // ---------------------------------------------------------------------------------------------------

        if (CLIENT_ID == 0 || CLIENT_ID == 1) {

            // number of keys
            int pedidos_keys = 500;
            // value size (bytes)
            int tam_data = 1;

            // plan test

            Map<Long, byte[]> mapTestPut = new HashMap<>();

            String data = ("X" + CLIENT_ID).repeat(tam_data);
            for (int i = 0; i < pedidos_keys; i++) {
                mapTestPut.put((long) i, data.getBytes());
            }

            // send async put request

            Chrono.start();

            API.setDestinationID(1);
            CompletableFuture<Void> resultPut = API.put(mapTestPut);

            resultPut.thenAccept(voidValue -> {
                Chrono.stop();
                LOGGER.info("(cliente) Recebi confirmação PUT de que terminou, OK!");
                LOGGER.warn(Chrono.stats());
            });

        } else if (CLIENT_ID == 2 || CLIENT_ID == 3) {

            // plan test

            Collection<Long> keysToGet = new HashSet<Long>();

            for (int i = 0; i < 1; i++) {
                keysToGet.add((long) i);
            }

            // send asyn get request
            Chrono.start();

            API.setDestinationID(0);
            CompletableFuture<Map<Long, byte[]>> resultGet = API.get(keysToGet);

            resultGet.thenAccept(map -> {

                Chrono.stop();
                LOGGER.info("(cliente) Recebi confirmação GET de que terminou, OK!");
                LOGGER.warn(Chrono.stats());

                System.out.println("Result map:");
                map.entrySet().forEach(
                        e -> System.out.print("key: " + e.getKey() + ", val: " + (new String(e.getValue())) + ";\t"));
                System.out.println();
            });

        }
    }
}
