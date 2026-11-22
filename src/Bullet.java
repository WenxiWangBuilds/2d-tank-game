public class Bullet {
    public static final double RADIUS = 10;

    private Vector position;
    private final Vector velocity;
    private final boolean fromPlayer;

    public Bullet(Vector position, Vector direction) {
        this(position, direction, 700, true);
    }

    public Bullet(Vector position, Vector direction, double speed, boolean fromPlayer) {
        this.position = position;
        this.velocity = direction.normalized().scale(speed);
        this.fromPlayer = fromPlayer;
    }

    public void update(double delta) {
        position = position.add(velocity.scale(delta));
    }

    public Vector getPosition() {
        return position;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }
}