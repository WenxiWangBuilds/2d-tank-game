import java.util.Random;

/**
 * Is a subclass of player, but is not a player, does not take input from keyboard
 */
public class Enemy extends Player {
    private double perfered_distance;
    private final int level;
    private final Random rng = new Random();
    private double strafeDirection = 1;
    private double strafeTimer = 0;

    public Enemy(Vector position, int level) {
        super(position);
        this.level = level;

        speed = Math.min(150 + level * 18, 380);
        maxHealth = Math.min(3 + level, 10); //capped at 10
        health = maxHealth;
        reloadTime = Math.max(1.4 * Math.pow(0.9, level), 0.3);
        reloadTimer = reloadTime; // no spawn spam
        bulletSpeed = Math.min(350 + level * 20, 600);
        invulnerabilityTime = 0;
        this.perfered_distance = Math.random() * 300 + 60; //randomized to keep things interesting
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void update(double delta) {
        tick(delta); // no keyboard movement for the enemy
    }

    public void update(double delta, Player target) {
        Vector toTarget = target.getPosition().subtract(position);
        double distance = toTarget.length();
        Vector dir = toTarget.normalized();
        Vector perpendicular = new Vector(-dir.y, dir.x);

        // Close in or back off to hold a comfortable range
        double radial = 0;
        if (distance > perfered_distance + 60) radial = 1;
        else if (distance < perfered_distance - 60) radial = -1;

        // Strafe left or right, changing direction at random intervals
        strafeTimer -= delta;
        if (strafeTimer <= 0) {
            strafeDirection = rng.nextBoolean() ? 1 : -1;
            strafeTimer = 0.8 + rng.nextDouble() * 1.7;
        }

        Vector velocity = dir.scale(radial).add(perpendicular.scale(strafeDirection * 0.8));
        velocity = velocity.normalized().scale(speed);

        aimAt(target.getPosition());
        move(velocity, delta);
        tick(delta);
    }
}