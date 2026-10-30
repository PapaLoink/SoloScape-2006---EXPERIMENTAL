package com.rs2.model.skill.magic;

import com.rs2.model.Position;
import com.rs2.model.animation.GraphicEffect;
import com.rs2.model.player.Player;
import com.rs2.model.task.CycleEvent;
import com.rs2.model.task.CycleEventContainer;

/** Original home teleport: draw the circle, sit, read, then disappear. */
public final class HomeTeleportTask extends CycleEvent {
    private static final int[] ANIMATIONS = {4847, 4850, 4853, 4855, 4857};
    private static final int[] GRAPHICS = {800, 801, 802, 803, 804};
    // Legacy sound IDs, translated to the bundled cache by PacketSender.
    // The incantation sound includes the final disappearance.
    private static final int[] SOUNDS = {2500, 2501, 2502, 2503, -1};
    // How long each stage's player animation and graphic actually move in revision 443 (2.98/3.24, 0.86/0.20,
    // 1.16/1.16, 3.00/3.00, 1.20/1.20 s), rounded up to 600 ms ticks. Every one of them then ends on a long
    // hold-until-replaced frame (2-4 s), so waiting out the full lengths left the player frozen between stages.
    // By request: one extra tick each seated before the book comes out, reading before the incantation,
    // and before the player is moved home.
    private static final int[] DURATIONS = {6, 3, 3, 5, 3};
    private final Player player;
    private final Position origin;
    private final int initialHitpoints;
    private int stage;
    private int ticksRemaining;

    public HomeTeleportTask(Player player) {
        this.player = player;
        this.origin = new Position(player.getPosition().getX(), player.getPosition().getY(),
                player.getPosition().getPlane());
        this.initialHitpoints = player.getSkillManager().getCurrentLevels()[3];
    }

    public void start() {
        this.playStage();
    }

    private void playStage() {
        this.player.getUpdateState().setAnimation(ANIMATIONS[this.stage]);
        this.player.getUpdateState().setGraphic(GraphicEffect.createHeight0(GRAPHICS[this.stage]));
        this.player.packetSender.sendSoundEffect(SOUNDS[this.stage], 1, 0);
        this.ticksRemaining = DURATIONS[this.stage];
    }

    @Override
    public void execute(CycleEventContainer container) {
        if (!container.isActive()) {
            return;
        }
        Position position = this.player.getPosition();
        if (this.player.isDead() || this.player.isTeleblocked()
                || this.player.isInTeleportRestrictedArea()
                || this.player.getSkillManager().getCurrentLevels()[3] < this.initialHitpoints
                || position.getX() != this.origin.getX() || position.getY() != this.origin.getY()
                || position.getPlane() != this.origin.getPlane()) {
            container.stop();
            return;
        }
        if (--this.ticksRemaining > 0) {
            return;
        }
        if (++this.stage < ANIMATIONS.length) {
            this.playStage();
            return;
        }
        this.player.moveTo(new Position(3222, 3218, 0));
        this.player.homeTeleportAvailableAtMillis = System.currentTimeMillis() + 30L * 60L * 1000L;
        container.stop();
    }

    @Override
    public void onStop() {
        this.player.getUpdateState().setAnimation(-1);
        this.player.getUpdateState().setGraphic(GraphicEffect.createHeight0(-1));
        this.player.setActionLocked(false);
    }
}
