import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Player implements KeyListener {
    public static final double RADIUS = 25;
    public static final int CANNON_LENGTH = 35;

    protected Vector position;
    protected Vector aimDirection = new Vector(1, 0);
    protected Vector cannonTip;

    protected double speed = 300;
    protected int maxHealth = 5;
    protected int health = 5;
    //protected double reloadTime = 0.005;
    protected double reloadTime = 0.5;     // seconds between shots
    protected double reloadTimer = 0;      // seconds left until the next shot is ready
    protected double bulletSpeed = 600;

    //protected double invulnerabilityTime = 25;
    protected double invulnerabilityTime = 2.5; // grace period after being hit

    protected double invulnerableTimer = 0;

    private boolean up, down, left, right;

    public Player(Vector position) {
        this.position = position;
        this.cannonTip = position.add(aimDirection.scale(CANNON_LENGTH));
    }

    public void reset(Vector newPosition) {
        position = newPosition;
        health = maxHealth;
        reloadTimer = 0;
        invulnerableTimer = 0;
        up = down = left = right = false;
        cannonTip = position.add(aimDirection.scale(CANNON_LENGTH));
    }

    // ---- movement ----

    public void update(double delta) {
        double dx = (right ? 1 : 0) - (left ? 1 : 0);
        double dy = (down ? 1 : 0) - (up ? 1 : 0);
        Vector velocity = new Vector(dx, dy).normalized().scale(speed);
        move(velocity, delta);
        tick(delta);
    }

    protected void move(Vector velocity, double delta) {
        position = position.add(velocity.scale(delta));
        position.x = Math.max(RADIUS, Math.min(App.windowWidth - RADIUS, position.x));
        position.y = Math.max(RADIUS, Math.min(App.windowHeight - RADIUS, position.y));
        cannonTip = position.add(aimDirection.scale(CANNON_LENGTH));
    }

    protected void tick(double delta) {
        if (reloadTimer > 0) reloadTimer = Math.max(0, reloadTimer - delta);
        if (invulnerableTimer > 0) invulnerableTimer = Math.max(0, invulnerableTimer - delta);
    }

    // ---- aiming and shooting ----

    public void aimAt(Vector target) {
        Vector dir = target.subtract(position).normalized();
        if (dir.length() > 0) {
            aimDirection = dir;
        }
        cannonTip = position.add(aimDirection.scale(CANNON_LENGTH));
    }

    public boolean canShoot() {
        return reloadTimer <= 0 && isAlive();
    }

    public Bullet shoot(boolean fromPlayer) {
        reloadTimer = reloadTime;
        return new Bullet(cannonTip, aimDirection, bulletSpeed, fromPlayer);
    }

    // 0 = just fired, 1 = ready to fire
    public double getReloadProgress() {
        if (reloadTime <= 0) return 1;
        return Math.max(0, Math.min(1, 1 - reloadTimer / reloadTime));
    }

    // ---- health ----

    public void takeDamage(int amount) {
        if (isInvulnerable()) return;
        health = Math.max(0, health - amount);
        invulnerableTimer = invulnerabilityTime;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isInvulnerable() {
        return invulnerableTimer > 0;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    // ---- accessors ----

    public Vector getPosition() {
        return position;
    }

    public Vector getCannonTip() {
        return cannonTip;
    }

    public void setCannonTip(Vector cannonTip) {
        this.cannonTip = cannonTip;
    }

    // ---- keyboard input (WASD or arrow keys) ----

    public void keyPressed(KeyEvent e) {
        setKey(e.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent e) {
        setKey(e.getKeyCode(), false);
    }

    public void keyTyped(KeyEvent e) {
    }

    private void setKey(int code, boolean pressed) {
        switch (code) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    up = pressed;    break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  down = pressed;  break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  left = pressed;  break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: right = pressed; break;
            default: break;
        }
    }
}