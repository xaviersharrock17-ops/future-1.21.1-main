package nick;

import net.fabricmc.api.ClientModInitializer;

public class FutureModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[Future] Client initialized!");
    }
}
