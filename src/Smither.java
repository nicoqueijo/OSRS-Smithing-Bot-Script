import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankType;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

@ScriptManifest(
        author = "Nico",
        description = "Smiths mithril platebodies at Varrock anvils",
        category = Category.SMITHING,
        version = 1.0,
        name = "Smither"
)
public class Smither extends AbstractScript {

    private final int HAMMER_ID = 2347;
    private final int MITHRIL_PLATEBODY_ID = 1121;
    private final int MITHRIL_BAR_ID = 2359;
    private final int ANVIL = 2097;

    private final Tile ANVIL_NE_TILE = new Tile(3188, 3425, 0);
    private final Tile ANVIL_SW_TILE = new Tile(3187, 3427, 0);
    private final Area ANVIL_AREA = new Area(new Tile(3188, 3425, 0), new Tile(3187, 3427, 0));

    private final Tile BANK_NE_TILE = new Tile(3185, 3435, 0);
    private final Tile BANK_SW_TILE = new Tile(3183, 3436, 0);
    private final Area BANK_AREA = new Area(BANK_NE_TILE, BANK_SW_TILE);

    private final Point PLATEBODY_TOP_LEFT_POINT = new Point(186, 216);
    private final Dimension DIMENSION = new Dimension(30, 30);
    private final Rectangle PLATEBODY_RECTANGLE = new Rectangle(PLATEBODY_TOP_LEFT_POINT, DIMENSION);

    private final int BANK_BOOTH = 34810;

    private int SLEEP_LOWER_BOUND = 4000;
    private int SLEEP_UPPER_BOUND = 5000;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public int onLoop() {
        // Open bank
        getBank().getClosestBank(BankType.BOOTH).interact();
        sleepUntil(() -> getBank().isOpen(), randomSleep());
        // Deposit all platebodies
        if (getBank().isOpen() && getBank().count(MITHRIL_BAR_ID) == 0) {
            log("Stopping. Out of bars.");
            stop();
        }
        getBank().depositAll(MITHRIL_PLATEBODY_ID);
        sleepUntil(() -> !getInventory().contains(MITHRIL_PLATEBODY_ID), randomSleep());
        // Withdraw all bars
        getBank().withdrawAll(MITHRIL_BAR_ID);
        sleepUntil(() -> getInventory().emptySlotCount() == 0, randomSleep());
        // Go to anvil area
        getWalking().walk(ANVIL_AREA.getRandomTile());
        sleepUntil(() -> ANVIL_AREA.contains(getLocalPlayer().getTile()), randomSleep());
        // Click smith anvil
        getGameObjects().closest(ANVIL).interact();
        sleepUntil(() -> getWidgets().getWidgetChild(312, 22) != null &&
                getWidgets().getWidgetChild(312, 22).isVisible(), randomSleep());
        // Click platebody rectangle
        sleepUntil(() -> getWidgets().getWidgetChild(312, 22).interact(), randomSleep());
        // Move mouse out of screen
        sleepUntil(() -> getMouse().moveMouseOutsideScreen(), randomSleep());
        // Wait until done smithing
        sleepUntil(() -> getInventory().count(item -> item.getID() == MITHRIL_BAR_ID) <= 2,
                Calculations.random(15000, 16000));
        // Go to bank area
        getWalking().walk(BANK_AREA.getRandomTile());
        getMouse().moveMouseOutsideScreen();
        // Assure I'm in bank area
        sleepUntil(() -> BANK_AREA.contains(getLocalPlayer().getTile()), randomSleep());
        possiblyTakeBreak();
        return Calculations.random(70, 80);
    }

    private void possiblyTakeBreak() {
        int probabilityOfTakingABreak = Calculations.random(80);
        if (probabilityOfTakingABreak == 0) {
            log("Taking a break.");
            getMouse().moveMouseOutsideScreen();
            int oneMinute = 1000 * 60;
            int threeMinutes = 1000 * 60 * 3;
            sleep(oneMinute, threeMinutes);
        }
    }

    private int randomSleep() {
        return Calculations.random(SLEEP_LOWER_BOUND, SLEEP_UPPER_BOUND);
    }
}
